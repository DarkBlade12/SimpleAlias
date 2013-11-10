package com.darkblade12.simplealias.commands.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.CommandAlias;
import com.darkblade12.simplealias.alias.Executor;
import com.darkblade12.simplealias.alias.Type;
import com.darkblade12.simplealias.alias.types.MultipleAlias;
import com.darkblade12.simplealias.alias.types.SingleAlias;
import com.darkblade12.simplealias.alias.types.TextAlias;
import com.darkblade12.simplealias.commands.CommandDetails;
import com.darkblade12.simplealias.commands.ICommand;
import com.darkblade12.simplealias.util.StringUtil;

@CommandDetails(name = "setting", usage = "/sa setting <name> <setting> <args>", description = "Changes a setting of an alias", executableAsConsole = true, permission = "SimpleAlias.setting")
public class SettingCommand implements ICommand {
	@Override
	public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
		String name = StringUtil.stripFirstSlash(params[0]);
		Alias a = plugin.aliasHandler.get(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
			return;
		}
		Type t = a.getType();
		Setting s = Setting.fromAbbreviation(params[1]);
		if (s == null) {
			plugin.commandHandler.showUsage(sender, this);
			return;
		}
		switch (s) {
			case DESCRIPTION:
				a.setDescription(StringUtils.join((String[]) Arrays.copyOfRange(params, 2, params.length), " "));
				break;
			case EXECUTOR:
				if (t == Type.TEXT) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting is only available for \"Single\" and \"Multiple\" type aliases!");
					return;
				}
				Executor executor = Executor.fromName(params[2]);
				if (executor == null) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThe entered executor name is invalid!");
					return;
				}
				((CommandAlias) a).setExecutor(executor);
				break;
			case EXECUTABLE_AS_CONSOLE:
				if (t == Type.TEXT) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting is only available for \"Single\" and \"Multiple\" type aliases!");
					return;
				}
				boolean executableAsConsole;
				try {
					executableAsConsole = Boolean.parseBoolean(params[2]);
				} catch (Exception e) {
					sender.sendMessage("§cThe entered boolean value is invalid!");
					return;
				}
				((CommandAlias) a).setExecutableAsConsole(executableAsConsole);
				break;
			case MESSAGE:
				if (t == Type.TEXT) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting is only available for \"Single\" and \"Multiple\" type aliases!");
					return;
				}
				((CommandAlias) a).setMessage(ChatColor.translateAlternateColorCodes('&', StringUtils.join((String[]) Arrays.copyOfRange(params, 2, params.length), " ")));
				break;
			case COOLDOWN_ENABLED:
				if (t == Type.TEXT) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting is only available for \"Single\" and \"Multiple\" type aliases!");
					return;
				}
				boolean cooldownEnabled;
				try {
					cooldownEnabled = Boolean.parseBoolean(params[2]);
				} catch (Exception e) {
					sender.sendMessage("§cThe entered boolean value is invalid!");
					return;
				}
				((CommandAlias) a).setCooldownEnabled(cooldownEnabled);
				break;
			case COOLDOWN:
				if (t == Type.TEXT) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting is only available for \"Single\" and \"Multiple\" type aliases!");
					return;
				}
				long cooldown;
				try {
					cooldown = Long.parseLong(params[2]);
				} catch (Exception e) {
					sender.sendMessage("§cThe entered number is invalid, please type in a normal number!");
					return;
				}
				((CommandAlias) a).setCooldown(cooldown);
				break;
			case PERMISSION_ENABLED:
				boolean permissionEnabled;
				try {
					permissionEnabled = Boolean.parseBoolean(params[2]);
				} catch (Exception e) {
					sender.sendMessage("§cThe entered boolean value is invalid!");
					return;
				}
				a.setPermissionEnabled(permissionEnabled);
				break;
			case PERMISSION:
				a.setPermission(params[2]);
				break;
			case PERMITTED_GROUPS_ENABLED:
				boolean permittedGroupsEnabled;
				try {
					permittedGroupsEnabled = Boolean.parseBoolean(params[2]);
				} catch (Exception e) {
					sender.sendMessage("§cThe entered boolean value is invalid!");
					return;
				}
				a.setPermittedGroupsEnabled(permittedGroupsEnabled);
				break;
			case PERMITTED_GROUPS:
				Set<String> permittedGroups = new HashSet<String>();
				for (String group : (String[]) Arrays.copyOfRange(params, 2, params.length))
					permittedGroups.add(group);
				a.setPermittedGroups(permittedGroups);
				break;
			case COMMAND:
				if (t != Type.SINGLE) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting is only available for \"Single\" type aliases!");
					return;
				}
				String command = StringUtil.stripFirstSlash(StringUtils.join((String[]) Arrays.copyOfRange(params, 2, params.length), " "));
				if (command.split(" ")[0].equalsIgnoreCase(name)) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cYou can't make an alias executing itself!");
					return;
				}
				((SingleAlias) a).setCommand(command);
				break;
			case DISABLE_COMMAND:
				if (t != Type.SINGLE) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting is only available for \"Single\" type aliases!");
					return;
				}
				boolean disableCommand;
				try {
					disableCommand = Boolean.parseBoolean(params[2]);
				} catch (Exception e) {
					sender.sendMessage("§cThe entered boolean value is invalid!");
					return;
				}
				((SingleAlias) a).setDisableCommand(disableCommand);
				break;
			case DISABLE_MESSAGE:
				if (t != Type.SINGLE) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting is only available for \"Single\" type aliases!");
					return;
				}
				((SingleAlias) a).setDisableMessage(ChatColor.translateAlternateColorCodes('&', StringUtils.join((String[]) Arrays.copyOfRange(params, 2, params.length), " ")));
				break;
			case COMMANDS:
				if (t != Type.MULTIPLE) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting is only available for \"Multiple\" type aliases!");
					return;
				}
				List<String> commands = new ArrayList<String>();
				for (String cmd : StringUtils.join((String[]) Arrays.copyOfRange(params, 2, params.length), " ").split("#")) {
					String rcmd = StringUtil.stripFirstSlash(cmd);
					if (rcmd.split(" ")[0].equalsIgnoreCase(name)) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cYou can't make an alias executing itself!");
						return;
					}
					commands.add(rcmd);
				}
				((MultipleAlias) a).setCommands(commands);
				break;
			case LINES:
				if (t != Type.TEXT) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting is only available for \"Text\" type aliases!");
					return;
				}
				List<String> lines = new ArrayList<String>();
				for (String line : StringUtils.join((String[]) Arrays.copyOfRange(params, 2, params.length), " ").split("#"))
					lines.add(ChatColor.translateAlternateColorCodes('&', line));
				((TextAlias) a).setLines(lines);
				break;
		}
		sender.sendMessage(SimpleAlias.PREFIX + "§aThe setting §c" + s + " §aof the alias §6" + a.getName() + " §awas changed.");
	}

	private enum Setting {
		DESCRIPTION("Description", "desc"),
		EXECUTOR("Executor", "exec"),
		EXECUTABLE_AS_CONSOLE("Executable as Console", "exac"),
		MESSAGE("Message", "msg"),
		COOLDOWN_ENABLED("Cooldown Enabled", "cdwne"),
		COOLDOWN("Cooldown", "cdwn"),
		PERMISSION_ENABLED("Permission Enabled", "perme"),
		PERMISSION("Permission", "perm"),
		PERMITTED_GROUPS_ENABLED("Permitted Groups Enabled", "pgre"),
		PERMITTED_GROUPS("Permitted Groups", "pgr"),
		COMMAND("Command", "cmd"),
		DISABLE_COMMAND("Disable Command", "dcmd"),
		DISABLE_MESSAGE("Disable Message", "dmsg"),
		COMMANDS("Commands", "cmds"),
		LINES("Lines", "lns");

		private String name;
		private String abbreviation;
		private static final Map<String, Setting> ABBREVIATION_MAP = new HashMap<String, Setting>();

		static {
			for (Setting s : values())
				ABBREVIATION_MAP.put(s.getAbbreviation(), s);
		}

		private Setting(String name, String abbreviation) {
			this.name = name;
			this.abbreviation = abbreviation;
		}

		public String getName() {
			return this.name;
		}

		public String getAbbreviation() {
			return this.abbreviation;
		}

		public static Setting fromAbbreviation(String abbreviation) {
			if (abbreviation != null)
				for (Entry<String, Setting> e : ABBREVIATION_MAP.entrySet())
					if (e.getKey().equals(abbreviation))
						return e.getValue();
			return null;
		}

		@Override
		public String toString() {
			return getName();
		}
	}
}