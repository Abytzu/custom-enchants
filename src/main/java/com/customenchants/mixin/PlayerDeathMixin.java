package com.customenchants.mixin;

// Soulbound death handling is done via SoulboundHandler using Fabric's
// ServerLivingEntityEvents.ALLOW_DEATH — no mixin needed here.
// File kept to avoid breaking mixins.json if it references this class.

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
public class PlayerDeathMixin {
    // intentionally empty
}
