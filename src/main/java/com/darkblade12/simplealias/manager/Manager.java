package com.darkblade12.simplealias.manager;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.darkblade12.simplealias.SimpleAlias;

public abstract class Manager implements Listener {
	public abstract boolean onEnable();

	public abstract void onDisable();

	public boolean onReload() {
		onDisable();
		return onEnable();
	}

	protected final void registerEvents() {
		SimpleAlias instance = SimpleAlias.instance();
		instance.getServer().getPluginManager().registerEvents(this, instance);
	}

	protected final void unregisterAll() {
		HandlerList.unregisterAll(this);
	}
}