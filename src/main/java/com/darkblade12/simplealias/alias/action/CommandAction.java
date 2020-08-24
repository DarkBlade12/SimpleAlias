package com.darkblade12.simplealias.alias.action;

import com.darkblade12.simplealias.SimpleAlias;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class CommandAction extends Action {
    private String command;
    private Executor executor;
    private boolean grantPermission;
    private boolean silent;

    public CommandAction(String name, Set<String> enabledWorlds, Set<String> enabledPermissionNodes, Set<String> enabledPermissionGroups,
                         Map<Integer, String> enabledParams, int priority, boolean translateColorCodes, String command, Executor executor,
                         boolean grantPermission, boolean silent) {
        super(name, enabledWorlds, enabledPermissionNodes, enabledPermissionGroups, enabledParams, priority, translateColorCodes);
        this.command = command;
        this.executor = executor;
        this.grantPermission = grantPermission;
        this.silent = silent;
    }

    public CommandAction(String name, String command) {
        this(name, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashMap<>(), 0, false, command, Executor.SENDER, false, false);
    }

    @Override
    public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
        executor.dispatchCommand(plugin, sender, replaceVariables(command, plugin, sender, params), grantPermission, silent);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setGrantPermission(boolean grantPermission) {
        this.grantPermission = grantPermission;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    public ActionType getType() {
        return ActionType.COMMAND;
    }

    public String getCommand() {
        return command;
    }

    public Executor getExecutor() {
        return executor;
    }

    public boolean hasGrantPermission() {
        return grantPermission;
    }

    public boolean isSilent() {
        return silent;
    }
}
