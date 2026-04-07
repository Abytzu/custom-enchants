package com.customenchants.enchantment;

import com.customenchants.config.ModConfig;
import com.customenchants.config.ParticleHelper;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraft.entity.ExperienceOrbEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EnchantmentEventHandler {

    private static final Random RANDOM = new Random();

    // Track positions handled by smelting touch so fortune AFTER event skips them
    private static final java.util.Set<BlockPos> smeltingHandled =
        java.util.Collections.synchronizedSet(new java.util.HashSet<>());

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register(EnchantmentEventHandler::onBlockBreakBefore);
        PlayerBlockBreakEvents.AFTER.register(EnchantmentEventHandler::onBlockBreakAfter);
    }

    private static boolean onBlockBreakBefore(World world, PlayerEntity player, BlockPos pos,
                                               BlockState state, BlockEntity blockEntity) {
        if (world.isClient()) return true;
        if (!(world instanceof ServerWorld serverWorld)) return true;

        ItemStack tool = player.getMainHandStack();
        var registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        // ── Smelting Touch ────────────────────────────────────────────────────────
        if (ModConfig.get().enableSmeltingTouch) {
            var silkRef = registry.getOptional(Enchantments.SILK_TOUCH);
            int silkLevel = silkRef.isPresent() ? EnchantmentHelper.getLevel(silkRef.get(), tool) : 0;
            if (silkLevel == 0) {
                var smeltRef = registry.getOptional(ModEnchantments.SMELTING_TOUCH);
                if (smeltRef.isPresent() && EnchantmentHelper.getLevel(smeltRef.get(), tool) > 0) {

                    // Check fortune level on the tool to apply multiplier ourselves
                    int fortuneLevel = 0;
                    var f4Ref = registry.getOptional(ModEnchantments.FORTUNE);
                    if (f4Ref.isPresent()) fortuneLevel = EnchantmentHelper.getLevel(f4Ref.get(), tool);

                    // Get raw drops (without fortune applied, since we handle it)
                    // Use a copy of the tool without enchants to get base drops
                    List<ItemStack> rawDrops = Block.getDroppedStacks(state, serverWorld, pos, blockEntity, player, tool);

                    boolean anyConverted = false;
                    List<ItemStack> finalDrops = new ArrayList<>();
                    for (ItemStack drop : rawDrops) {
                        Item out = SMELT_MAP.get(drop.getItem());
                        if (out != null) {
                            // Apply fortune multiplier to smelted items
                            int count = drop.getCount();
                            if (fortuneLevel > 0 && RANDOM.nextInt(6) != 0) {
                                count *= fortuneLevel;
                            }
                            finalDrops.add(new ItemStack(out, count));
                            anyConverted = true;
                        } else {
                            finalDrops.add(drop.copy());
                        }
                    }

                    if (anyConverted) {
                        smeltingHandled.add(pos.toImmutable());
                        serverWorld.removeBlock(pos, false);
                        for (ItemStack drop : finalDrops) {
                            Block.dropStack(serverWorld, pos, drop);
                        }
                        // Drop XP that the ore block would have given
                        int xpDrop = getOreXp(state.getBlock());
                        if (xpDrop > 0) {
                            ExperienceOrbEntity.spawn(serverWorld, net.minecraft.util.math.Vec3d.ofCenter(pos), xpDrop);
                        }
                        ParticleHelper.spawnSmeltingTouchParticles(serverWorld, pos);
                        return false;
                    }
                }
            }
        }

        // ── Harvesting ────────────────────────────────────────────────────────────
        if (ModConfig.get().enableHarvesting && isFarmingBlock(state) && isFullyGrown(state)) {
            var harvestRef = registry.getOptional(ModEnchantments.HARVESTING);
            if (harvestRef.isPresent()) {
                int level = EnchantmentHelper.getLevel(harvestRef.get(), tool);
                if (level > 0) {
                    List<ItemStack> drops = Block.getDroppedStacks(state, serverWorld, pos, blockEntity, player, tool);
                    int totalBonus = 0;
                    for (ItemStack drop : drops) {
                        int bonus = fortuneBonus(level);
                        if (bonus > 0) {
                            Block.dropStack(serverWorld, pos, new ItemStack(drop.getItem(), bonus));
                            totalBonus += bonus;
                        }
                    }
                    if (totalBonus > 0) ParticleHelper.spawnHarvestingParticles(serverWorld, pos, totalBonus);
                }
            }
        }

        return true;
    }

    private static void onBlockBreakAfter(World world, PlayerEntity player, BlockPos pos,
                                           BlockState state, BlockEntity blockEntity) {
        if (world.isClient()) return;
        if (!(world instanceof ServerWorld serverWorld)) return;

        // Skip blocks already handled by smelting touch
        BlockPos immutable = pos.toImmutable();
        if (smeltingHandled.remove(immutable)) return;

        ItemStack tool = player.getMainHandStack();
        var registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        // ── Fortune (levels 1-4) ──────────────────────────────────────────────────
        if (ModConfig.get().enableFortuneFour) {
            var fortuneRef = registry.getOptional(Enchantments.FORTUNE);
            int vanillaFortune = fortuneRef.isPresent() ? EnchantmentHelper.getLevel(fortuneRef.get(), tool) : 0;
            if (vanillaFortune == 0) {
                var f4Ref = registry.getOptional(ModEnchantments.FORTUNE);
                if (f4Ref.isPresent()) {
                    int f4Level = EnchantmentHelper.getLevel(f4Ref.get(), tool);
                    if (f4Level > 0 && RANDOM.nextInt(6) != 0) {
                        List<ItemStack> drops = Block.getDroppedStacks(state, serverWorld, pos, blockEntity, player, tool);
                        for (ItemStack drop : drops) {
                            Block.dropStack(serverWorld, pos, new ItemStack(drop.getItem(), drop.getCount() * f4Level));
                        }
                        ParticleHelper.spawnFortuneFourParticles(serverWorld, pos);
                    }
                }
            }
        }
    }

    private static int fortuneBonus(int level) {
        if (RANDOM.nextInt(level + 2) == 0) return 0;
        return 1 + RANDOM.nextInt(level);
    }

    private static boolean isFarmingBlock(BlockState state) {
        Block b = state.getBlock();
        return b instanceof CropBlock || b instanceof SweetBerryBushBlock || b instanceof NetherWartBlock;
    }

    private static boolean isFullyGrown(BlockState state) {
        Block b = state.getBlock();
        if (b instanceof CropBlock c) return c.isMature(state);
        if (b instanceof SweetBerryBushBlock) return state.get(SweetBerryBushBlock.AGE) == 3;
        if (b instanceof NetherWartBlock) return state.get(NetherWartBlock.AGE) == 3;
        return true;
    }


    private static int getOreXp(Block block) {
        // Iron, copper, gold normally give 0 XP on break (XP comes from furnace smelting).
        // With Smelting Touch the furnace step is skipped, so we award 1 XP here instead.
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return 1;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return 1;
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return 1;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) return 1;
        if (block == Blocks.NETHER_GOLD_ORE) return 1;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return 3;
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return 5;
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return 7;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return 5;
        if (block == Blocks.NETHER_QUARTZ_ORE) return 3;
        if (block == Blocks.ANCIENT_DEBRIS) return 2;
        return 0;
    }

    private static final Map<Item, Item> SMELT_MAP = Map.ofEntries(
        Map.entry(Items.RAW_IRON, Items.IRON_INGOT), Map.entry(Items.RAW_GOLD, Items.GOLD_INGOT),
        Map.entry(Items.RAW_COPPER, Items.COPPER_INGOT), Map.entry(Items.IRON_ORE, Items.IRON_INGOT),
        Map.entry(Items.GOLD_ORE, Items.GOLD_INGOT), Map.entry(Items.COPPER_ORE, Items.COPPER_INGOT),
        Map.entry(Items.DEEPSLATE_IRON_ORE, Items.IRON_INGOT), Map.entry(Items.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT),
        Map.entry(Items.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT), Map.entry(Items.NETHER_GOLD_ORE, Items.GOLD_INGOT),
        Map.entry(Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP), Map.entry(Items.COBBLESTONE, Items.STONE),
        Map.entry(Items.COBBLED_DEEPSLATE, Items.DEEPSLATE), Map.entry(Items.STONE, Items.SMOOTH_STONE),
        Map.entry(Items.SANDSTONE, Items.SMOOTH_SANDSTONE), Map.entry(Items.RED_SANDSTONE, Items.SMOOTH_RED_SANDSTONE),
        Map.entry(Items.POTATO, Items.BAKED_POTATO), Map.entry(Items.PORKCHOP, Items.COOKED_PORKCHOP),
        Map.entry(Items.BEEF, Items.COOKED_BEEF), Map.entry(Items.CHICKEN, Items.COOKED_CHICKEN),
        Map.entry(Items.MUTTON, Items.COOKED_MUTTON), Map.entry(Items.RABBIT, Items.COOKED_RABBIT),
        Map.entry(Items.COD, Items.COOKED_COD), Map.entry(Items.SALMON, Items.COOKED_SALMON),
        Map.entry(Items.CLAY_BALL, Items.BRICK), Map.entry(Items.NETHERRACK, Items.NETHER_BRICK),
        Map.entry(Items.CACTUS, Items.GREEN_DYE), Map.entry(Items.KELP, Items.DRIED_KELP),
        Map.entry(Items.WET_SPONGE, Items.SPONGE), Map.entry(Items.OAK_LOG, Items.CHARCOAL),
        Map.entry(Items.SPRUCE_LOG, Items.CHARCOAL), Map.entry(Items.BIRCH_LOG, Items.CHARCOAL),
        Map.entry(Items.JUNGLE_LOG, Items.CHARCOAL), Map.entry(Items.ACACIA_LOG, Items.CHARCOAL),
        Map.entry(Items.DARK_OAK_LOG, Items.CHARCOAL), Map.entry(Items.MANGROVE_LOG, Items.CHARCOAL),
        Map.entry(Items.CHERRY_LOG, Items.CHARCOAL), Map.entry(Items.OAK_WOOD, Items.CHARCOAL),
        Map.entry(Items.SPRUCE_WOOD, Items.CHARCOAL), Map.entry(Items.BIRCH_WOOD, Items.CHARCOAL),
        Map.entry(Items.JUNGLE_WOOD, Items.CHARCOAL), Map.entry(Items.ACACIA_WOOD, Items.CHARCOAL),
        Map.entry(Items.DARK_OAK_WOOD, Items.CHARCOAL), Map.entry(Items.MANGROVE_WOOD, Items.CHARCOAL),
        Map.entry(Items.CHERRY_WOOD, Items.CHARCOAL)
    );
}
