package com.customenchants.enchantment;

// Haste speed boost is now handled via HasteSpeedMixin (calcBlockBreakingDelta).
// No tick-based status effect needed.
public class HasteHandler {
    public static void register() {
        // no-op — speed is applied in HasteSpeedMixin
    }
}
