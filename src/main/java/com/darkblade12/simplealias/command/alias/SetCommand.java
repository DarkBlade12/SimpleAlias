package com.darkblade12.simplealias.command.alias;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.Setting;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.hook.types.VaultHook;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "set", params = "<name> <setting> <value>", description = "Sets the value of a setting of an alias", permission = Permission.SET_COMMAND, infiniteParams = true)
public final class SetCommand implements ICommand {
	private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

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
					if (!BOOLEAN_PATTERN.matcher(value).matches()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
						return;
					}
					boolean executableAsConsole = Boolean.parseBoolean(value);
					a.setExecutableAsConsole(executableAsConsole);
					value = String.valueOf(executableAsConsole);
				} else if (s == Setting.ENABLED_WORLDS) {
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
					a.setEnabledWorlds(enabledWorlds);
					value = StringUtils.join(enabledWorlds, ", ");
				} else if (s == Setting.EXECUTION_ORDER) {
					String[] actions = value.split(", ");
					List<String> executionOrder = new ArrayList<String>();
					for (String action : actions) {
						Action aliasAction = a.getAction(action);
						if (aliasAction == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because the action §4" + action + " §cdoesn't exist!");
							return;
						}
						executionOrder.add(aliasAction.getName());
					}
					a.setExecutionOrder(executionOrder);
					value = StringUtils.join(executionOrder, ", ");
				} else if (s == Setting.USAGE_CHECK_ENABLED) {
					if (!BOOLEAN_PATTERN.matcher(value).matches()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
						return;
					}
					boolean usageCheckEnabled = Boolean.parseBoolean(value);
					if (usageCheckEnabled) {
						int minParams = a.getUsageCheckMinParams();
						if (minParams < 0) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.USAGE_CHECK_MIN_PARAMS.getPath() + " §cis lower than 0!");
							return;
						} else if (a.getUsageCheckMaxParams() < minParams) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.USAGE_CHECK_MAX_PARAMS.getPath() + " §cis lower than §e" + Setting.USAGE_CHECK_MIN_PARAMS.getPath() + "§c!");
							return;
						} else if (a.getUsageCheckMessage() == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.USAGE_CHECK_MESSAGE.getPath() + " §cis null!");
							return;
						}
					}
					a.setUsageCheckEnabled(usageCheckEnabled);
					value = String.valueOf(usageCheckEnabled);
				} else if (s == Setting.USAGE_CHECK_MIN_PARAMS) {
					int usageCheckMinParams;
					try {
						usageCheckMinParams = Integer.parseInt(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't a valid number!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (usageCheckMinParams > a.getUsageCheckMaxParams()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7" + usageCheckMinParams + "§c, because it's higher than §e" + Setting.USAGE_CHECK_MAX_PARAMS.getPath() + "§c!");
						return;
					}
					a.setUsageCheckMinParams(usageCheckMinParams);
					value = String.valueOf(usageCheckMinParams);
				} else if (s == Setting.USAGE_CHECK_MAX_PARAMS) {
					int usageCheckMaxParams;
					try {
						usageCheckMaxParams = Integer.parseInt(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't a valid number!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (usageCheckMaxParams < a.getUsageCheckMinParams()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7" + usageCheckMaxParams + "§c, because it's lower than §e" + Setting.USAGE_CHECK_MIN_PARAMS.getPath() + "§c!");
						return;
					}
					a.setUsageCheckMaxParams(usageCheckMaxParams);
					value = String.valueOf(usageCheckMaxParams);
				} else if (s == Setting.USAGE_CHECK_MESSAGE) {
					String usageCheckMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value));
					a.setUsageCheckMessage(usageCheckMessage);
					value = usageCheckMessage;
				} else if (s == Setting.PERMISSION_ENABLED) {
					if (!BOOLEAN_PATTERN.matcher(value).matches()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
						return;
					}
					boolean permissionEnabled = Boolean.parseBoolean(value);
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
					value = String.valueOf(permissionEnabled);
				} else if (s == Setting.PERMISSION_NODE) {
					a.setPermissionNode(value);
				} else if (s == Setting.PERMISSION_GROUPS) {
					String[] groups = value.split(", ");
					Set<String> permissionGroups = new HashSet<String>();
					VaultHook v = SimpleAlias.getVaultHook();
					boolean enabled = v.isEnabled() && v.hasPermissionGroupSupport();
					for (String group : groups) {
						String exactGroup = v.getExactGroupName(group);
						if (enabled && exactGroup == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because the group §4" + group + " §cdoesn't exist!");
							return;
						}
						permissionGroups.add(enabled ? exactGroup : group);
					}
					a.setPermissionGroups(permissionGroups);
					value = StringUtils.join(permissionGroups, ", ");
				} else if (s == Setting.PERMISSION_MESSAGE) {
					String permissionMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value));
					a.setPermissionMessage(permissionMessage);
					value = permissionMessage;
				} else if (s == Setting.DELAY_ENABLED) {
					if (!BOOLEAN_PATTERN.matcher(value).matches()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
						return;
					}
					boolean delayEnabled = Boolean.parseBoolean(value);
					if (delayEnabled) {
						if (a.getDelayDuration() < 1) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.DELAY_DURATION.getPath() + " §cis lower than 1!");
							return;
						} else if (a.getDelayCancelOnMove() && a.getDelayCancelMessage() == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.DELAY_CANCEL_ON_MOVE.getPath() + " §cis true and §e" + Setting.DELAY_CANCEL_MESSAGE.getPath() + " §cis null!");
							return;
						} else if (a.getDelayMessage() == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.DELAY_MESSAGE.getPath() + " §cis null!");
							return;
						}
					}
					a.setDelayEnabled(delayEnabled);
					value = String.valueOf(delayEnabled);
				} else if (s == Setting.DELAY_CANCEL_ON_MOVE) {
					if (!BOOLEAN_PATTERN.matcher(value).matches()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
						return;
					}
					boolean delayCancelOnMove = Boolean.parseBoolean(value);
					if (delayCancelOnMove && a.getDelayCancelMessage() == null) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.DELAY_CANCEL_MESSAGE + " §cis null!");
						return;
					}
					a.setDelayCancelOnMove(delayCancelOnMove);
					value = String.valueOf(delayCancelOnMove);
				} else if (s == Setting.DELAY_DURATION) {
					int delayDuration;
					try {
						delayDuration = Integer.parseInt(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't a valid number!");
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
					value = String.valueOf(delayDuration);
				} else if (s == Setting.DELAY_MESSAGE) {
					String delayMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value));
					a.setDelayMessage(delayMessage);
					value = delayMessage;
				} else if (s == Setting.DELAY_CANCEL_MESSAGE) {
					String delayCancelMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value));
					a.setDelayCancelMessage(delayCancelMessage);
					value = delayCancelMessage;
				} else if (s == Setting.COOLDOWN_ENABLED) {
					if (!BOOLEAN_PATTERN.matcher(value).matches()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
						return;
					}
					boolean cooldownEnabled = Boolean.parseBoolean(value);
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
					value = String.valueOf(cooldownEnabled);
				} else if (s == Setting.COOLDOWN_DURATION) {
					int cooldownDuration;
					try {
						cooldownDuration = Integer.parseInt(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't a valid number!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (cooldownDuration < 1) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7" + cooldownDuration + "§c, because it's lower than 1!");
						return;
					}
					a.setCooldownDuration(cooldownDuration);
					value = String.valueOf(cooldownDuration);
				} else if (s == Setting.COOLDOWN_MESSAGE) {
					String cooldownMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value));
					a.setCooldownMessage(cooldownMessage);
					value = cooldownMessage;
				} else if (s == Setting.COST_ENABLED) {
					if (!BOOLEAN_PATTERN.matcher(value).matches()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
						return;
					}
					boolean costEnabled = Boolean.parseBoolean(value);
					if (costEnabled) {
						if (a.getCostAmount() < 1) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.COST_AMOUNT.getPath() + " §cis lower than 1!");
							return;
						} else if (a.getCostMessage() == null) {
							sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.COST_MESSAGE.getPath() + " §cis null!");
							return;
						}
					}
					a.setCostEnabled(costEnabled);
					value = String.valueOf(costEnabled);
				} else if (s == Setting.COST_AMOUNT) {
					double costAmount;
					try {
						costAmount = Double.parseDouble(value);
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't a valid number!");
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						return;
					}
					if (costAmount == 0) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7" + costAmount + "§c, because it's equal to 0!");
						return;
					} else if (costAmount < 0) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7" + costAmount + "§c, because it's lower than 0!");
						return;
					}
					a.setCostAmount(costAmount);
					value = String.valueOf(costAmount);
				} else if (s == Setting.COST_MESSAGE) {
					String costMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(value));
					a.setCostMessage(costMessage);
					value = costMessage;
				} else if (s == Setting.LOGGING_ENABLED) {
					if (!BOOLEAN_PATTERN.matcher(value).matches()) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe value §e" + value + " §cis invalid for the setting §6" + path + "§c, because it isn't boolean!");
						return;
					}
					boolean loggingEnabled = Boolean.parseBoolean(value);
					if (loggingEnabled && a.getLoggingMessage() == null) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThe setting §6" + path + " §ccan't be set to §7true§c, because §e" + Setting.LOGGING_MESSAGE.getPath() + " §cis null!");
						return;
					}
					a.setLoggingEnabled(loggingEnabled);
					value = String.valueOf(loggingEnabled);
				} else if (s == Setting.LOGGING_MESSAGE) {
					a.setLoggingMessage(value);
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