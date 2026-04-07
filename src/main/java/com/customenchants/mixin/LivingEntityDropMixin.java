package com.customenchants.mixin;

import com.customenchants.config.ModConfig;
import com.customenchants.config.ParticleHelper;
import com.customenchants.enchantment.DeathDropTracker;
import com.customenchants.enchantment.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class LivingEntityDropMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onItemTick(CallbackInfo ci) {
        if (!ModConfig.get().enableTelekinesis) return;

        ItemEntity itemEntity = (ItemEntity) (Object) this;
        if (itemEntity.getEntityWorld().isClient()) return;
        if (itemEntity.age < 5) return;

        // Skip items that were spawned by a player death
        if (DeathDropTracker.DEATH_DROP_IDS.contains(itemEntity.getUuid())) return;

        PlayerEntity nearestPlayer = itemEntity.getEntityWorld().getClosestPlayer(
                itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), 8.0, false);
        if (nearestPlayer == null) return;

        // Don't pull items the player themselves threw/dropped manually
        net.minecraft.entity.Entity owner = itemEntity.getOwner();
        if (owner != null && owner.getUuid().equals(nearestPlayer.getUuid())) return;

        var registry = nearestPlayer.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        var telekRef = registry.getOptional(ModEnchantments.TELEKINESIS);
        if (telekRef.isEmpty()) return;

        boolean hasTelekinesis =
            EnchantmentHelper.getLevel(telekRef.get(), nearestPlayer.getMainHandStack()) > 0 ||
            EnchantmentHelper.getLevel(telekRef.get(), nearestPlayer.getOffHandStack()) > 0;

        if (hasTelekinesis) {
            ItemStack droppedStack = itemEntity.getStack();
            if (!droppedStack.isEmpty()) {
                boolean inserted = nearestPlayer.getInventory().insertStack(droppedStack.copy());
                if (inserted) {
                    itemEntity.discard();
                    if (itemEntity.getEntityWorld() instanceof ServerWorld serverWorld) {
                        ParticleHelper.spawnTelekinesisParticles(serverWorld,
                            new Vec3d(itemEntity.getX(), itemEntity.getY(), itemEntity.getZ()));
                    }
                }
            }
        }
    }
}
