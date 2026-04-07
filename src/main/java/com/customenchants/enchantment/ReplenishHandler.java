package com.customenchants.enchantment;

import com.customenchants.config.ModConfig;
import com.customenchants.config.ParticleHelper;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ReplenishHandler {

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(ReplenishHandler::onBlockBreak);
    }

    private static void onBlockBreak(World world, PlayerEntity player, BlockPos pos,
                                      BlockState state, BlockEntity blockEntity) {
        if (!ModConfig.get().enableReplenish) return;
        if (world.isClient()) return;

        ItemStack heldItem = player.getMainHandStack();
        if (heldItem.isEmpty()) return;

        var registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        var replenishRef = registry.getOptional(ModEnchantments.REPLENISH);
        if (replenishRef.isEmpty()) return;

        if (EnchantmentHelper.getLevel(replenishRef.get(), heldItem) <= 0) return;

        BlockState below = world.getBlockState(pos.down());
        if (!(below.getBlock() instanceof FarmlandBlock)) return;
        if (!isFullyGrown(state)) return;

        BlockState replant = getSeedReplant(player, world, pos, state);
        if (replant != null) {
            world.setBlockState(pos, replant);
            if (world instanceof ServerWorld serverWorld) {
                ParticleHelper.spawnReplenishParticles(serverWorld, pos);
            }
        }
    }

    private static BlockState getSeedReplant(PlayerEntity player, World world, BlockPos pos, BlockState brokenState) {
        Block brokenBlock = brokenState.getBlock();
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            BlockState toPlace = null;
            if (stack.isOf(Items.WHEAT_SEEDS) && brokenBlock instanceof CropBlock) toPlace = Blocks.WHEAT.getDefaultState();
            else if (stack.isOf(Items.CARROT) && brokenBlock instanceof CropBlock) toPlace = Blocks.CARROTS.getDefaultState();
            else if (stack.isOf(Items.POTATO) && brokenBlock instanceof CropBlock) toPlace = Blocks.POTATOES.getDefaultState();
            else if (stack.isOf(Items.BEETROOT_SEEDS) && brokenBlock instanceof CropBlock) toPlace = Blocks.BEETROOTS.getDefaultState();
            else if (stack.isOf(Items.NETHER_WART) && brokenBlock instanceof NetherWartBlock) {
                if (world.getBlockState(pos.down()).isOf(Blocks.SOUL_SAND)) toPlace = Blocks.NETHER_WART.getDefaultState();
            }
            if (toPlace != null) { stack.decrement(1); return toPlace; }
        }
        return null;
    }

    private static boolean isFullyGrown(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof CropBlock c) return c.isMature(state);
        if (b instanceof SweetBerryBushBlock) return state.get(SweetBerryBushBlock.AGE) == 3;
        if (b instanceof NetherWartBlock) return state.get(NetherWartBlock.AGE) == 3;
        return false;
    }
}
