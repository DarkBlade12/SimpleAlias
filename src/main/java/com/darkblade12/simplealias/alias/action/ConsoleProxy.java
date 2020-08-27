package com.darkblade12.simplealias.alias.action;

import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

final class ConsoleProxy implements ConsoleCommandSender {
    private final ConsoleCommandSender consoleSender;
    private final boolean silent;

    ConsoleProxy(ConsoleCommandSender consoleSender, boolean silent) {
        this.consoleSender = consoleSender;
        this.silent = silent;
    }

    @Override
    public void sendMessage(String message) {
        if (!silent) {
            consoleSender.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(String[] messages) {
        if (!silent) {
            consoleSender.sendMessage(messages);
        }
    }

    @Override
    public Server getServer() {
        return consoleSender.getServer();
    }

    @Override
    public String getName() {
        return consoleSender.getName();
    }

    @Override
    public Spigot spigot() {
        return consoleSender.spigot();
    }

    @Override
    public boolean isPermissionSet(String name) {
        return consoleSender.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return consoleSender.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return consoleSender.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return consoleSender.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return consoleSender.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return consoleSender.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return consoleSender.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return consoleSender.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        consoleSender.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        consoleSender.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return consoleSender.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return consoleSender.isOp();
    }

    @Override
    public void setOp(boolean value) {
        consoleSender.setOp(value);
    }

    @Override
    public boolean isConversing() {
        return consoleSender.isConversing();
    }

    @Override
    public void acceptConversationInput(String input) {
        consoleSender.acceptConversationInput(input);
    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        return !silent && consoleSender.beginConversation(conversation);
    }

    @Override
    public void abandonConversation(Conversation conversation) {
        consoleSender.abandonConversation(conversation);
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
        consoleSender.abandonConversation(conversation, details);
    }

    @Override
    public void sendRawMessage(String message) {
        if (!silent) {
            consoleSender.sendRawMessage(message);
        }
    }
}
