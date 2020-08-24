package com.darkblade12.simplealias.plugin.hook;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;

public final class VaultHook extends Hook {
    public static final String DEFAULT_GROUP = "Default";
    private Economy economy;
    private Permission permission;

    public VaultHook() {
        super("Vault");
    }

    @Override
    protected boolean initialize() {
        RegisteredServiceProvider<Economy> economyRsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (economyRsp != null) {
            economy = economyRsp.getProvider();
        }

        RegisteredServiceProvider<Permission> permissionRsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (permissionRsp != null) {
            permission = permissionRsp.getProvider();
        }

        return economy != null || permission != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isEconomyEnabled() {
        return economy != null && economy.isEnabled();
    }

    public Permission getPermission() {
        return permission;
    }

    public boolean isPermissionEnabled() {
        return permission != null && permission.isEnabled();
    }

    public boolean hasPermissionGroupSupport() {
        return isPermissionEnabled() && permission.hasGroupSupport();
    }

    public double getBalance(OfflinePlayer player) {
        return !isEconomyEnabled() ? 0 : economy.getBalance(player);
    }

    public boolean withdrawPlayer(OfflinePlayer player, double amount) {
        if (!isEconomyEnabled()) {
            return false;
        }

        EconomyResponse resp = economy.withdrawPlayer(player, amount);
        return resp.transactionSuccess();
    }

    public boolean depositPlayer(OfflinePlayer player, double amount) {
        if (!isEconomyEnabled()) {
            return false;
        }

        EconomyResponse resp = economy.depositPlayer(player, amount);
        return resp.transactionSuccess();
    }

    public String formatCurrency(double amount) {
        StringBuilder builder = new StringBuilder();
        builder.append(new DecimalFormat("#.##").format(amount));
        builder.append(getCurrencyName(amount, true));
        return builder.toString();
    }

    public String getCurrencyName(boolean singular, boolean spaced) {
        if (!isEconomyEnabled()) {
            return "";
        }

        String name = singular ? economy.currencyNameSingular() : economy.currencyNamePlural();
        if (spaced && !name.isEmpty()) {
            return " " + name;
        }

        return name;
    }

    public String getCurrencyName(double amount, boolean spaced) {
        return getCurrencyName(amount == 1, spaced);
    }

    public String getCurrencyName(boolean singular) {
        return getCurrencyName(singular, false);
    }

    public String getCurrencyName(double amount) {
        return getCurrencyName(amount, false);
    }

    public String[] getGroups() {
        return isPermissionEnabled() ? permission.getGroups() : new String[0];
    }

    public String[] getGroups(Player player) {
        return hasPermissionGroupSupport() ? permission.getPlayerGroups(player) : new String[0];
    }

    public boolean hasGroup(String group) {
        return hasPermissionGroupSupport() && Arrays.stream(permission.getGroups()).anyMatch(group::equalsIgnoreCase);
    }

    public String getExactGroupName(String group) {
        if (!hasPermissionGroupSupport()) {
            return group;
        }

        return Arrays.stream(permission.getGroups()).filter(group::equalsIgnoreCase).findFirst().orElse(null);
    }

    public String getPrimaryGroup(Player player) {
        if (!isPermissionEnabled()) {
            return DEFAULT_GROUP;
        }

        try {
            return permission.getPrimaryGroup(player);
        } catch (Exception e) {
            return DEFAULT_GROUP;
        }
    }

    public boolean isInGroup(Player player, String group) {
        return hasPermissionGroupSupport() && permission.playerInGroup(player, group);
    }

    public boolean isInAnyGroup(Player player, Collection<String> groups) {
        return hasPermissionGroupSupport() && groups.stream().anyMatch(g -> permission.playerInGroup(player, g));
    }

    public boolean isInAllGroups(Player player, Collection<String> groups) {
        return hasPermissionGroupSupport() && groups.stream().allMatch(g -> permission.playerInGroup(player, g));
    }
}
