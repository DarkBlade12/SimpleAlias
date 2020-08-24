package com.darkblade12.simplealias.plugin.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class Hook {
    protected String pluginName;
    protected boolean enabled;

    protected Hook(String pluginName) {
        this.pluginName = pluginName;
    }

    public boolean enable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(getPluginName());
        if (plugin == null || !plugin.isEnabled()) {
            return false;
        }

        return enabled = initialize();
    }

    protected abstract boolean initialize();

    public String getPluginName() {
        return pluginName;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
