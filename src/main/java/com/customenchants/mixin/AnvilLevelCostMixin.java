package com.customenchants.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilLevelCostMixin {

    @Shadow public Property levelCost;

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void keepCostAtLeastOne(CallbackInfo ci) {
        AnvilScreenHandler self = (AnvilScreenHandler) (Object) this;
        ItemStack right = self.getSlot(1).getStack();
        ItemStack out   = self.getSlot(2).getStack();
        if (out.isEmpty() || !right.isEmpty()) return;
        if (this.levelCost.get() > 1) this.levelCost.set(1);
    }

    @Inject(method = "onTakeOutput", at = @At("HEAD"), cancellable = true)
    private void skipXpOnRenameOnly(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        AnvilScreenHandler self = (AnvilScreenHandler) (Object) this;
        if (!self.getSlot(1).getStack().isEmpty()) return; // not rename-only

        // Put item on cursor (like clicking a slot normally), clear inputs, no XP cost
        self.setCursorStack(stack);
        self.getSlot(0).setStack(ItemStack.EMPTY);
        self.getSlot(2).setStack(ItemStack.EMPTY);
        ci.cancel();
    }
}
