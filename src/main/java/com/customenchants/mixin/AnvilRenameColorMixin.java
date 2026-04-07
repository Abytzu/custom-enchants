package com.customenchants.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class AnvilRenameColorMixin {

    @Inject(method = "onRenameItem", at = @At("RETURN"))
    private void onRenameItem(RenameItemC2SPacket packet, CallbackInfo ci) {
        String name = packet.getName();
        if (name == null || name.isEmpty()) return;
        // Only act if the name contains color codes
        if (!name.contains("&") && !name.contains("§")) return;

        ServerPlayNetworkHandler self = (ServerPlayNetworkHandler) (Object) this;
        if (!(self.player.currentScreenHandler instanceof AnvilScreenHandler anvil)) return;

        // Convert & to § 
        String translated = name.replace("&", "§");

        // The output slot (index 2) holds the renamed item
        ItemStack output = anvil.getSlot(2).getStack();
        if (output.isEmpty()) return;

        // Set the custom name as a literal Text — the client will render § color codes
        // in item names natively (legacy formatting in item display names is supported)
        output.set(DataComponentTypes.CUSTOM_NAME, Text.literal(translated));
        self.player.currentScreenHandler.sendContentUpdates();
    }
}
