package com.darkblade12.simplealias.cooldown;

import com.darkblade12.simplealias.nameable.Nameable;

public class Cooldown implements Nameable {
	private String name;
	private long expiredTime;

	public Cooldown(String name, long expiredTime) {
		this.name = name;
		this.expiredTime = expiredTime;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public long getExpiredTime() {
		return this.expiredTime;
	}

	public long getWaitingTime() {
		return expiredTime >= 0 ? isExpired() ? 0 : expiredTime - System.currentTimeMillis() : -1;
	}

	public boolean isExpired() {
		return expiredTime >= 0 ? System.currentTimeMillis() > expiredTime : false;
	}

	@Override
	public String toString() {
		return name + "#" + expiredTime;
	}
}