package com.customenchants.enchantment;

import com.customenchants.config.ModConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class EfficiencyUpgradeHandler {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(EfficiencyUpgradeHandler::onTick);
    }

    private static void onTick(MinecraftServer server) {
        if (!ModConfig.get().enableEfficiency) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!(player.currentScreenHandler instanceof AnvilScreenHandler anvil)) continue;

            ItemStack left  = anvil.getSlot(0).getStack();
            ItemStack right = anvil.getSlot(1).getStack();
            ItemStack out   = anvil.getSlot(2).getStack();

            if (left.isEmpty() || right.isEmpty()) continue;

            Registry<Enchantment> registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
            var vanillaEffOpt = registry.getOptional(Enchantments.EFFICIENCY);
            var customEffOpt  = registry.getOptional(ModEnchantments.EFFICIENCY);
            if (vanillaEffOpt.isEmpty() || customEffOpt.isEmpty()) continue;

            RegistryEntry<Enchantment> vanillaEff = vanillaEffOpt.get();
            RegistryEntry<Enchantment> customEff  = customEffOpt.get();

            int leftVanilla  = getLevel(left,  vanillaEff);
            int rightVanilla = getLevel(right, vanillaEff);
            int leftCustom   = getLevel(left,  customEff);
            int rightCustom  = getLevel(right, customEff);

            int newLevel = 0;
            if (leftVanilla == 5 && rightVanilla == 5 && leftCustom == 0 && rightCustom == 0) {
                newLevel = 6;
            } else if (leftCustom == 6 && rightCustom == 6) {
                newLevel = 7;
            }
            if (newLevel == 0) continue;

            // Build the correct output and put it in slot 2
            ItemStack output = left.copy();
            output.setCount(1);
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
                output.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT));
            builder.set(vanillaEff, 0);
            builder.set(customEff, newLevel);
            output.set(DataComponentTypes.ENCHANTMENTS, builder.build());

            // Only update if output doesn't already have the right enchant (avoid constant updates)
            int currentCustom = getLevel(out, customEff);
            if (currentCustom != newLevel || !out.isEmpty()) {
                anvil.getSlot(2).setStack(output);
                anvil.sendContentUpdates();
            }
        }
    }

    private static int getLevel(ItemStack stack, RegistryEntry<Enchantment> enchant) {
        if (stack.isEmpty()) return 0;
        return stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
            .getLevel(enchant);
    }
}
