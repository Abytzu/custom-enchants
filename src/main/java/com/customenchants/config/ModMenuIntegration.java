package com.customenchants.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuIntegration::buildScreen;
    }

    private static Screen buildScreen(Screen parent) {
        ModConfig cfg = ModConfig.get();
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("Custom Enchants Config"))
            .setSavingRunnable(ModConfig::save);

        ConfigEntryBuilder e = builder.entryBuilder();

        // ── Enchant Toggles ──────────────────────────────────────────────────
        ConfigCategory toggles = builder.getOrCreateCategory(Text.literal("Enchant Toggles"));

        toggles.addEntry(e.startBooleanToggle(Text.literal("Telekinesis"), cfg.enableTelekinesis)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.enableTelekinesis = v).build());
        toggles.addEntry(e.startBooleanToggle(Text.literal("Soulbound"), cfg.enableSoulbound)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.enableSoulbound = v).build());
        toggles.addEntry(e.startBooleanToggle(Text.literal("Harvesting"), cfg.enableHarvesting)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.enableHarvesting = v).build());
        toggles.addEntry(e.startBooleanToggle(Text.literal("Replenish"), cfg.enableReplenish)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.enableReplenish = v).build());
        toggles.addEntry(e.startBooleanToggle(Text.literal("Smelting Touch"), cfg.enableSmeltingTouch)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.enableSmeltingTouch = v).build());
        toggles.addEntry(e.startBooleanToggle(Text.literal("Fortune IV"), cfg.enableFortuneFour)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.enableFortuneFour = v).build());
        toggles.addEntry(e.startBooleanToggle(Text.literal("Bedrock Breaker"), cfg.enableBedrockBreaker)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.enableBedrockBreaker = v).build());
        toggles.addEntry(e.startBooleanToggle(Text.literal("Efficiency VI-VII (Mining Speed)"), cfg.enableEfficiency)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.enableEfficiency = v).build());
        toggles.addEntry(e.startBooleanToggle(Text.literal("Unbreakable"), cfg.enableUnbreakable)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.enableUnbreakable = v).build());

        // ── Particle Toggles ─────────────────────────────────────────────────
        ConfigCategory particles = builder.getOrCreateCategory(Text.literal("Particles"));

        particles.addEntry(e.startBooleanToggle(Text.literal("Telekinesis Particles"), cfg.telekinesisParticles)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.telekinesisParticles = v).build());
        particles.addEntry(e.startBooleanToggle(Text.literal("Replenish Particles"), cfg.replenishParticles)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.replenishParticles = v).build());
        particles.addEntry(e.startBooleanToggle(Text.literal("Smelting Touch Particles"), cfg.smeltingTouchParticles)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.smeltingTouchParticles = v).build());
        particles.addEntry(e.startBooleanToggle(Text.literal("Fortune IV Particles"), cfg.fortuneFourParticles)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.fortuneFourParticles = v).build());
        particles.addEntry(e.startBooleanToggle(Text.literal("Harvesting Particles"), cfg.harvestingParticles)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.harvestingParticles = v).build());

        // ── Misc Settings ─────────────────────────────────────────────────────
        ConfigCategory misc = builder.getOrCreateCategory(Text.literal("Settings"));

        misc.addEntry(e.startBooleanToggle(Text.literal("Harvesting: Only Fully Grown"), cfg.harvestingOnlyFullyGrown)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.harvestingOnlyFullyGrown = v).build());
        misc.addEntry(e.startBooleanToggle(Text.literal("Smelting Touch Works With Fortune"), cfg.smeltingTouchWorksWithFortune)
            .setDefaultValue(true).setSaveConsumer(v -> cfg.smeltingTouchWorksWithFortune = v).build());

        return builder.build();
    }
}
