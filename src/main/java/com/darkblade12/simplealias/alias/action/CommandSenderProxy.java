package com.darkblade12.simplealias.alias.action;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

final class CommandSenderProxy implements RemoteConsoleCommandSender {
    private final CommandSender originalSender;
    private final boolean grantPermission;
    private final boolean silent;

    CommandSenderProxy(CommandSender originalSender, boolean grantPermission, boolean silent) {
        this.originalSender = originalSender;
        this.grantPermission = grantPermission;
        this.silent = silent;
    }

    @Override
    public void sendMessage(String message) {
        if (!silent) {
            originalSender.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(String[] messages) {
        if (!silent) {
            originalSender.sendMessage(messages);
        }
    }

    @Override
    public Server getServer() {
        return originalSender.getServer();
    }

    @Override
    public String getName() {
        return originalSender.getName();
    }

    @Override
    public Spigot spigot() {
        return originalSender.spigot();
    }

    @Override
    public boolean isPermissionSet(String name) {
        return originalSender.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return originalSender.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return grantPermission || originalSender.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return grantPermission || originalSender.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return originalSender.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return originalSender.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return originalSender.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return originalSender.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        originalSender.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        originalSender.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return originalSender.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return grantPermission || originalSender.isOp();
    }

    @Override
    public void setOp(boolean value) {
        originalSender.setOp(value);
    }
}
