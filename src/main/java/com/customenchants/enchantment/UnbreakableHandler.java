package com.customenchants.enchantment;

import com.customenchants.config.ModConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;

public class UnbreakableHandler {

    private static int ticker = 0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(UnbreakableHandler::onTick);
    }

    private static void onTick(MinecraftServer server) {
        if (++ticker < 20) return;
        ticker = 0;

        if (!ModConfig.get().enableUnbreakable) return;

        server.getPlayerManager().getPlayerList().forEach(player -> {
            var registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            var unbreakRef = registry.getOptional(ModEnchantments.UNBREAKABLE);
            if (unbreakRef.isEmpty()) return;

            PlayerInventory inv = player.getInventory();
            for (int i = 0; i < inv.size(); i++) {
                ItemStack stack = inv.getStack(i);
                if (stack.isEmpty()) continue;

                boolean hasEnchant = EnchantmentHelper.getLevel(unbreakRef.get(), stack) > 0;
                boolean isUnbreakable = stack.contains(DataComponentTypes.UNBREAKABLE);

                if (hasEnchant && !isUnbreakable) {
                    stack.set(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE);
                } else if (!hasEnchant && isUnbreakable) {
                    stack.remove(DataComponentTypes.UNBREAKABLE);
                }
            }
        });
    }
}
