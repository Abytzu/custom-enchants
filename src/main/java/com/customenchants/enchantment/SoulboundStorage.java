package com.customenchants.enchantment;

import net.minecraft.item.ItemStack;

import java.util.*;

/**
 * Temporary storage to hold soulbound items between death and respawn.
 */
public class SoulboundStorage {

    private static final Map<UUID, List<ItemStack>> PENDING = new HashMap<>();

    public static void storeSoulboundItems(UUID uuid, List<ItemStack> items) {
        PENDING.put(uuid, new ArrayList<>(items));
    }

    public static List<ItemStack> retrieveSoulboundItems(UUID uuid) {
        return PENDING.remove(uuid);
    }

    public static boolean hasSoulboundItems(UUID uuid) {
        return PENDING.containsKey(uuid);
    }
}
