package com.darkblade12.simplealias.alias.types;

import java.util.Set;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.CommandAlias;
import com.darkblade12.simplealias.alias.Executor;
import com.darkblade12.simplealias.alias.Type;

public class SingleAlias extends CommandAlias {
	private final static String DEFAULT_DISABLE_MESSAGE = "Unknown Command. Type \"/help\" for help.";
	private String command;
	private boolean disableCommand;
	private String disableMessage;

	public SingleAlias(SimpleAlias plugin, String name, String command, boolean disableCommand, String disableMessage, String description, Executor executor, boolean executableAsConsole, String message,
			boolean cooldownEnabled, long cooldown, boolean permissionEnabled, String permission, boolean permittedGroupsEnabled, Set<String> permittedGroups) {
		super(plugin, name, Type.SINGLE, description, executor, executableAsConsole, message, cooldownEnabled, cooldown, permissionEnabled, permission, permittedGroupsEnabled, permittedGroups);
		this.command = command;
		this.disableCommand = disableCommand;
		this.disableMessage = disableMessage;
	}

	public SingleAlias(SimpleAlias plugin, String name, String command) {
		super(plugin, name, Type.SINGLE, null, Executor.SENDER, false, null, false, 0, true, DEFAULT_PERMISSION.replace("<name>", name), false, null);
		this.command = command;
		this.disableCommand = false;
		this.disableMessage = DEFAULT_DISABLE_MESSAGE;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		super.execute(sender, args);
		performCommand(sender, prepareCommand(sender, command, args));
		if (message != null)
			sender.sendMessage(message);
	}

	public void setCommand(String command) {
		this.command = command;
		setConfigValue("Execution_Settings.Command", command);
	}

	public void setDisableCommand(boolean disableCommand) {
		this.disableCommand = disableCommand;
		setConfigValue("Execution_Settings.Disable_Command", disableCommand);
	}

	public void setDisableMessage(String disableMessage) {
		this.disableMessage = disableMessage;
		setConfigValue("Execution_Settings.Disable_Message", disableMessage.replace('§', '&'));
	}

	public String getCommand() {
		return this.command;
	}

	public boolean getDisableCommand() {
		return this.disableCommand;
	}

	public String getDisableMessage() {
		return this.disableMessage;
	}
}