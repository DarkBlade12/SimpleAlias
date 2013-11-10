package com.darkblade12.simplealias;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.darkblade12.simplealias.alias.AliasHandler;
import com.darkblade12.simplealias.commands.CommandHandler;
import com.darkblade12.simplealias.cooldown.CooldownHandler;
import com.darkblade12.simplealias.hook.FactionsHook;
import com.darkblade12.simplealias.hook.VaultHook;

public class SimpleAlias extends JavaPlugin {
	public static String PREFIX = "§f§l[§a§oSimple§7§oAlias§f§l]§r ";
	public static String MASTER_PERMISSION = "SimpleAlias.*";
	public static String ALIAS_MASTER_PERMISSION = "SimpleAlias.alias.*";
	public static String COOLDOWN_BYPASS_PERMISSION = "SimpleAlias.bypass.cooldown";
	public Logger l;
	public AliasHandler aliasHandler;
	public CooldownHandler cooldownHandler;
	public CommandHandler commandHandler;
	public VaultHook vaultHook;
	public FactionsHook factionsHook;

	@Override
	public void onEnable() {
		l = getLogger();
		aliasHandler = new AliasHandler(this);
		cooldownHandler = new CooldownHandler(this);
		commandHandler = new CommandHandler(this);
		vaultHook = new VaultHook();
		if (vaultHook.load())
			l.info("Vault hooked! (Permission installed: " + (VaultHook.permission != null) + ", Economy installed: " + (VaultHook.economy != null) + ")");
		factionsHook = new FactionsHook();
		if (factionsHook.load())
			l.info("Factions hooked!");
		l.info("Alias system is activated!");
	}

	@Override
	public void onDisable() {
		cooldownHandler.saveLists();
		l.info("Alias system is deactivated!");
	}

	public void reload() {
		aliasHandler.registerAliases();
		cooldownHandler.saveLists();
		vaultHook.load();
		factionsHook.load();
	}
}