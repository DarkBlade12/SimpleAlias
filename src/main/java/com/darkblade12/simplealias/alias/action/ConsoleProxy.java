package com.darkblade12.simplealias.alias.action;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

final class ConsoleProxy implements ConsoleCommandSender {
    private final CommandSender source;
    private final boolean grantPermission;
    private final boolean silent;

    ConsoleProxy(CommandSender source, boolean grantPermission, boolean silent) {
        this.source = source;
        this.grantPermission = grantPermission;
        this.silent = silent;
    }

    @Override
    public void sendMessage(String message) {
        if (!silent) {
            source.sendMessage(message);
        }
    }

    @Override
    public void sendMessage(String[] messages) {
        if (!silent) {
            source.sendMessage(messages);
        }
    }

    @Override
    public Server getServer() {
        return source.getServer();
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public Spigot spigot() {
        return source.spigot();
    }

    @Override
    public boolean isPermissionSet(String name) {
        return source.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return source.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(String name) {
        return grantPermission || source.hasPermission(name);
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return grantPermission || source.hasPermission(perm);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
        return source.addAttachment(plugin, name, value);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin) {
        return source.addAttachment(plugin);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
        return source.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin plugin, int ticks) {
        return source.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(PermissionAttachment attachment) {
        source.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        source.recalculatePermissions();
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return source.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return grantPermission || source.isOp();
    }

    @Override
    public void setOp(boolean value) {
        source.setOp(value);
    }

    @Override
    public boolean isConversing() {
        return source instanceof Conversable && ((Conversable) source).isConversing();
    }

    @Override
    public void acceptConversationInput(String input) {
        if (source instanceof Conversable) {
            ((Conversable) source).acceptConversationInput(input);
        }
    }

    @Override
    public boolean beginConversation(Conversation conversation) {
        return !silent && source instanceof Conversable && ((Conversable) source).beginConversation(conversation);
    }

    @Override
    public void abandonConversation(Conversation conversation) {
        if (source instanceof Conversable) {
            ((Conversable) source).abandonConversation(conversation);
        }
    }

    @Override
    public void abandonConversation(Conversation conversation, ConversationAbandonedEvent details) {
        if (source instanceof Conversable) {
            ((Conversable) source).abandonConversation(conversation, details);
        }
    }

    @Override
    public void sendRawMessage(String message) {
        if (!silent && source instanceof Conversable) {
            ((Conversable) source).sendRawMessage(message);
        }
    }
}
