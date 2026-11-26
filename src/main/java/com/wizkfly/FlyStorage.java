package com.wizkfly;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

// Quản lý lưu trữ trạng thái bay của người chơi ra tập tin đĩa
public final class FlyStorage {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("wizk-fly-players.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Lớp tiện ích, không cho khởi tạo trực tiếp
    private FlyStorage() {
    }

    // Đọc toàn bộ danh sách trạng thái người chơi đã lưu từ tập tin
    public static Map<UUID, PlayerFlyState> load() {
        Map<UUID, PlayerFlyState> result = new HashMap<>();
        if (!Files.exists(PATH)) {
            return result;
        }

        try (Reader reader = Files.newBufferedReader(PATH)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                try {
                    UUID uuid = UUID.fromString(entry.getKey());
                    JsonObject obj = entry.getValue().getAsJsonObject();
                    boolean enabled = obj.has("enabled") && obj.get("enabled").getAsBoolean();
                    float speed = obj.has("speed") ? obj.get("speed").getAsFloat() : 0.05f;
                    result.put(uuid, new PlayerFlyState(uuid, enabled, speed));
                } catch (IllegalArgumentException e) {
                    WizkFlyMod.LOGGER.warn("Bỏ qua UUID không hợp lệ trong wizk-fly-players.json: {}", entry.getKey());
                }
            }
        } catch (Exception e) {
            WizkFlyMod.LOGGER.error("Không thể đọc tập tin wizk-fly-players.json", e);
        }

        return result;
    }

    // Ghi danh sách trạng thái người chơi hiện tại vào tập tin đĩa
    public static void save(Map<UUID, PlayerFlyState> states) {
        JsonObject root = new JsonObject();
        for (PlayerFlyState state : states.values()) {
            if (!state.isEnabled() && Math.abs(state.getSpeed() - 0.05f) < 0.0001f) {
                continue;
            }
            JsonObject obj = new JsonObject();
            obj.addProperty("enabled", state.isEnabled());
            obj.addProperty("speed", state.getSpeed());
            root.add(state.getUuid().toString(), obj);
        }

        try {
            if (PATH.getParent() != null) {
                Files.createDirectories(PATH.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            WizkFlyMod.LOGGER.error("Không thể ghi tập tin wizk-fly-players.json", e);
        }
    }
}
