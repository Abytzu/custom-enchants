package com.customenchants.mixin;

import com.customenchants.config.ModConfig;
import com.customenchants.enchantment.ModEnchantments;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.BlockView;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class BedrockBreakerMixin {

    @Inject(method = "calcBlockBreakingDelta", at = @At("HEAD"), cancellable = true)
    private void overrideBedrockDelta(PlayerEntity player, BlockView world, BlockPos pos,
                                       CallbackInfoReturnable<Float> cir) {
        BlockState self = (BlockState) (Object) this;
        if (self.getBlock() != Blocks.BEDROCK) return;
        if (!ModConfig.get().enableBedrockBreaker) return;

        var registry = player.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);
        var bedrockRef = registry.getOptional(ModEnchantments.BEDROCK_BREAKER);
        if (bedrockRef.isEmpty()) return;

        ItemStack tool = player.getMainHandStack();
        if (EnchantmentHelper.getLevel(bedrockRef.get(), tool) <= 0) return;

        // Use obsidian as proxy to get the tool's actual mining speed for hard blocks.
        // getMiningSpeedMultiplier on obsidian gives: netherite=9, diamond=8, iron=6,
        // stone=4, wood=2, hand=1 — exactly what we want.
        float speed = tool.getMiningSpeedMultiplier(Blocks.OBSIDIAN.getDefaultState());

        // Efficiency: adds effLevel^2 + 1 to speed (vanilla formula)
        var effRef = registry.getOptional(Enchantments.EFFICIENCY);
        if (effRef.isPresent()) {
            int effLevel = EnchantmentHelper.getLevel(effRef.get(), tool);
            if (effLevel > 0) speed += effLevel * effLevel + 1;
        }

        // Haste / Mining Fatigue
        var haste = player.getStatusEffect(StatusEffects.HASTE);
        if (haste != null) speed *= 1.0f + (haste.getAmplifier() + 1) * 0.2f;

        var fatigue = player.getStatusEffect(StatusEffects.MINING_FATIGUE);
        if (fatigue != null) {
            speed *= switch (fatigue.getAmplifier()) {
                case 0  -> 0.3f;
                case 1  -> 0.09f;
                case 2  -> 0.0027f;
                default -> 8.1e-4f;
            };
        }

        // Penalise mining while airborne (vanilla behaviour)
        if (!player.isOnGround()) speed /= 5.0f;

        // delta = speed / (hardness * 30). Hardness 117 → netherite+eff5 ≈ 5s
        float delta = speed / (117f * 30f);
        cir.setReturnValue(Math.min(delta, 1.0f));
    }
}
