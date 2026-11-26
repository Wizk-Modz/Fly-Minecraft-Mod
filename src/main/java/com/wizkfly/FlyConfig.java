package com.wizkfly;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;

// Đọc và ghi cấu hình wizk-fly-mod.toml
public final class FlyConfig {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("wizk-fly-mod.toml");

    private String toggleFlyKey = "key.keyboard.g";
    private boolean persistState = true;
    private boolean safeFall = true;
    private int safeFallTimeoutSeconds = 10;
    private boolean autoFlightOnToggle = true;
    private float defaultFlySpeed = 0.05f;
    private String messageDisplay = "actionbar";

    public String toggleFlyKey() {
        return toggleFlyKey;
    }

    public boolean persistState() {
        return persistState;
    }

    public boolean safeFall() {
        return safeFall;
    }

    public int safeFallTimeoutSeconds() {
        return safeFallTimeoutSeconds;
    }

    public boolean autoFlightOnToggle() {
        return autoFlightOnToggle;
    }

    public float defaultFlySpeed() {
        return defaultFlySpeed;
    }

    public String messageDisplay() {
        return messageDisplay;
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
                    case "toggle_fly_key" -> {
                        if (!value.isEmpty()) {
                            config.toggleFlyKey = value;
                        }
                    }
                    case "persist_state" -> config.persistState = Boolean.parseBoolean(value);
                    case "safe_fall" -> config.safeFall = Boolean.parseBoolean(value);
                    case "safe_fall_timeout_seconds" -> {
                        try {
                            config.safeFallTimeoutSeconds = Math.max(1, Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            warnings.add(key);
                        }
                    }
                    case "auto_flight_on_toggle" -> config.autoFlightOnToggle = Boolean.parseBoolean(value);
                    case "default_fly_speed" -> {
                        try {
                            config.defaultFlySpeed = Math.clamp(Float.parseFloat(value), 0.01f, 0.5f);
                        } catch (NumberFormatException e) {
                            warnings.add(key);
                        }
                    }
                    case "message_display" -> {
                        String lower = value.toLowerCase();
                        if (lower.equals("actionbar") || lower.equals("chat") || lower.equals("both") || lower.equals("none")) {
                            config.messageDisplay = lower;
                        } else {
                            warnings.add(key);
                        }
                    }
                    default -> {
                    }
                }
            }
        } catch (IOException e) {
            WizkFlyMod.LOGGER.error("Không thể đọc cấu hình wizk-fly-mod.toml", e);
            return config;
        }

        for (String key : warnings) {
            WizkFlyMod.LOGGER.warn("Giá trị không hợp lệ cho '{}' trong wizk-fly-mod.toml, dùng mặc định", key);
        }
        return config;
    }

    // Ghi cấu hình hiện tại ra tập tin
    public void save() {
        String content = """
            # Wizk Fly Mod Configuration
            # Phím tắt bật/tắt bay phía client (GLFW key name)
            toggle_fly_key = "%s"

            # Lưu trạng thái bay khi người chơi thoát hoặc server khởi động lại (true/false)
            persist_state = %s

            # Bảo vệ chống sát thương rơi khi tắt bay giữa không trung (true/false)
            safe_fall = %s

            # Thời gian tối đa bảo vệ rơi an toàn tính bằng giây
            safe_fall_timeout_seconds = %d

            # Tự động bay ngay khi bật lúc đang ở trên không (true/false)
            auto_flight_on_toggle = %s

            # Tốc độ bay mặc định (từ 0.01 đến 0.5, mặc định vanilla là 0.05)
            default_fly_speed = %.2f

            # Nơi hiển thị thông báo bật/tắt bay ("actionbar", "chat", "both", "none")
            message_display = "%s"
            """.formatted(
                toggleFlyKey,
                persistState,
                safeFall,
                safeFallTimeoutSeconds,
                autoFlightOnToggle,
                defaultFlySpeed,
                messageDisplay
            );

        try {
            if (PATH.getParent() != null) {
                Files.createDirectories(PATH.getParent());
            }
            Files.writeString(PATH, content);
        } catch (IOException e) {
            WizkFlyMod.LOGGER.error("Không thể ghi cấu hình wizk-fly-mod.toml", e);
        }
    }

    // Bỏ dấu nháy kép bao quanh chuỗi giá trị nếu có
    private static String unquote(String value) {
        if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
