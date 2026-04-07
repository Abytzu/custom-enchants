package com.customenchants.client;

import com.customenchants.network.RerollPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.EnchantmentScreenHandler;
import org.lwjgl.glfw.GLFW;

public class CustomEnchantsClient implements ClientModInitializer {

    public static KeyBinding rerollKey;

    @Override
    public void onInitializeClient() {
        rerollKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.customenchants.reroll",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KeyBinding.Category.GAMEPLAY
        ));

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof EnchantmentScreen)) return;
            ScreenKeyboardEvents.afterKeyPress(screen).register((s, keyInput) -> {
                // matchesKey checks against the currently bound key (respects rebinding)
                if (rerollKey.matchesKey(keyInput)) {
                    sendReroll(client);
                }
            });
        });
    }

    private static void sendReroll(MinecraftClient client) {
        if (client.player == null) return;
        if (!(client.player.currentScreenHandler instanceof EnchantmentScreenHandler handler)) return;
        if (handler.getSlot(0).getStack().isEmpty()) return;
        ClientPlayNetworking.send(new RerollPacket.Payload());
    }
}
