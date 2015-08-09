package com.darkblade12.simplealias.command.alias;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.ActionSetting;
import com.darkblade12.simplealias.alias.action.Executor;
import com.darkblade12.simplealias.alias.action.Type;
import com.darkblade12.simplealias.alias.action.types.CommandAction;
import com.darkblade12.simplealias.alias.action.types.MessageAction;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.hook.types.VaultHook;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "setaction", params = "<name> <action> <setting> <value>", description = "Removes an existing action from an alias", permission = Permission.SET_ACTION_COMMAND, infiniteParams = true)
public final class SetActionCommand implements ICommand {
	private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		Alias a = SimpleAlias.getAliasManager().getAlias(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
		} else {
			Action action = a.getAction(params[1]);
			if (action == null) {
				sender.sendMessage(SimpleAlias.PREFIX + "§cAn action with this name doesn't exist!");
			} else {
				String actionName = action.getName();
				Type type = action.getType();
				ActionSetting s = ActionSetting.fromName(params[2]);
				if (s == null) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cA setting with this name/path doesn't exist!");
				} else {
					String path = s.getFullPath(actionName);
					String[] valueArray = (String[]) Arrays.copyOfRange(params, 3, params.length);
					String value = StringUtils.join(valueArray, " ");
					if (s == ActionSetting.ENABLED_WORLDS) {
						String[] worlds = value.split(", ");
						Set<String> enabledWorlds = new HashSet<String>();
						for (String world : worlds) {
							World w = Bukkit.getWorld(world);
							if (w == null) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because the world §4" + world + " §cdoesn't exist!");
								return;
							}
							enabledWorlds.add(w.getName());
						}
						action.setEnabledWorlds(enabledWorlds);
						value = StringUtils.join(enabledWorlds, ", ");
					} else if (s == ActionSetting.ENABLED_PERMISSION_NODES) {
						String[] nodes = value.split(", ");
						Set<String> enabledPermissionNodes = new HashSet<String>();
						for (String node : nodes)
							enabledPermissionNodes.add(node);
						action.setEnabledPermissionNodes(enabledPermissionNodes);
					} else if (s == ActionSetting.ENABLED_PERMISSION_GROUPS) {
						String[] groups = value.split(", ");
						Set<String> enabledPermissionGroups = new HashSet<String>();
						VaultHook v = SimpleAlias.getVaultHook();
						boolean enabled = v.isEnabled() && v.hasPermissionGroupSupport();
						for (String group : groups) {
							String exactGroup = v.getExactGroupName(group);
							if (enabled && exactGroup == null) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because the group §4" + group + " §cdoesn't exist!");
								return;
							}
							enabledPermissionGroups.add(enabled ? exactGroup : group);
						}
						action.setEnabledPermissionGroups(enabledPermissionGroups);
						value = StringUtils.join(enabledPermissionGroups, ", ");
					} else if (s == ActionSetting.ENABLED_PARAMS) {
						String[] actionParams = value.split(", ");
						Map<Integer, String> enabledParams = new HashMap<Integer, String>();
						for (String param : actionParams) {
							String[] split = param.split("@");
							if (split.length != 2) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because §4" + param + " §chas an invalid format!");
								return;
							}
							int index;
							try {
								index = Integer.parseInt(split[1]);
							} catch (Exception e) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because §4" + param + " §cdoesn't have a valid number as index!");
								if (Settings.isDebugEnabled()) {
									e.printStackTrace();
								}
								return;
							}
							if (enabledParams.containsKey(index)) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because the index §4" + index + " §cis duplicate!");
								return;
							}
							enabledParams.put(index, split[0]);
						}
						action.setEnabledParams(enabledParams);
					} else if (s == ActionSetting.PRIORITY) {
						int priority;
						try {
							priority = Integer.parseInt(value);
						} catch (Exception e) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't a valid number!");
							if (Settings.isDebugEnabled()) {
								e.printStackTrace();
							}
							return;
						}
						action.setPriority(priority);
						value = String.valueOf(priority);
					} else if (s == ActionSetting.TRANSLATE_COLOR_CODES) {
						if (!BOOLEAN_PATTERN.matcher(value).matches()) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
							return;
						}
						boolean translateColorCodes = Boolean.parseBoolean(value);
						action.setTranslateColorCodes(translateColorCodes);
						value = String.valueOf(translateColorCodes);
					} else if (s == ActionSetting.COMMAND) {
						if (type != Type.COMMAND) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting can only be set for a §6COMMAND §caction!");
							return;
						}
						String command = StringUtils.removeStart(value, "/");
						if (command.split(" ")[0].equalsIgnoreCase(name)) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it will execute the alias!");
							return;
						}
						((CommandAction) action).setCommand(command);
						value = command;
					} else if (s == ActionSetting.EXECUTOR) {
						if (type != Type.COMMAND) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting can only be set for a §6COMMAND §caction!");
							return;
						}
						Executor executor = Executor.fromName(value);
						if (executor == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because the executor §4" + value + " §cdoesn't exist!");
							return;
						}
						((CommandAction) action).setExecutor(executor);
						value = executor.name();
					} else if (s == ActionSetting.GRANT_PERMISSION) {
						if (type != Type.COMMAND) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting can only be set for a §6COMMAND §caction!");
							return;
						} else if (!BOOLEAN_PATTERN.matcher(value).matches()) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
							return;
						}
						boolean grantPermission = Boolean.parseBoolean(value);
						((CommandAction) action).setGrantPermission(grantPermission);
						value = String.valueOf(grantPermission);
					} else if (s == ActionSetting.TEXT) {
						if (type != Type.MESSAGE) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting can only be set for a §6MESSAGE §caction!");
							return;
						}
						String text = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value));
						((MessageAction) action).setText(text);
						value = text;
					} else if (s == ActionSetting.BROADCAST) {
						if (type != Type.MESSAGE) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting can only be set for a §6MESSAGE §caction!");
							return;
						} else if (!BOOLEAN_PATTERN.matcher(value).matches()) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
							return;
						}
						boolean broadcast = Boolean.parseBoolean(value);
						((MessageAction) action).setBroadcast(broadcast);
						value = String.valueOf(broadcast);
					}
					try {
						a.save();
						sender.sendMessage(SimpleAlias.PREFIX + "§aThe setting §e" + path + " §aof the alias §6" + name + " §awas set to §2" + value + "§a.");
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cFailed to save the alias! Cause: " + e.getMessage());
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}