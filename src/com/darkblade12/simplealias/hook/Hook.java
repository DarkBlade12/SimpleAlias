package com.darkblade12.simplealias.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Hook<P extends JavaPlugin> {
	protected P plugin;
	protected static boolean enabled;

	@SuppressWarnings("unchecked")
	public boolean load() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(getPluginName());
		if (plugin == null) {
			enabled = false;
			return false;
		}
		this.plugin = (P) plugin;
		enabled = initiate();
		return enabled;
	}

	protected boolean initiate() {
		return true;
	}

	public abstract String getPluginName();

	public P getPlugin() {
		return this.plugin;
	}

	public static boolean isEnabled() {
		return enabled;
	}
}