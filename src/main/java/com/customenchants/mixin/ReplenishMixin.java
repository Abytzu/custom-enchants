package com.customenchants.mixin;

import com.customenchants.config.ModConfig;
import com.customenchants.config.ParticleHelper;
import com.customenchants.enchantment.ModEnchantments;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Replenish uses Fabric's PlayerBlockBreakEvents instead of a mixin,
 * since PlayerEntity's block breaking method name varies across 1.21.x versions.
 * Registration happens in ReplenishHandler, called from CustomEnchantsMain.
 */
@Mixin(PlayerEntity.class)
public class ReplenishMixin {
    // intentionally empty - logic moved to ReplenishHandler
}
