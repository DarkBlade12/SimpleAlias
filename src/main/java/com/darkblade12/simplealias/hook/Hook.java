package com.darkblade12.simplealias.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class Hook {
	protected boolean enabled;

	public boolean onLoad() {
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(getPluginName());
		if (plugin != null && plugin.isEnabled()) {
			return (enabled = onEnable());
		}
		return false;
	}

	protected abstract boolean onEnable();

	public abstract String getPluginName();

	public final boolean isEnabled() {
		return enabled;
	}
}