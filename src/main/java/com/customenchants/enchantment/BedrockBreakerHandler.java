package com.customenchants.enchantment;

import com.customenchants.config.ModConfig;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BedrockBreakerHandler {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(BedrockBreakerHandler::onBreakBefore);
    }

    private static boolean onBreakBefore(World world, PlayerEntity player, BlockPos pos,
                                          BlockState state, BlockEntity blockEntity) {
        if (!ModConfig.get().enableBedrockBreaker) return true;
        if (state.getBlock() != Blocks.BEDROCK) return true;
        if (world.isClient()) return true;
        if (!(world instanceof ServerWorld serverWorld)) return true;

        var registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        var bedrockRef = registry.getOptional(ModEnchantments.BEDROCK_BREAKER);
        if (bedrockRef.isEmpty()) return true;

        ItemStack tool = player.getMainHandStack();
        if (EnchantmentHelper.getLevel(bedrockRef.get(), tool) <= 0) return true;

        // Remove block
        serverWorld.removeBlock(pos, false);

        // Play obsidian break sound (closest to bedrock — heavy stone crumble)
        BlockSoundGroup sounds = Blocks.OBSIDIAN.getDefaultState().getSoundGroup();
        serverWorld.playSound(null, pos,
            sounds.getBreakSound(),
            SoundCategory.BLOCKS,
            (sounds.getVolume() + 1.0f) / 2.0f,
            sounds.getPitch() * 0.8f
        );

        // Drop bedrock item
        Block.dropStack(serverWorld, pos, new ItemStack(Blocks.BEDROCK));

        // Damage the tool (same as breaking a normal block)
        if (player instanceof ServerPlayerEntity serverPlayer) {
            tool.damage(1, serverPlayer, net.minecraft.entity.EquipmentSlot.MAINHAND);
        }

        return false;
    }
}
