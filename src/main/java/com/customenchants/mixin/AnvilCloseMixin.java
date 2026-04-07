package com.customenchants.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgingScreenHandler.class)
public abstract class AnvilCloseMixin {

    @Inject(method = "onClosed", at = @At("HEAD"), cancellable = true)
    private void returnItemsOnClose(PlayerEntity player, CallbackInfo ci) {
        ForgingScreenHandler self = (ForgingScreenHandler) (Object) this;
        // Return input slots 0 and 1 to the player manually,
        // then cancel vanilla onClosed which tries to dropInventory via the context
        // (which with EMPTY context would drop them into the void)
        for (int i = 0; i <= 1; i++) {
            ItemStack stack = self.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                if (!player.getInventory().insertStack(stack)) {
                    player.dropItem(stack, false);
                }
                self.getSlot(i).setStack(ItemStack.EMPTY);
            }
        }
        ci.cancel();
    }
}
