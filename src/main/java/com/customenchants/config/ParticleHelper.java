package com.customenchants.config;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Centralised particle spawner for all custom enchantments.
 * All methods are server-side — ServerWorld sends packets to nearby clients.
 */
public class ParticleHelper {

    /**
     * Telekinesis: a rising swirl of enchant glyphs when an item is pulled to inventory.
     */
    public static void spawnTelekinesisParticles(ServerWorld world, Vec3d pos) {
        if (!ModConfig.get().telekinesisParticles) return;

        // Spiral of enchant particles rising upward
        for (int i = 0; i < 12; i++) {
            double angle = (i / 12.0) * 2 * Math.PI;
            double radius = 0.4;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            double dy = (i / 12.0) * 1.2; // rise as spiral progresses
            world.spawnParticles(
                ParticleTypes.ENCHANT,
                pos.x + dx, pos.y + dy, pos.z + dz,
                1, 0, 0.05, 0, 0.05
            );
        }
        // Small burst of portal particles at centre
        world.spawnParticles(
            ParticleTypes.PORTAL,
            pos.x, pos.y + 0.3, pos.z,
            6, 0.2, 0.2, 0.2, 0.1
        );
    }

    /**
     * Replenish: green happy villager particles + composter particles around the replanted crop.
     */
    public static void spawnReplenishParticles(ServerWorld world, BlockPos pos) {
        if (!ModConfig.get().replenishParticles) return;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.1;
        double cz = pos.getZ() + 0.5;

        // Green happy-villager puffs
        world.spawnParticles(
            ParticleTypes.HAPPY_VILLAGER,
            cx, cy, cz,
            8, 0.4, 0.3, 0.4, 0.0
        );
        // Subtle composter (green sparkle) effect
        world.spawnParticles(
            ParticleTypes.COMPOSTER,
            cx, cy + 0.3, cz,
            5, 0.3, 0.2, 0.3, 0.0
        );
    }

    /**
     * Smelting Touch: flame + smoke burst at the broken block position.
     */
    public static void spawnSmeltingTouchParticles(ServerWorld world, BlockPos pos) {
        if (!ModConfig.get().smeltingTouchParticles) return;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        world.spawnParticles(
            ParticleTypes.FLAME,
            cx, cy, cz,
            10, 0.3, 0.3, 0.3, 0.05
        );
        world.spawnParticles(
            ParticleTypes.SMOKE,
            cx, cy + 0.3, cz,
            6, 0.2, 0.2, 0.2, 0.02
        );
        world.spawnParticles(
            ParticleTypes.LAVA,
            cx, cy, cz,
            3, 0.2, 0.1, 0.2, 0.0
        );
    }

    /**
     * Fortune IV: golden sparkle burst at the block position.
     */
    public static void spawnFortuneFourParticles(ServerWorld world, BlockPos pos) {
        if (!ModConfig.get().fortuneFourParticles) return;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        // Gold/totem flash
        world.spawnParticles(
            ParticleTypes.TOTEM_OF_UNDYING,
            cx, cy, cz,
            20, 0.4, 0.4, 0.4, 0.3
        );
        world.spawnParticles(
            ParticleTypes.ENCHANT,
            cx, cy + 0.5, cz,
            10, 0.3, 0.3, 0.3, 0.2
        );
    }

    /**
     * Harvesting: leaf/nature particles on crop break for bonus drops.
     */
    public static void spawnHarvestingParticles(ServerWorld world, BlockPos pos, int bonusCount) {
        if (!ModConfig.get().harvestingParticles) return;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        // More particles for bigger bonuses
        int count = 4 + bonusCount * 2;

        world.spawnParticles(
            ParticleTypes.HAPPY_VILLAGER,
            cx, cy, cz,
            count, 0.4, 0.3, 0.4, 0.0
        );
        world.spawnParticles(
            ParticleTypes.COMPOSTER,
            cx, cy + 0.2, cz,
            bonusCount, 0.3, 0.2, 0.3, 0.0
        );
    }
}
