package com.darkblade12.simplealias.alias.action.types;

import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.Executor;
import com.darkblade12.simplealias.alias.action.Type;
import com.darkblade12.simplealias.util.ColorCode;

public final class CommandAction extends Action {
	private String command;
	private Executor executor;
	private boolean grantPermission;

	public CommandAction(String name, Set<String> enabledWorlds, Set<String> enabledPermissionNodes, Set<String> enabledPermissionGroups, Map<Integer, String> enabledParams, int priority, boolean translateColorCodes, String command, Executor executor, boolean grantPermission) {
		super(name, enabledWorlds, enabledPermissionNodes, enabledPermissionGroups, enabledParams, priority, translateColorCodes);
		this.command = command;
		this.executor = executor;
		this.grantPermission = grantPermission;
	}

	@Override
	public void execute(CommandSender sender, String[] params) {
		executor.dispatchCommand(sender, applyReplacement(command, sender, translateColorCodes ? ColorCode.translateAlternateColorCodes('&', params) : params), grantPermission);
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

	@Override
	public Type getType() {
		return Type.COMMAND;
	}

	public String getCommand() {
		return this.command;
	}

	public Executor getExecutor() {
		return this.executor;
	}

	public boolean getGrantPermission() {
		return this.grantPermission;
	}
}