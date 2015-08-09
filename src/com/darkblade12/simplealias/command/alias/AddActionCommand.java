package com.darkblade12.simplealias.command.alias;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.ActionSetting;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.hook.types.VaultHook;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "addaction", params = "<name> <action> <setting> <value>", description = "Adds a value to an action setting of an alias", permission = Permission.ADD_ACTION_COMMAND, infiniteParams = true)
public final class AddActionCommand implements ICommand {
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
						Set<String> actionEnabledWorlds = action.getEnabledWorlds();
						for (String world : worlds) {
							World w = Bukkit.getWorld(world);
							if (w == null) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the world §4" + world + " §cdoesn't exist!");
								return;
							}
							String exactWorld = w.getName();
							if (actionEnabledWorlds.contains(exactWorld)) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the world §4" + exactWorld + " §cis already present!");
								return;
							}
							enabledWorlds.add(exactWorld);
						}
						actionEnabledWorlds.addAll(enabledWorlds);
						value = StringUtils.join(enabledWorlds, ", ");
					} else if (s == ActionSetting.ENABLED_PERMISSION_NODES) {
						String[] nodes = value.split(", ");
						Set<String> enabledPermissionNodes = new HashSet<String>();
						Set<String> actionEnabledPermissionNodes = action.getEnabledPermissionNodes();
						for (String node : nodes) {
							if (actionEnabledPermissionNodes.contains(node)) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the permission node §4" + node + " §cis already present!");
								return;
							}
							enabledPermissionNodes.add(node);
						}
						actionEnabledPermissionNodes.addAll(enabledPermissionNodes);
					} else if (s == ActionSetting.ENABLED_PERMISSION_GROUPS) {
						String[] groups = value.split(", ");
						Set<String> enabledPermissionGroups = new HashSet<String>();
						Set<String> actionEnabledPermissionGroups = action.getEnabledPermissionGroups();
						VaultHook v = SimpleAlias.getVaultHook();
						boolean enabled = v.isEnabled() && v.hasPermissionGroupSupport();
						for (String group : groups) {
							String exactGroup = v.getExactGroupName(group);
							if (enabled && exactGroup == null) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the group §4" + group + " §cdoesn't exist!");
								return;
							}
							String groupName = enabled ? exactGroup : group;
							if (actionEnabledPermissionGroups.contains(groupName)) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the group §4" + groupName + " §cis already present!");
								return;
							}
							enabledPermissionGroups.add(groupName);
						}
						actionEnabledPermissionGroups.addAll(enabledPermissionGroups);
						value = StringUtils.join(enabledPermissionGroups, ", ");
					} else if (s == ActionSetting.ENABLED_PARAMS) {
						String[] actionParams = value.split(", ");
						Map<Integer, String> enabledParams = new HashMap<Integer, String>();
						Map<Integer, String> actionEnabledParams = action.getEnabledParams();
						for (String param : actionParams) {
							String[] split = param.split("@");
							if (split.length != 2) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because §4" + param + " §chas an invalid format!");
								return;
							}
							int index;
							try {
								index = Integer.parseInt(split[1]);
							} catch (Exception e) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because §4" + param + " §cdoesn't have a valid number as index!");
								if (Settings.isDebugEnabled()) {
									e.printStackTrace();
								}
								return;
							}
							if (enabledParams.containsKey(index)) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the index §4" + index + " §cis duplicate!");
								return;
							} else if (actionEnabledParams.containsKey(index)) {
								sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §ccan't be added to the setting §6" + path + "§c, because the index §4" + index + " §cis already present!");
								return;
							}
							enabledParams.put(index, split[0]);
						}
						actionEnabledParams.putAll(enabledParams);
					} else {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThis setting doesn't support the addition of values!");
						return;
					}
					try {
						a.save();
						sender.sendMessage(SimpleAlias.PREFIX + "§aThe value §2" + value + " §awas added to the setting §e" + path + " §aof the alias §6" + name + "§a.");
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