package com.wizkfly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;

// Đọc và ghi cấu hình wizk-fly-mod.toml
public final class FlyConfig {
    // Chế độ quyền sử dụng mod
    public enum Permission {
        ALL,
        OWNER;

        // Chuyển chuỗi cấu hình sang enum, trả về null nếu không hợp lệ
        static Permission from(String value) {
            if (value == null) {
                return null;
            }
            return switch (value.trim().toLowerCase()) {
                case "all" -> ALL;
                case "owner" -> OWNER;
                default -> null;
            };
        }
    }

    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("wizk-fly-mod.toml");

    private Permission permission = Permission.ALL;
    private String toggleFlyKey = "key.keyboard.g";

    // Trả về chế độ quyền đang dùng
    public Permission permission() {
        return permission;
    }

    // Trả về tên phím bật/tắt Fly
    public String toggleFlyKey() {
        return toggleFlyKey;
    }

    // Đọc cấu hình từ đĩa, tạo file mặc định nếu chưa tồn tại
    public static FlyConfig load() {
        FlyConfig config = new FlyConfig();
        if (!Files.exists(PATH)) {
            config.save();
            return config;
        }
        List<String> warnings = new ArrayList<>();
        try {
            for (String raw : Files.readAllLines(PATH)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int separator = line.indexOf('=');
                if (separator < 0) {
                    continue;
                }
                String key = line.substring(0, separator).trim();
                String value = unquote(line.substring(separator + 1).trim());
                switch (key) {
                    case "permission" -> {
                        Permission parsed = Permission.from(value);
                        if (parsed == null) {
                            warnings.add("permission");
                        } else {
                            config.permission = parsed;
                        }
                    }
                    case "toggle_fly_key" -> {
                        if (value.isEmpty()) {
                            warnings.add("toggle_fly_key");
                        } else {
                            config.toggleFlyKey = value;
                        }
                    }
                    default -> {
                    }
                }
            }
        } catch (IOException e) {
            WizkFlyMod.LOGGER.error("Failed to read wizk-fly-mod.toml, using defaults", e);
            return config;
        }
        for (String key : warnings) {
            WizkFlyMod.LOGGER.warn("Invalid value for '{}' in wizk-fly-mod.toml, using default", key);
        }
        return config;
    }

    // Ghi cấu hình hiện tại ra file
    public void save() {
        String content = """
            # Wizk Fly Mod configuration
            # permission: "all" cho mọi player, "owner" chỉ dành cho quản trị viên
            permission = "%s"
            # toggle_fly_key: tên phím GLFW, ví dụ "key.keyboard.g"
            toggle_fly_key = "%s"
            """.formatted(permission == Permission.OWNER ? "owner" : "all", toggleFlyKey);
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, content);
        } catch (IOException e) {
            WizkFlyMod.LOGGER.error("Failed to write wizk-fly-mod.toml", e);
        }
    }

    // Bỏ dấu nháy kép bao quanh giá trị nếu có
    private static String unquote(String value) {
        if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
