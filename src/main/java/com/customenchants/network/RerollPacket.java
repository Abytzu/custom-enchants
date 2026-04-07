package com.customenchants.network;

import com.customenchants.mixin.EnchantmentSeedAccessor;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class RerollPacket {

    public static final CustomPayload.Id<Payload> ID =
        new CustomPayload.Id<>(Identifier.of("customenchants", "reroll"));

    public static final PacketCodec<PacketByteBuf, Payload> CODEC =
        PacketCodec.unit(new Payload());

    public record Payload() implements CustomPayload {
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ID, CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                if (!(player.currentScreenHandler instanceof EnchantmentScreenHandler handler)) return;

                // Change the seed Property to a new value — this causes the client
                // to regenerate enchantment offers when sendContentUpdates() syncs it
                EnchantmentSeedAccessor accessor = (EnchantmentSeedAccessor) handler;
                int currentSeed = accessor.getSeedProperty().get();
                accessor.getSeedProperty().set(currentSeed + (int) System.currentTimeMillis());
                handler.onContentChanged(handler.getSlot(0).inventory);
                handler.sendContentUpdates();

                player.networkHandler.sendPacket(
                    new net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket(
                        net.minecraft.registry.Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE),
                        net.minecraft.sound.SoundCategory.BLOCKS,
                        player.getX(), player.getY(), player.getZ(),
                        1.0f, player.getRandom().nextFloat() * 0.1f + 0.9f,
                        player.getRandom().nextLong()
                    )
                );
            });
        });
    }
}
