package com.wizkfly;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;

// Quản lý toàn diện trạng thái bay, an toàn rơi và đồng bộ dữ liệu người chơi
public final class FlyManager {
    private static final FlyManager INSTANCE = new FlyManager();

    private final Map<UUID, PlayerFlyState> playerStates = new HashMap<>();
    private final Map<UUID, GameType> lastGameMode = new HashMap<>();

    // Khởi tạo singleton nội bộ
    private FlyManager() {
    }

    public static FlyManager get() {
        return INSTANCE;
    }

    // Nạp dữ liệu trạng thái đã lưu khi khởi động mod
    public void init() {
        if (WizkFlyMod.config().persistState()) {
            playerStates.putAll(FlyStorage.load());
        }
    }

    // Lưu toàn bộ dữ liệu trạng thái hiện tại ra tập tin
    public void save() {
        if (WizkFlyMod.config().persistState()) {
            FlyStorage.save(playerStates);
        }
    }

    // Lấy hoặc tạo mới trạng thái bay của một người chơi
    public PlayerFlyState getState(ServerPlayer player) {
        return playerStates.computeIfAbsent(player.getUUID(), uuid ->
            new PlayerFlyState(uuid, false, WizkFlyMod.config().defaultFlySpeed()));
    }

    // Kiểm tra người chơi có đang được bật quyền bay hay không
    public boolean isEnabled(ServerPlayer player) {
        return getState(player).isEnabled();
    }

    // Bật hoặc tắt trạng thái bay cho người chơi
    public boolean toggle(ServerPlayer player) {
        PlayerFlyState state = getState(player);
        boolean newEnabled = !state.isEnabled();
        state.setEnabled(newEnabled);

        if (newEnabled) {
            state.clearSafeFall();
            if (WizkFlyMod.config().autoFlightOnToggle() && !player.onGround()) {
                player.getAbilities().flying = true;
            }
            applyState(player, false);
            sendMessage(player, Component.translatable("wizk-fly-mod.message.fly.enabled").withStyle(ChatFormatting.GREEN));
        } else {
            if (WizkFlyMod.config().safeFall() && !player.onGround()) {
                state.activateSafeFall(WizkFlyMod.config().safeFallTimeoutSeconds() * 20);
            }
            applyState(player, true);
            sendMessage(player, Component.translatable("wizk-fly-mod.message.fly.disabled").withStyle(ChatFormatting.RED));
        }

        syncToClient(player);
        save();
        return newEnabled;
    }

    // Đặt tốc độ bay mới cho người chơi
    public void setSpeed(ServerPlayer player, float speed) {
        PlayerFlyState state = getState(player);
        state.setSpeed(speed);
        applyState(player, false);
        syncToClient(player);
        save();
    }

    // Đồng bộ thuộc tính bay của player với dữ liệu quản lý
    public void applyState(ServerPlayer player) {
        applyState(player, false);
    }

    // Áp dụng trạng thái bay vào Abilities của player, tắt bay nếu được yêu cầu
    public void applyState(ServerPlayer player, boolean stopFlying) {
        GameType gameType = player.gameMode();
        if (gameType == GameType.CREATIVE || gameType == GameType.SPECTATOR) {
            return;
        }

        Abilities abilities = player.getAbilities();
        PlayerFlyState state = getState(player);
        boolean enabled = state.isEnabled();

        abilities.mayfly = enabled;
        abilities.setFlyingSpeed(state.getSpeed());

        if (!enabled || stopFlying) {
            abilities.flying = false;
        }

        player.onUpdateAbilities();
    }

    // Gửi gói tin cập nhật trạng thái bay xuống phía client
    public void syncToClient(ServerPlayer player) {
        if (ServerPlayNetworking.canSend(player, FlySyncPayload.TYPE)) {
            PlayerFlyState state = getState(player);
            ServerPlayNetworking.send(player, new FlySyncPayload(
                state.isEnabled(),
                player.getAbilities().flying,
                state.getSpeed()
            ));
        }
    }

    // Gửi thông báo đến người chơi theo cấu hình hiển thị đã chọn
    public void sendMessage(ServerPlayer player, Component component) {
        String mode = WizkFlyMod.config().messageDisplay();
        switch (mode) {
            case "actionbar" -> player.sendOverlayMessage(component);
            case "chat" -> player.sendSystemMessage(component);
            case "both" -> {
                player.sendOverlayMessage(component);
                player.sendSystemMessage(component);
            }
            default -> {
            }
        }
    }

    // Xử lý khôi phục quyền bay khi người chơi vừa đăng nhập vào server
    public void onPlayerJoin(ServerPlayer player) {
        if (WizkFlyMod.config().persistState()) {
            PlayerFlyState state = playerStates.get(player.getUUID());
            if (state != null && state.isEnabled()) {
                applyState(player, false);
                syncToClient(player);
            }
        }
    }

    // Xử lý dọn dẹp khi người chơi rời khỏi server
    public void onPlayerLeave(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!WizkFlyMod.config().persistState()) {
            playerStates.remove(uuid);
        }
        lastGameMode.remove(uuid);
    }

    // Kiểm tra định kỳ chế độ chơi và đếm lùi thời gian bảo vệ rơi an toàn
    public void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            GameType current = player.gameMode();
            GameType previous = lastGameMode.put(player.getUUID(), current);

            if (previous != null && previous != current && current != GameType.CREATIVE && current != GameType.SPECTATOR) {
                applyState(player, true);
                syncToClient(player);
            }

            PlayerFlyState state = playerStates.get(player.getUUID());
            if (state != null && state.isSafeFallActive()) {
                player.resetFallDistance();

                if (player.onGround()) {
                    state.clearSafeFall();
                    player.sendOverlayMessage(Component.translatable("wizk-fly-mod.message.fly.safe_fall")
                        .withStyle(ChatFormatting.AQUA));
                } else if (!state.tickSafeFall()) {
                    state.clearSafeFall();
                }
            }
        }
    }
}
