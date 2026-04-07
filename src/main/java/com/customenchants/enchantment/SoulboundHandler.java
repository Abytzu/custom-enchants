package com.customenchants.enchantment;

import com.customenchants.config.ModConfig;
import com.customenchants.enchantment.DeathDropTracker;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SoulboundHandler {

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register(SoulboundHandler::onDeath);
        ServerPlayerEvents.AFTER_RESPAWN.register(SoulboundHandler::onRespawn);
    }

    private static boolean onDeath(LivingEntity entity, DamageSource source, float damage) {
        if (!(entity instanceof ServerPlayerEntity player)) return true;

        // Register all items currently in inventory as "death drops" so telekinesis
        // won't immediately hoover them up after they hit the ground.
        // We collect entity UUIDs after the items spawn — do it by hooking the world
        // drop via a scheduled task after vanilla drops happen (1 tick delay).
        ServerWorld world = (ServerWorld) player.getEntityWorld();
        world.getServer().execute(() -> {
            // Gather any ItemEntity within 3 blocks of death position that spawned
            // very recently (age <= 2 ticks) — those are the death drops.
            world.getEntitiesByClass(ItemEntity.class,
                player.getBoundingBox().expand(3.0),
                ie -> ie.age <= 2
            ).forEach(ie -> DeathDropTracker.DEATH_DROP_IDS.add(ie.getUuid()));
        });

        if (!ModConfig.get().enableSoulbound) return true;

        var registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        var soulboundRef = registry.getOptional(ModEnchantments.SOULBOUND);
        if (soulboundRef.isEmpty()) return true;
        var soulboundEntry = soulboundRef.get();

        List<ItemStack> soulboundItems = new ArrayList<>();
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty() && EnchantmentHelper.getLevel(soulboundEntry, stack) > 0) {
                soulboundItems.add(stack.copy());
                inv.setStack(i, ItemStack.EMPTY);
            }
        }

        if (!soulboundItems.isEmpty()) {
            SoulboundStorage.storeSoulboundItems(player.getUuid(), soulboundItems);
        }

        return true;
    }

    private static void onRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        // Clear death drop protection — player has respawned and can now pick up normally
        DeathDropTracker.DEATH_DROP_IDS.clear();

        List<ItemStack> items = SoulboundStorage.retrieveSoulboundItems(oldPlayer.getUuid());
        if (items == null || items.isEmpty()) return;

        PlayerInventory inv = newPlayer.getInventory();
        for (ItemStack stack : items) {
            if (!inv.insertStack(stack.copy())) {
                newPlayer.dropItem(stack, false);
            }
        }
    }
}
