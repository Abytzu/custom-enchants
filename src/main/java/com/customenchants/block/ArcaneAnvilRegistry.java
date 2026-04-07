package com.customenchants.block;

import com.customenchants.CustomEnchantsMain;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ArcaneAnvilRegistry {

    public static Block ARCANE_ANVIL;
    public static Item ARCANE_ANVIL_ITEM;

    public static void register() {
        Identifier id = Identifier.of(CustomEnchantsMain.MOD_ID, "arcane_anvil");
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);

        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
            .registryKey(key)
            .mapColor(MapColor.IRON_GRAY)
            .requiresTool()
            .strength(5.0f, 1200.0f)
            .sounds(BlockSoundGroup.ANVIL);

        ARCANE_ANVIL = Registry.register(Registries.BLOCK, key, new ArcaneAnvilBlock(settings));
        ARCANE_ANVIL_ITEM = Registry.register(Registries.ITEM, id,
            new BlockItem(ARCANE_ANVIL, new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id))));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(e ->
            e.add(ARCANE_ANVIL_ITEM));
    }
}
