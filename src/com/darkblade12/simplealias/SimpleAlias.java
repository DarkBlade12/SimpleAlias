package com.darkblade12.simplealias;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.command.alias.AliasCommandHandler;
import com.darkblade12.simplealias.cooldown.CooldownManager;
import com.darkblade12.simplealias.hook.types.FactionsHook;
import com.darkblade12.simplealias.hook.types.VaultHook;
import com.darkblade12.simplealias.metrics.MetricsLite;
import com.darkblade12.simplealias.reader.types.ConfigurationTemplateReader;

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
		instance = this;
		logger = getLogger();
		templateReader = new ConfigurationTemplateReader("template.yml", "plugins/SimpleAlias/");
		vaultHook = new VaultHook();
		factionsHook = new FactionsHook();
		cooldownManager = new CooldownManager();
		aliasManager = new AliasManager();
	}

	@Override
	public void onEnable() {
		long start = System.currentTimeMillis();
		if (!templateReader.readConfigurationTemplate()) {
			logger.warning("Failed to read template.yml, plugin will disable!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		if (vaultHook.onLoad())
			logger.info("Vault hooked! (Permission installed: " + vaultHook.isPermissionEnabled() + ", Economy installed: " + vaultHook.isEconomyEnabled() + ")");
		if (factionsHook.onLoad())
			logger.info("Factions hooked!");
		cooldownManager.onEnable();
		aliasManager.onEnable();
		new AliasCommandHandler();
		enableMetrics();
		logger.info("Alias system is activated! (" + (System.currentTimeMillis() - start) + " ms)");
	}

	@Override
	public void onDisable() {
		logger.info("Alias system is deactivated!");
	}

	public void onReload() {
		vaultHook.onLoad();
		factionsHook.onLoad();
		cooldownManager.onReload();
		aliasManager.onReload();
	}

	private void enableMetrics() {
		try {
			MetricsLite m = new MetricsLite(this);
			if (m.isOptOut()) {
				logger.warning("Metrics are disabled!");
			} else {
				logger.info("This plugin is using Metrics by Hidendra!");
				m.start();
			}
		} catch (Exception e) {
			logger.info("An error occured while enabling Metrics!");
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