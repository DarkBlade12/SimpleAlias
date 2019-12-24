package com.darkblade12.simplealias.cooldown;

import java.io.Serializable;

import com.darkblade12.simplealias.nameable.Nameable;
import com.darkblade12.simplealias.util.TimeUnit;

public final class Cooldown implements Nameable, Serializable {
	private static final long serialVersionUID = 3105378695539732729L;
	private String name;
	private long expiredTime;

	public Cooldown(String name, long expiredTime) {
		this.name = name;
		this.expiredTime = expiredTime;
	}

	public static Cooldown fromDuration(String name, long duration) {
		return new Cooldown(name, System.currentTimeMillis() + (duration * 1000));
	}

	@Override
	public String getName() {
		return name;
	}

	public long getExpiredTime() {
		return this.expiredTime;
	}

	public long getRemainingTime() {
		return expiredTime >= 0 ? isExpired() ? 0 : expiredTime - System.currentTimeMillis() : -1;
	}
	
	public String getRemainingTimeString() {
		long remaining = getRemainingTime();
		return remaining == -1 ? "forever" : TimeUnit.convertToString(remaining);
	}

	public boolean isExpired() {
		return expiredTime >= 0 ? System.currentTimeMillis() > expiredTime : false;
	}
}