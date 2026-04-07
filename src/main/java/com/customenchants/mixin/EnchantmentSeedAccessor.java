package com.customenchants.mixin;

import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnchantmentScreenHandler.class)
public interface EnchantmentSeedAccessor {
    @Accessor("seed")
    Property getSeedProperty();
}
