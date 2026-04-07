package com.customenchants.command;

import com.customenchants.config.ModConfig;
import com.customenchants.enchantment.ModEnchantments;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public class CustomEnchantsCommand {

    // Custom enchants kept by short name for backwards compat
    private static final Map<String, RegistryKey<Enchantment>> CUSTOM_MAP = Map.of(
        "telekinesis",    ModEnchantments.TELEKINESIS,
        "soulbound",      ModEnchantments.SOULBOUND,
        "harvesting",     ModEnchantments.HARVESTING,
        "replenish",      ModEnchantments.REPLENISH,
        "smelting_touch", ModEnchantments.SMELTING_TOUCH,
        "fortune",        ModEnchantments.FORTUNE,
        "bedrock_breaker", ModEnchantments.BEDROCK_BREAKER,
        "efficiency",           ModEnchantments.EFFICIENCY,
        "unbreakable",     ModEnchantments.UNBREAKABLE
    );

    // Suggestions: all custom short names + all vanilla enchant IDs (without "minecraft:" prefix)
    private static final SuggestionProvider<ServerCommandSource> ENCHANT_SUGGESTIONS =
        (ctx, builder) -> {
            String input = builder.getRemaining().toLowerCase();
            // Custom enchants (short names)
            CUSTOM_MAP.keySet().stream()
                .filter(s -> s.startsWith(input))
                .forEach(builder::suggest);
            // Vanilla/other mod enchants — suggest without "minecraft:" prefix for brevity
            Registry<Enchantment> registry = ctx.getSource().getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT);
            registry.getKeys().stream()
                .map(k -> {
                    String full = k.getValue().toString();
                    // Strip minecraft: prefix, keep other namespaces as-is
                    return full.startsWith("minecraft:") ? full.substring("minecraft:".length()) : full;
                })
                .filter(s -> !CUSTOM_MAP.containsKey(s)) // don't duplicate custom enchants
                .filter(s -> s.startsWith(input))
                .forEach(builder::suggest);
            return builder.buildFuture();
        };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {

        var node = CommandManager.literal("customenchants")
            .requires(src -> true)
            .then(CommandManager.literal("reload")
                .executes(ctx -> {
                    ModConfig.load();
                    ctx.getSource().sendFeedback(
                        () -> Text.literal("✅ Custom Enchants config reloaded.").formatted(Formatting.GREEN),
                        true
                    );
                    return 1;
                })
            )
            .then(CommandManager.literal("add")
                .then(CommandManager.argument("enchant", StringArgumentType.word())
                    .suggests(ENCHANT_SUGGESTIONS)
                    .executes(ctx -> addEnchant(ctx.getSource(),
                        StringArgumentType.getString(ctx, "enchant"), 1))
                    .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 255))
                        .executes(ctx -> addEnchant(ctx.getSource(),
                            StringArgumentType.getString(ctx, "enchant"),
                            IntegerArgumentType.getInteger(ctx, "level")))
                    )
                )
            )
            .then(CommandManager.literal("remove")
                .then(CommandManager.argument("enchant", StringArgumentType.word())
                    .suggests(ENCHANT_SUGGESTIONS)
                    .executes(ctx -> removeEnchant(ctx.getSource(),
                        StringArgumentType.getString(ctx, "enchant")))
                )
            )
            .then(CommandManager.literal("list")
                .executes(ctx -> listEnchants(ctx.getSource()))
            )
            .then(CommandManager.literal("repair")
                .executes(ctx -> repairItem(ctx.getSource()))
            )
            .build();

        dispatcher.getRoot().addChild(node);
        dispatcher.register(CommandManager.literal("et").redirect(node));

        // /av — open anvil GUI
        dispatcher.register(
            CommandManager.literal("av")
                .requires(src -> true)
                .executes(ctx -> openAnvil(ctx.getSource()))
        );
    }

    private static Optional<RegistryKey<Enchantment>> resolveKey(ServerCommandSource source, String name) {
        Registry<Enchantment> registry = source.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        // 1. Check custom short names first
        if (CUSTOM_MAP.containsKey(name.toLowerCase())) {
            return Optional.of(CUSTOM_MAP.get(name.toLowerCase()));
        }

        // 2. Try as full identifier (e.g. "minecraft:sharpness" or "customenchants:fortune")
        Identifier id = Identifier.tryParse(name.toLowerCase());
        if (id != null) {
            RegistryKey<Enchantment> key = RegistryKey.of(RegistryKeys.ENCHANTMENT, id);
            if (registry.getOptional(key).isPresent()) return Optional.of(key);
        }

        // 3. Try with "minecraft:" prefix (e.g. "sharpness" -> "minecraft:sharpness")
        Identifier withNamespace = Identifier.tryParse("minecraft:" + name.toLowerCase());
        if (withNamespace != null) {
            RegistryKey<Enchantment> key = RegistryKey.of(RegistryKeys.ENCHANTMENT, withNamespace);
            if (registry.getOptional(key).isPresent()) return Optional.of(key);
        }

        return Optional.empty();
    }

    private static int addEnchant(ServerCommandSource source, String enchantName, int requestedLevel) {
        ServerPlayerEntity player = getPlayer(source);
        if (player == null) return 0;

        Optional<RegistryKey<Enchantment>> keyOpt = resolveKey(source, enchantName);
        if (keyOpt.isEmpty()) {
            source.sendError(Text.literal("Unknown enchantment: '" + enchantName + "'").formatted(Formatting.RED));
            return 0;
        }

        ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) {
            source.sendError(Text.literal("Hold an item in your main hand first.").formatted(Formatting.RED));
            return 0;
        }

        var registry = source.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        var entryOpt = registry.getOptional(keyOpt.get());
        if (entryOpt.isEmpty()) {
            source.sendError(Text.literal("Enchantment not found in registry: " + enchantName).formatted(Formatting.RED));
            return 0;
        }
        var entry = entryOpt.get();
        int maxLevel = entry.value().getMaxLevel();
        int targetLevel = Math.min(requestedLevel, maxLevel);

        ItemEnchantmentsComponent current = held.getOrDefault(
            DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(current);
        // If adding our custom Efficiency (level 6 or 7), strip vanilla Efficiency first
        var customEffKey = com.customenchants.enchantment.ModEnchantments.EFFICIENCY;
        if (keyOpt.get().equals(customEffKey) && targetLevel >= 6) {
            var vanillaEffOpt = registry.getOptional(net.minecraft.enchantment.Enchantments.EFFICIENCY);
            vanillaEffOpt.ifPresent(e -> builder.set(e, 0));
        }
        builder.set(entry, targetLevel);
        held.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        player.currentScreenHandler.sendContentUpdates();
        player.getInventory().updateItems();

        final int finalLevel = targetLevel;
        source.sendFeedback(
            () -> Text.literal("✅ ").formatted(Formatting.GREEN)
                .append(Text.literal(enchantName).formatted(Formatting.AQUA))
                .append(Text.literal(" " + toRoman(finalLevel) + " added to held item.").formatted(Formatting.GREEN)),
            true
        );
        return 1;
    }

    private static int removeEnchant(ServerCommandSource source, String enchantName) {
        ServerPlayerEntity player = getPlayer(source);
        if (player == null) return 0;

        Optional<RegistryKey<Enchantment>> keyOpt = resolveKey(source, enchantName);
        if (keyOpt.isEmpty()) {
            source.sendError(Text.literal("Unknown enchantment: '" + enchantName + "'").formatted(Formatting.RED));
            return 0;
        }

        ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) {
            source.sendError(Text.literal("Hold an item in your main hand first.").formatted(Formatting.RED));
            return 0;
        }

        var registry = source.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        var entryOpt = registry.getOptional(keyOpt.get());
        if (entryOpt.isEmpty()) return 0;
        var entry = entryOpt.get();

        ItemEnchantmentsComponent current = held.getOrDefault(
            DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        if (current.getLevel(entry) == 0) {
            source.sendError(Text.literal("Held item does not have '" + enchantName + "'.").formatted(Formatting.RED));
            return 0;
        }

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(current);
        builder.set(entry, 0);
        held.set(DataComponentTypes.ENCHANTMENTS, builder.build());

        source.sendFeedback(
            () -> Text.literal("✅ Removed ").formatted(Formatting.GREEN)
                .append(Text.literal(enchantName).formatted(Formatting.AQUA))
                .append(Text.literal(" from held item.").formatted(Formatting.GREEN)),
            true
        );
        return 1;
    }

    private static int listEnchants(ServerCommandSource source) {
        ServerPlayerEntity player = getPlayer(source);
        if (player == null) return 0;

        ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) {
            source.sendError(Text.literal("Hold an item in your main hand first.").formatted(Formatting.RED));
            return 0;
        }

        var registry = source.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        ItemEnchantmentsComponent enchants = held.getOrDefault(
            DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

        source.sendFeedback(() -> Text.literal("─── Enchantments ───").formatted(Formatting.GOLD, Formatting.BOLD), false);

        if (enchants.isEmpty()) {
            source.sendFeedback(() -> Text.literal("  (no enchantments)").formatted(Formatting.GRAY), false);
            return 1;
        }

        enchants.getEnchantments().forEach(regEntry -> {
            int level = enchants.getLevel(regEntry);
            int max = regEntry.value().getMaxLevel();
            String id = regEntry.getIdAsString();
            boolean isCustom = CUSTOM_MAP.values().stream()
                .anyMatch(k -> registry.getOptional(k).map(re -> re.equals(regEntry)).orElse(false));
            String displayName = isCustom
                ? id.replace("customenchants:", "").replace("_", " ")
                : id.replace("minecraft:", "").replace("_", " ");

            source.sendFeedback(
                () -> Text.literal("  ✦ ").formatted(isCustom ? Formatting.YELLOW : Formatting.GRAY)
                    .append(Text.literal(displayName).formatted(isCustom ? Formatting.AQUA : Formatting.WHITE))
                    .append(Text.literal(" " + toRoman(level) + " (" + level + "/" + max + ")").formatted(Formatting.GRAY)),
                false
            );
        });
        return 1;
    }

    private static ServerPlayerEntity getPlayer(ServerCommandSource source) {
        try {
            return source.getPlayerOrThrow();
        } catch (Exception e) {
            source.sendError(Text.literal("Must be run by a player."));
            return null;
        }
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV";
            case 5 -> "V"; case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII";
            case 9 -> "IX"; case 10 -> "X"; default -> String.valueOf(n);
        };
    }

    private static int repairItem(ServerCommandSource source) {
        ServerPlayerEntity player = getPlayer(source);
        if (player == null) return 0;

        net.minecraft.item.ItemStack held = player.getMainHandStack();
        if (held.isEmpty()) {
            source.sendError(Text.literal("Hold an item in your main hand first.").formatted(Formatting.RED));
            return 0;
        }
        if (!held.isDamageable()) {
            source.sendError(Text.literal("That item does not have durability.").formatted(Formatting.RED));
            return 0;
        }

        held.set(DataComponentTypes.DAMAGE, 0);
        player.currentScreenHandler.sendContentUpdates();
        player.getInventory().updateItems();

        source.sendFeedback(
            () -> Text.literal("✅ Item repaired to full durability.").formatted(Formatting.GREEN),
            true
        );
        return 1;
    }

    private static int openAnvil(ServerCommandSource source) {
        ServerPlayerEntity player = getPlayer(source);
        if (player == null) return 0;

        // ScreenHandlerContext.EMPTY always returns true for canUse — no block needed
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, inv, p) -> new AnvilScreenHandler(syncId, inv,
                ScreenHandlerContext.EMPTY),
            Text.literal("Anvil")
        ));
        return 1;
    }
}
