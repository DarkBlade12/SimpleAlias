package com.darkblade12.simplealias.hook;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VaultHook extends Hook<Vault> {
	public static Economy economy;
	public static Permission permission;

	@Override
	protected boolean initiate() {
		economy = Bukkit.getServicesManager().getRegistration(Economy.class) != null ? Bukkit.getServicesManager().getRegistration(Economy.class).getProvider() : null;
		permission = Bukkit.getServicesManager().getRegistration(Permission.class) != null ? Bukkit.getServicesManager().getRegistration(Permission.class).getProvider() : null;
		return economy != null || permission != null;
	}

	public static double getBalance(Player p) {
		return economy == null ? 0 : economy.getBalance(p.getName());
	}

	public static String getGroup(Player p) {
		try {
			return permission == null ? "Default" : permission.getPrimaryGroup(p);
		} catch (Exception e) {
			return "Default";
		}
	}

	@Override
	public String getPluginName() {
		return "Vault";
	}
}