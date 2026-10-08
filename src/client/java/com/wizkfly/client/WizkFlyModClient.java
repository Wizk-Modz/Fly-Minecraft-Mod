package com.wizkfly.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import com.wizkfly.FlyConfig;
import com.wizkfly.FlyTogglePayload;
import com.wizkfly.WizkFlyMod;

public class WizkFlyModClient implements ClientModInitializer {
	private static final KeyMapping.Category CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath(WizkFlyMod.MOD_ID, "main"));

	private KeyMapping toggleFlyKey;

	@Override
	public void onInitializeClient() {
		FlyConfig config = FlyConfig.load();
		toggleFlyKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.wizk-fly-mod.toggle_fly",
			InputConstants.Type.KEYSYM,
			resolveKeyCode(config.toggleFlyKey()),
			CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
	}

	// Gửi yêu cầu bật/tắt Fly lên server khi người chơi nhấn keybind
	private void onEndTick(Minecraft client) {
		while (toggleFlyKey.consumeClick()) {
			if (client.player != null && ClientPlayNetworking.canSend(FlyTogglePayload.TYPE)) {
				ClientPlayNetworking.send(FlyTogglePayload.INSTANCE);
			}
		}
	}

	// Chuyển tên phím trong cấu hình thành mã phím, dùng phím chưa gán nếu không hợp lệ
	private static int resolveKeyCode(String keyName) {
		try {
			return InputConstants.getKey(keyName).getValue();
		} catch (IllegalArgumentException e) {
			WizkFlyMod.LOGGER.warn("Invalid toggle_fly_key '{}', leaving key unbound", keyName);
			return InputConstants.UNKNOWN.getValue();
		}
	}
}
