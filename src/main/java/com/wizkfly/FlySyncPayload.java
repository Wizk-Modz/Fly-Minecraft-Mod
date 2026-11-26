package com.wizkfly;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// Gói tin server gửi xuống client để đồng bộ trạng thái bay
public record FlySyncPayload(boolean enabled, boolean flying, float speed) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FlySyncPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(WizkFlyMod.MOD_ID, "sync_fly"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlySyncPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.BOOL, FlySyncPayload::enabled,
            ByteBufCodecs.BOOL, FlySyncPayload::flying,
            ByteBufCodecs.FLOAT, FlySyncPayload::speed,
            FlySyncPayload::new
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
