package com.customenchants;

import com.customenchants.command.CustomEnchantsCommand;
import com.customenchants.config.ModConfig;
import com.customenchants.enchantment.BedrockBreakerHandler;
import com.customenchants.enchantment.EnchantmentEventHandler;
import com.customenchants.enchantment.HasteHandler;
import com.customenchants.enchantment.ModEnchantments;
import com.customenchants.enchantment.ReplenishHandler;
import com.customenchants.enchantment.SoulboundHandler;
import com.customenchants.enchantment.UnbreakableHandler;
import com.customenchants.network.RerollPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomEnchantsMain implements ModInitializer {

    public static final String MOD_ID = "customenchants";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.load();
        ModEnchantments.register();
        ReplenishHandler.register();
        EnchantmentEventHandler.register();
        SoulboundHandler.register();
        BedrockBreakerHandler.register();
        HasteHandler.register();
        UnbreakableHandler.register();
        RerollPacket.register();
        CommandRegistrationCallback.EVENT.register(CustomEnchantsCommand::register);

        LOGGER.info("Custom Enchants mod loaded!");
    }
}
