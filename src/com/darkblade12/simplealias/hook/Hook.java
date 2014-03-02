package com.darkblade12.simplealias.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unchecked")
public abstract class Hook<P extends JavaPlugin> {
	protected P plugin;
	protected boolean enabled;

	public boolean onLoad() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(getPluginName());
		if (plugin != null && plugin.isEnabled()) {
			this.plugin = (P) plugin;
			return (enabled = onEnable());
		}
		return false;
	}

	protected abstract boolean onEnable();

	public abstract String getPluginName();

	public final P getPlugin() {
		return this.plugin;
	}

	public final boolean isEnabled() {
		return enabled;
	}
}