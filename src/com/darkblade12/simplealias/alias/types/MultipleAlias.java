package com.darkblade12.simplealias.alias.types;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.CommandAlias;
import com.darkblade12.simplealias.alias.Executor;
import com.darkblade12.simplealias.alias.Type;

public class MultipleAlias extends CommandAlias {
	private List<String> commands;

	public MultipleAlias(SimpleAlias plugin, String name, List<String> commands, String description, Executor executor, boolean executableAsConsole, String message, boolean cooldownEnabled, long cooldown,
			boolean permissionEnabled, String permission, boolean permittedGroupsEnabled, Set<String> permittedGroups) {
		super(plugin, name, Type.MULTIPLE, description, executor, executableAsConsole, message, cooldownEnabled, cooldown, permissionEnabled, permission, permittedGroupsEnabled, permittedGroups);
		this.commands = commands;
	}

	public MultipleAlias(SimpleAlias plugin, String name, List<String> commands) {
		super(plugin, name, Type.MULTIPLE, null, Executor.SENDER, false, null, false, 0, true, DEFAULT_PERMISSION.replace("<name>", name), false, null);
		this.commands = commands;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		super.execute(sender, args);
		for (String command : commands)
			performCommand(sender, prepareCommand(sender, command, args));
		if (message != null)
			sender.sendMessage(message);
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
		setConfigValue("Execution_Settings.Commands", StringUtils.join(commands, "#"));
	}

	public void addCommands(String... commands) {
		for (String command : commands)
			this.commands.add(command);
	}

	public List<String> getCommands() {
		return this.commands;
	}
}