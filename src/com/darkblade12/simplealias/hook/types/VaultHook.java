package com.darkblade12.simplealias.hook.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.darkblade12.simplealias.hook.Hook;

public final class VaultHook extends Hook<Vault> {
	private Economy economy;
	private Permission permission;

	@Override
	protected boolean onEnable() {
		economy = Bukkit.getServicesManager().getRegistration(Economy.class) != null ? Bukkit.getServicesManager().getRegistration(Economy.class).getProvider() : null;
		permission = Bukkit.getServicesManager().getRegistration(Permission.class) != null ? Bukkit.getServicesManager().getRegistration(Permission.class).getProvider() : null;
		return isEconomyEnabled() || isPermissionEnabled();
	}

	public Economy getEconomy() {
		return economy;
	}

	public boolean isEconomyEnabled() {
		return economy != null;
	}

	public double getBalance(Player p) {
		return isEconomyEnabled() ? economy.getBalance(p) : 0;
	}

	public boolean withdrawMoney(Player p, double amount) {
		if (amount <= 0)
			throw new IllegalArgumentException("Amount value has to be higher than 0");
		if (getBalance(p) < amount) {
			return false;
		} else {
			economy.withdrawPlayer(p, amount);
			return true;
		}
	}

	public String getCurrencyName(double amount) {
		return isEconomyEnabled() ? amount == 1 ? economy.currencyNameSingular() : economy.currencyNamePlural() : "";
	}

	public Permission getPermission() {
		return permission;
	}

	public boolean isPermissionEnabled() {
		return permission != null;
	}

	public boolean hasPermissionGroupSupport() {
		return isPermissionEnabled() && permission.hasGroupSupport();
	}

	public boolean hasGroup(String name) {
		if (!hasPermissionGroupSupport())
			return false;
		for (String group : permission.getGroups())
			if (group.equalsIgnoreCase(name))
				return true;
		return false;
	}

	public String getExactGroupName(String name) {
		if (!hasPermissionGroupSupport())
			return null;
		for (String group : permission.getGroups())
			if (group.equalsIgnoreCase(name))
				return group;
		return null;
	}

	public String getPrimaryGroup(Player p) {
		String group = hasPermissionGroupSupport() ? permission.getPrimaryGroup(p) : null;
		return group == null ? "N/A" : null;
	}

	public Set<String> getGroups(Player p) {
		Set<String> groups = new HashSet<String>();
		if (hasPermissionGroupSupport()) {
			String[] groupArray = permission.getPlayerGroups(p);
			if (groupArray != null)
				for (String group : groupArray)
					groups.add(group);
		}
		return groups;
	}

	public boolean isInGroup(Player p, String name) {
		return hasPermissionGroupSupport() ? permission.playerInGroup(p, name) : false;
	}

	public boolean isInAnyGroup(Player p, Collection<String> names) {
		if (hasPermissionGroupSupport())
			for (String name : names)
				if (permission.playerInGroup(p, name))
					return true;
		return false;
	}

	public boolean isInAllGroups(Player p, Collection<String> names) {
		if (!hasPermissionGroupSupport()) {
			return false;
		} else {
			for (String name : names)
				if (!permission.playerInGroup(p, name))
					return false;
			return true;
		}
	}

	@Override
	public String getPluginName() {
		return "Vault";
	}
}