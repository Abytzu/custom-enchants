package com.customenchants.mixin;

import com.customenchants.config.ModConfig;
import com.customenchants.enchantment.ModEnchantments;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.RegistryKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class HasteSpeedMixin {

    @Inject(method = "calcBlockBreakingDelta", at = @At("RETURN"), cancellable = true)
    private void applyEfficiencyBoost(PlayerEntity player, BlockView world, BlockPos pos,
                                       CallbackInfoReturnable<Float> cir) {
        if (!ModConfig.get().enableEfficiency) return;

        var registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        var effRef = registry.getOptional(ModEnchantments.EFFICIENCY);
        if (effRef.isEmpty()) return;

        int level = EnchantmentHelper.getLevel(effRef.get(), player.getMainHandStack());
        if (level <= 0) level = EnchantmentHelper.getLevel(effRef.get(), player.getOffHandStack());
        if (level != 6 && level != 7) return; // only handle our extended levels

        BlockState self = (BlockState) (Object) this;
        float hardness = self.getHardness(world, pos);
        if (hardness < 0) return;

        // Vanilla Eff V was stripped — we provide the full speed equivalent:
        // Eff V adds 26 (5²+1). We target 20%/40% faster than Eff V (speed 35 with netherite).
        // Eff VI target speed = 35 * 1.2 = 42 → bonus = 26 + 7 = 33
        // Eff VII target speed = 35 * 1.4 = 49 → bonus = 26 + 14 = 40
        float bonus = level == 6 ? 33.0f : 40.0f;
        cir.setReturnValue(cir.getReturnValue() + bonus / (hardness * 30f));
    }
}
