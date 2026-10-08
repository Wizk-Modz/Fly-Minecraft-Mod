package com.wizkfly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;

// Quản lý trạng thái Fly riêng của từng player
public final class FlyManager {
    private static final FlyManager INSTANCE = new FlyManager();

    private final Set<UUID> flyEnabled = new HashSet<>();
    private final Map<UUID, GameType> lastGameMode = new HashMap<>();

    // Khởi tạo singleton, chỉ dùng nội bộ
    private FlyManager() {
    }

    // Trả về thực thể quản lý dùng chung
    public static FlyManager get() {
        return INSTANCE;
    }

    // Kiểm tra player đang bật Fly của mod
    public boolean isEnabled(ServerPlayer player) {
        return flyEnabled.contains(player.getUUID());
    }

    // Bật/tắt Fly cho player, trả về trạng thái mới
    public boolean toggle(ServerPlayer player) {
        if (isEnabled(player)) {
            flyEnabled.remove(player.getUUID());
            applyState(player);
            player.sendSystemMessage(Component.translatable("wizk-fly-mod.message.fly.disabled"));
            return false;
        }
        flyEnabled.add(player.getUUID());
        applyState(player);
        player.sendSystemMessage(Component.translatable("wizk-fly-mod.message.fly.enabled"));
        return true;
    }

    // Đồng bộ trạng thái Fly của player với dữ liệu đã lưu
    public void applyState(ServerPlayer player) {
        applyState(player, false);
    }

    // Đồng bộ trạng thái Fly của player, tắt bay ngay nếu được yêu cầu
    public void applyState(ServerPlayer player, boolean stopFlying) {
        GameType gameType = player.gameMode();
        if (gameType == GameType.CREATIVE || gameType == GameType.SPECTATOR) {
            return;
        }
        Abilities abilities = player.getAbilities();
        boolean enabled = isEnabled(player);
        abilities.mayfly = enabled;
        if (!enabled || stopFlying) {
            abilities.flying = false;
        }
        player.onUpdateAbilities();
    }

    // Xóa trạng thái Fly đã lưu của player khi rời server
    public void onPlayerLeave(ServerPlayer player) {
        UUID uuid = player.getUUID();
        flyEnabled.remove(uuid);
        lastGameMode.remove(uuid);
    }

    // Đồng bộ lại Fly cho mọi player khi chế độ chơi thay đổi
    public void onServerTick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            GameType current = player.gameMode();
            GameType previous = lastGameMode.put(player.getUUID(), current);
            if (previous == current) {
                continue;
            }
            if (current != GameType.CREATIVE && current != GameType.SPECTATOR) {
                // Vào lại chế độ chơi thường: giữ quyền bay đã cấp nhưng tắt bay ngay
                applyState(player, true);
            }
        }
    }
}
