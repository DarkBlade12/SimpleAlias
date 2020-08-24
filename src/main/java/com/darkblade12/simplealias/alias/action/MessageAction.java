package com.darkblade12.simplealias.alias.action;

import com.darkblade12.simplealias.SimpleAlias;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MessageAction extends Action {
    private String message;
    private boolean broadcast;

    public MessageAction(String name, Set<String> enabledWorlds, Set<String> enabledPermissionNodes, Set<String> enabledPermissionGroups,
                         Map<Integer, String> enabledParams, int priority, boolean translateColorCodes, String message, boolean broadcast) {
        super(name, enabledWorlds, enabledPermissionNodes, enabledPermissionGroups, enabledParams, priority, translateColorCodes);
        this.message = message;
        this.broadcast = broadcast;
    }

    public MessageAction(String name, String message) {
        this(name, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashMap<>(), 0, false, message, false);
    }

    @Override
    public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
        String message = replaceVariables(this.message, plugin, sender, params);
        if (broadcast) {
            Bukkit.broadcastMessage(message);
        } else {
            sender.sendMessage(message);
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    @Override
    public ActionType getType() {
        return ActionType.MESSAGE;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isBroadcast() {
        return this.broadcast;
    }
}
