package com.wizkfly;

import java.util.UUID;

// Trạng thái bay và an toàn của một người chơi
public final class PlayerFlyState {
    private final UUID uuid;
    private boolean enabled;
    private float speed = 0.05f;
    private boolean safeFallActive;
    private int safeFallTicksRemaining;

    // Khởi tạo trạng thái mới cho người chơi
    public PlayerFlyState(UUID uuid) {
        this.uuid = uuid;
    }

    // Khởi tạo trạng thái với các giá trị cụ thể
    public PlayerFlyState(UUID uuid, boolean enabled, float speed) {
        this.uuid = uuid;
        this.enabled = enabled;
        this.speed = speed;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = Math.clamp(speed, 0.01f, 0.5f);
    }

    public boolean isSafeFallActive() {
        return safeFallActive;
    }

    // Bắt đầu kích hoạt bảo vệ chống sát thương rơi
    public void activateSafeFall(int ticks) {
        this.safeFallActive = true;
        this.safeFallTicksRemaining = ticks;
    }

    // Tắt trạng thái bảo vệ rơi an toàn
    public void clearSafeFall() {
        this.safeFallActive = false;
        this.safeFallTicksRemaining = 0;
    }

    // Giảm thời gian đếm ngược bảo vệ rơi sau mỗi tick
    public boolean tickSafeFall() {
        if (!safeFallActive) {
            return false;
        }
        if (safeFallTicksRemaining > 0) {
            safeFallTicksRemaining--;
        }
        if (safeFallTicksRemaining <= 0) {
            safeFallActive = false;
            return false;
        }
        return true;
    }
}
