package com.darkblade12.simplealias;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.command.alias.AliasCommandHandler;
import com.darkblade12.simplealias.cooldown.CooldownManager;
import com.darkblade12.simplealias.hook.types.FactionsHook;
import com.darkblade12.simplealias.hook.types.VaultHook;
import com.darkblade12.simplealias.reader.types.ConfigurationTemplateReader;
import org.bstats.bukkit.Metrics;

public final class SimpleAlias extends JavaPlugin {
	public static final String PREFIX = "§8§l[§a§oSimple§7§oAlias§8§l]§r ";
	private static SimpleAlias instance;
	private Logger logger;
	private ConfigurationTemplateReader templateReader;
	private VaultHook vaultHook;
	private FactionsHook factionsHook;
	private CooldownManager cooldownManager;
	private AliasManager aliasManager;

	public SimpleAlias() {
		logger = getLogger();
		vaultHook = new VaultHook();
		factionsHook = new FactionsHook();
		cooldownManager = new CooldownManager();
		aliasManager = new AliasManager();
	}

	@Override
	public void onEnable() {
		instance = this;
		long start = System.currentTimeMillis();
		try {
			Settings.load();
		} catch (Exception e) {
			logger.warning("Failed to load the settings from the config.yml, plugin will disable! Cause: " + e.getMessage());
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		initializeTemplateReader();
		if (!templateReader.readConfigurationTemplate()) {
			logger.warning("Failed to read template.yml, plugin will disable!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		if (vaultHook.onLoad())
			logger.info("Vault hooked! (Permission: " + vaultHook.isPermissionEnabled() + ", Economy: " + vaultHook.isEconomyEnabled() + ")");
		if (factionsHook.onLoad())
			logger.info("Factions hooked!");
		cooldownManager.onEnable();
		aliasManager.onEnable();
		if(Settings.isConverterEnabled()) {
			try {
				Converter.convertAliases();
			} catch (Exception e) {
				logger.info("Failed to convert aliases! Cause: " + e.getMessage());
				if(Settings.isDebugEnabled()) {
					e.printStackTrace();
				}
			}
		}
		new AliasCommandHandler();
		enableMetrics();
		logger.info("Alias system is activated! (" + (System.currentTimeMillis() - start) + " ms)");
	}

	@Override
	public void onDisable() {
		logger.info("Alias system is deactivated!");
	}

	public void onReload() {
		try {
			Settings.reload();
		} catch (Exception e) {
			logger.warning("Failed to load the settings from the config.yml, plugin will disable! Cause: " + e.getMessage());
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		initializeTemplateReader();
		if (!templateReader.readConfigurationTemplate()) {
			logger.warning("Failed to read template.yml, plugin will disable!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		vaultHook.onLoad();
		factionsHook.onLoad();
		cooldownManager.onReload();
		aliasManager.onReload();
		if(Settings.isConverterEnabled()) {
			try {
				Converter.convertAliases();
			} catch (Exception e) {
				logger.info("Failed to convert aliases! Cause: " + e.getMessage());
				if(Settings.isDebugEnabled()) {
					e.printStackTrace();
				}
			}
		}
	}

	public Configuration loadConfig() {
		if (!new File("plugins/" + getName() + "/config.yml").exists())
			saveDefaultConfig();
		logger.info("config.yml successfully loaded.");
		return getConfig();
	}

	private void initializeTemplateReader() {
		templateReader = new ConfigurationTemplateReader(Settings.getUncommentedTemplate() ? "template_uncommented.yml" : "template.yml", "template.yml", "plugins/SimpleAlias/");
	}

	private void enableMetrics() {
		try {
			Metrics m = new Metrics(this, 8539);
			if (!m.isEnabled()) {
				logger.warning("Metrics are disabled!");
			} else {
				logger.info("This plugin is using Metrics by BtoBastian!");
			}
		} catch (Exception e) {
			logger.info("An error occured while enabling Metrics!");
			if(Settings.isDebugEnabled()) {
				e.printStackTrace();
			}
		}
	}

	public static SimpleAlias instance() {
		if (instance == null || !instance.isEnabled())
			throw new UnsupportedOperationException("There is no enabled instance of SimpleAlias");
		return instance;
	}

	public static Logger logger() {
		return instance().logger;
	}

	public static ConfigurationTemplateReader getTemplateReader() {
		return instance().templateReader;
	}

	public static VaultHook getVaultHook() {
		return instance().vaultHook;
	}

	public static FactionsHook getFactionsHook() {
		return instance().factionsHook;
	}

	public static CooldownManager getCooldownManager() {
		return instance().cooldownManager;
	}

	public static AliasManager getAliasManager() {
		return instance().aliasManager;
	}
}