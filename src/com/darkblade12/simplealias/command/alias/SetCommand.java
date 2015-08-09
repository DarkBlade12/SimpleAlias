package com.darkblade12.simplealias.command.alias;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.Setting;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "set", params = "<name> <setting> <value>", description = "Sets the value of a setting of an alias", permission = Permission.SET_COMMAND)
public final class SetCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		Alias a = SimpleAlias.getAliasManager().getAlias(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
		} else {
			Setting s = Setting.fromName(params[1]);
			if (s == null) {
				sender.sendMessage(SimpleAlias.PREFIX + "§cA setting with this name/path doesn't exist!");
			} else {
				String path = s.getPath();
				String[] valueArray = (String[]) Arrays.copyOfRange(params, 2, params.length);
				String value = StringUtils.join(valueArray, " ");
				if (s == Setting.DESCRIPTION) {
					a.setDescription(value);
				} else if (s == Setting.EXECUTABLE_AS_CONSOLE) {
					boolean executableAsConsole;
					try {
						executableAsConsole = Boolean.parseBoolean(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because it isn't boolean!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					a.setExecutableAsConsole(executableAsConsole);
				} else if (s == Setting.ENABLED_WORLDS) {
					String[] worlds = value.split(", ");
					Set<String> enabledWorlds = new HashSet<String>();
					for (String world : worlds) {
						if (Bukkit.getWorld(world) == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because this world doesn't exist!");
							return;
						}
						enabledWorlds.add(world);
					}
					a.setEnabledWorlds(enabledWorlds);
				} else if (s == Setting.EXECUTION_ORDER) {
					String[] actions = value.split(", ");
					List<String> executionOrder = new ArrayList<String>();
					for (String action : actions) {
						if (!a.hasAction(action)) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because this action doesn't exist!");
							return;
						}
						executionOrder.add(action);
					}
					a.setExecutionOrder(executionOrder);
				} else if (s == Setting.USAGE_CHECK_ENABLED) {
					boolean usageCheckEnabled;
					try {
						usageCheckEnabled = Boolean.parseBoolean(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because it isn't boolean!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (usageCheckEnabled) {
						int minParams = a.getUsageCheckMinParams();
						if (minParams < 0) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.USAGE_CHECK_MIN_PARAMS.getPath() + " §cis lower than 0!");
							return;
						} else if (a.getUsageCheckMaxParams() < minParams) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.USAGE_CHECK_MAX_PARAMS.getPath() + " §cis lower than §5" + Setting.USAGE_CHECK_MIN_PARAMS + "§c!");
							return;
						} else if (a.getUsageCheckMessage() == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.USAGE_CHECK_MESSAGE.getPath() + " §cis null!");
							return;
						}
					}
				} else if (s == Setting.USAGE_CHECK_MIN_PARAMS) {
					int usageCheckMinParams;
					try {
						usageCheckMinParams = Integer.parseInt(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because it isn't a valid number!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					a.setUsageCheckMinParams(usageCheckMinParams);
				} else if (s == Setting.USAGE_CHECK_MAX_PARAMS) {
					int usageCheckMaxParams;
					try {
						usageCheckMaxParams = Integer.parseInt(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because it isn't a valid number!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (usageCheckMaxParams < a.getUsageCheckMinParams()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7" + usageCheckMaxParams + "§c, because §e" + Setting.USAGE_CHECK_MAX_PARAMS.getPath() + " §cis lower than §5" + Setting.USAGE_CHECK_MIN_PARAMS + "§c!");
						return;
					}
					a.setUsageCheckMaxParams(usageCheckMaxParams);
				} else if (s == Setting.USAGE_CHECK_MESSAGE) {
					a.setUsageCheckMessage(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value)));
				} else if (s == Setting.PERMISSION_ENABLED) {
					boolean permissionEnabled;
					try {
						permissionEnabled = Boolean.parseBoolean(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because it isn't boolean!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (permissionEnabled) {
						if (a.getPermissionNode() == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.PERMISSION_NODE.getPath() + " §cis null!");
							return;
						} else if (a.getPermissionMessage() == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.PERMISSION_MESSAGE.getPath() + " §cis null!");
							return;
						}
					}
					a.setPermissionEnabled(permissionEnabled);
				} else if (s == Setting.PERMISSION_NODE) {
					a.setPermissionNode(value);
				} else if (s == Setting.PERMISSION_GROUPS) {
					String[] groups = value.split(", ");
					Set<String> permissionGroups = new HashSet<String>();
					for (String group : groups) {
						permissionGroups.add(group);
					}
					a.setPermissionGroups(permissionGroups);
				} else if (s == Setting.PERMISSION_MESSAGE) {
					a.setPermissionMessage(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value)));
				} else if (s == Setting.DELAY_ENABLED) {
					boolean delayEnabled;
					try {
						delayEnabled = Boolean.parseBoolean(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because it isn't boolean!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (delayEnabled) {
						if (a.getDelayDuration() < 1) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.DELAY_DURATION.getPath() + " §cis lower than 1!");
							return;
						} else if (a.getDelayCancelOnMove() && a.getDelayCancelMessage() == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.DELAY_CANCEL_ON_MOVE.getPath() + " §cis true and §5" + Setting.DELAY_CANCEL_MESSAGE + " §cis null!");
							return;
						} else if (a.getDelayMessage() == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.DELAY_MESSAGE.getPath() + " §cis null!");
							return;
						}
					}
				} else if (s == Setting.DELAY_CANCEL_ON_MOVE) {
					boolean delayCancelOnMove;
					try {
						delayCancelOnMove = Boolean.parseBoolean(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because it isn't boolean!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (delayCancelOnMove && a.getDelayCancelMessage() == null) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.DELAY_CANCEL_MESSAGE + " §cis null!");
						return;
					}
					a.setDelayCancelOnMove(delayCancelOnMove);
				} else if (s == Setting.DELAY_DURATION) {
					int delayDuration;
					try {
						delayDuration = Integer.parseInt(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because it isn't a valid number!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (delayDuration < 1) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7" + delayDuration + "§c, because it's lower than 1!");
						return;
					}
					a.setDelayDuration(delayDuration);
				} else if (s == Setting.DELAY_MESSAGE) {
					a.setDelayMessage(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value)));
				} else if (s == Setting.DELAY_CANCEL_MESSAGE) {
					a.setDelayCancelMessage(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value)));
				} else if (s == Setting.COOLDOWN_ENABLED) {
					boolean cooldownEnabled;
					try {
						cooldownEnabled = Boolean.parseBoolean(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because it isn't boolean!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (cooldownEnabled) {
						if (a.getCooldownDuration() < 1) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.COOLDOWN_DURATION.getPath() + " §cis lower than 1!");
							return;
						} else if (a.getCooldownMessage() == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.COOLDOWN_MESSAGE.getPath() + " §cis null!");
							return;
						}
					}
					a.setCooldownEnabled(cooldownEnabled);
				} else if (s == Setting.COOLDOWN_DURATION) {
					int cooldownDuration;
					try {
						cooldownDuration = Integer.parseInt(value);
					} catch(Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " is invalid for the setting §6" + path + "§c, because it isn't a valid number!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if(cooldownDuration < 1) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7" + cooldownDuration + "§c, because it's lower than 1!");
						return;
					}
					a.setCooldownDuration(cooldownDuration);
				} else if(s == Setting.COOLDOWN_MESSAGE) {
					a.setCooldownMessage(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value)));
				}
				try {
					a.save();
					sender.sendMessage(SimpleAlias.PREFIX + "§aThe setting §e" + path + " of the alias §6" + name + " §awas set to §5" + value + "§a.");
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