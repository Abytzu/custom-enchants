package com.customenchants.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("customenchants.json");

    private static ModConfig INSTANCE;

    // ── Enchant toggles ──────────────────────────────────────────────────────
    public boolean enableTelekinesis    = true;
    public boolean enableSoulbound      = true;
    public boolean enableHarvesting     = true;
    public boolean enableReplenish      = true;
    public boolean enableSmeltingTouch  = true;
    public boolean enableFortuneFour    = true;
    public boolean enableBedrockBreaker = true;
    public boolean enableEfficiency          = true;
    public boolean enableUnbreakable    = true;

    // ── Particle toggles ─────────────────────────────────────────────────────
    public boolean telekinesisParticles   = true;
    public boolean replenishParticles     = true;
    public boolean smeltingTouchParticles = true;
    public boolean fortuneFourParticles   = true;
    public boolean harvestingParticles    = true;

    // ── Harvesting settings ───────────────────────────────────────────────────
    public boolean harvestingOnlyFullyGrown = true;

    // ── Smelting Touch settings ───────────────────────────────────────────────
    public boolean smeltingTouchWorksWithFortune = true;

    public static ModConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) INSTANCE = new ModConfig();
            } catch (IOException e) {
                System.err.println("[CustomEnchants] Failed to read config, using defaults: " + e.getMessage());
                INSTANCE = new ModConfig();
            }
        } else {
            INSTANCE = new ModConfig();
        }
        save();
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            System.err.println("[CustomEnchants] Failed to save config: " + e.getMessage());
        }
    }
}
