package com.wizkfly;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Lớp khởi tạo chính của Wizk Fly Mod phía server và common
public class WizkFlyMod implements ModInitializer {
    public static final String MOD_ID = "wizk-fly-mod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static FlyConfig config;

    @Override
    public void onInitialize() {
        config = FlyConfig.load();
        FlyManager.get().init();
        LOGGER.info("Wizk Fly Mod đã khởi tạo thành công");

        PayloadTypeRegistry.serverboundPlay().register(FlyTogglePayload.TYPE, FlyTogglePayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FlySyncPayload.TYPE, FlySyncPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FlyTogglePayload.TYPE, (payload, context) ->
            FlyManager.get().toggle(context.player()));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            FlyCommand.register(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
            FlyManager.get().onPlayerJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            FlyManager.get().onPlayerLeave(handler.player));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            FlyManager.get().applyState(newPlayer);
            FlyManager.get().syncToClient(newPlayer);
        });

        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
            FlyManager.get().applyState(player);
            FlyManager.get().syncToClient(player);
        });

        ServerTickEvents.END_SERVER_TICK.register(FlyManager.get()::onServerTick);
    }

    public static FlyConfig config() {
        return config;
    }

    // Tải lại cấu hình từ tập tin mà không cần khởi động lại server
    public static void reloadConfig() {
        config = FlyConfig.load();
        LOGGER.info("Đã tải lại cấu hình wizk-fly-mod.toml");
    }
}
