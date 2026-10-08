package com.wizkfly;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// Gói tin client gửi lên server khi người chơi nhấn keybind bật/tắt Fly
public record FlyTogglePayload() implements CustomPacketPayload {
    public static final FlyTogglePayload INSTANCE = new FlyTogglePayload();

    public static final CustomPacketPayload.Type<FlyTogglePayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(WizkFlyMod.MOD_ID, "toggle_fly"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlyTogglePayload> STREAM_CODEC =
        StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
