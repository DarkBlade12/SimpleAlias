package com.darkblade12.simplealias.loader;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigLoader extends FileLoader {
	private YamlConfiguration config;

	public ConfigLoader(Plugin plugin, String configName) {
		super(plugin, configName, "plugins/" + plugin.getName() + "/");
	}

	public ConfigLoader(Plugin plugin, String configName, boolean aliasConfig) {
		super(plugin, aliasConfig ? "alias.yml" : configName, "plugins/" + plugin.getName() + "/" + (aliasConfig ? "aliases/" : ""), configName);
	}

	public boolean loadConfig() {
		if (!super.loadFile())
			return false;
		config = YamlConfiguration.loadConfiguration(outputFile);
		return config != null;
	}

	public boolean saveDefaultConfig() {
		return super.saveResourceFile();
	}

	public boolean saveConfig(YamlConfiguration newConfig) {
		try {
			this.config = newConfig;
			newConfig.save(outputFile);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void deleteConfig() {
		super.deleteFile();
	}

	public YamlConfiguration getConfig() {
		return this.config;
	}
}