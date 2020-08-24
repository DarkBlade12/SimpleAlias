package com.darkblade12.simplealias.cooldown;

import com.darkblade12.simplealias.util.MessageUtils;

public final class Cooldown {
    private long expiredTime;

    public Cooldown(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public Cooldown(int duration) {
        this(System.currentTimeMillis() + duration * 1000L);
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public boolean isExpired() {
        return expiredTime >= 0 && System.currentTimeMillis() > expiredTime;
    }

    public long getRemainingDuration() {
        if (expiredTime < 0) {
            return -1;
        }

        long currentTime = System.currentTimeMillis();
        return isExpired() || currentTime >= expiredTime ? 0 : expiredTime - currentTime;
    }

    @Override
    public String toString() {
        long duration = getRemainingDuration();
        return duration == -1 ? "forever" : MessageUtils.formatDuration(duration);
    }
}
