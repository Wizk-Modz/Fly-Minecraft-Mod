package com.wizkfly;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WizkFlyMod implements ModInitializer {
	public static final String MOD_ID = "wizk-fly-mod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static FlyConfig config;

	@Override
	public void onInitialize() {
		config = FlyConfig.load();
		LOGGER.info("Wizk Fly Mod initialized with permission mode '{}'", config.permission().name().toLowerCase());

		PayloadTypeRegistry.serverboundPlay().register(FlyTogglePayload.TYPE, FlyTogglePayload.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(FlyTogglePayload.TYPE, (payload, context) -> handleToggleRequest(context.player()));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> FlyCommand.register(dispatcher));

		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> FlyManager.get().onPlayerLeave(handler.player));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> FlyManager.get().applyState(newPlayer));
		ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> FlyManager.get().applyState(player));
		ServerTickEvents.END_SERVER_TICK.register(FlyManager.get()::onServerTick);
	}

	// Trả về cấu hình đang dùng
	public static FlyConfig config() {
		return config;
	}

	// Xử lý yêu cầu bật/tắt Fly từ keybind, server tự kiểm tra quyền
	private static void handleToggleRequest(ServerPlayer player) {
		if (config.permission() == FlyConfig.Permission.OWNER && !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
			player.sendSystemMessage(Component.translatable("wizk-fly-mod.error.no_permission"));
			return;
		}
		FlyManager.get().toggle(player);
	}
}
