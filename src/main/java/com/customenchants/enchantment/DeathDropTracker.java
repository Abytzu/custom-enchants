package com.customenchants.enchantment;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks item entity UUIDs that were spawned by a player death.
 * Telekinesis skips these so the player can pick them up normally after respawning.
 */
public class DeathDropTracker {
    public static final Set<UUID> DEATH_DROP_IDS =
        Collections.newSetFromMap(new ConcurrentHashMap<>());
}
