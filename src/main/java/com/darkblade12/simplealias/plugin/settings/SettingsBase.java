package com.darkblade12.simplealias.plugin.settings;

import com.darkblade12.simplealias.plugin.PluginBase;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class SettingsBase<T extends PluginBase> {
    protected T plugin;
    protected FileConfiguration config;

    protected SettingsBase(T plugin) {
        this.plugin = plugin;
    }

    public abstract void load() throws InvalidValueException;

    public abstract void unload();

    public void reload() throws InvalidValueException {
        unload();
        load();
    }
}
