package com.customenchants.enchantment;

import com.customenchants.CustomEnchantsMain;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEnchantments {

    public static final RegistryKey<Enchantment> TELEKINESIS = RegistryKey.of(
            RegistryKeys.ENCHANTMENT, Identifier.of(CustomEnchantsMain.MOD_ID, "telekinesis"));
    public static final RegistryKey<Enchantment> SOULBOUND = RegistryKey.of(
            RegistryKeys.ENCHANTMENT, Identifier.of(CustomEnchantsMain.MOD_ID, "soulbound"));
    public static final RegistryKey<Enchantment> HARVESTING = RegistryKey.of(
            RegistryKeys.ENCHANTMENT, Identifier.of(CustomEnchantsMain.MOD_ID, "harvesting"));
    public static final RegistryKey<Enchantment> REPLENISH = RegistryKey.of(
            RegistryKeys.ENCHANTMENT, Identifier.of(CustomEnchantsMain.MOD_ID, "replenish"));
    public static final RegistryKey<Enchantment> SMELTING_TOUCH = RegistryKey.of(
            RegistryKeys.ENCHANTMENT, Identifier.of(CustomEnchantsMain.MOD_ID, "smelting_touch"));
    public static final RegistryKey<Enchantment> FORTUNE = RegistryKey.of(
            RegistryKeys.ENCHANTMENT, Identifier.of(CustomEnchantsMain.MOD_ID, "fortune"));
    public static final RegistryKey<Enchantment> BEDROCK_BREAKER = RegistryKey.of(
            RegistryKeys.ENCHANTMENT, Identifier.of(CustomEnchantsMain.MOD_ID, "bedrock_breaker"));
    public static final RegistryKey<Enchantment> EFFICIENCY = RegistryKey.of(
            RegistryKeys.ENCHANTMENT, Identifier.of(CustomEnchantsMain.MOD_ID, "efficiency"));
    public static final RegistryKey<Enchantment> UNBREAKABLE = RegistryKey.of(
            RegistryKeys.ENCHANTMENT, Identifier.of(CustomEnchantsMain.MOD_ID, "unbreakable"));

    public static void register() {
        CustomEnchantsMain.LOGGER.info("Registering Custom Enchantments...");
    }
}
