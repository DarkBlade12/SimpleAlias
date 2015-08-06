package com.darkblade12.simplealias;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.Executor;
import com.darkblade12.simplealias.alias.action.types.CommandAction;
import com.darkblade12.simplealias.alias.action.types.MessageAction;
import com.darkblade12.simplealias.reader.types.ConfigurationReader;
import com.darkblade12.simplealias.section.IndependantConfigurationSection;
import com.darkblade12.simplealias.section.exception.SectionNotFoundException;

public final class Converter {
	private static final File DIRECTORY = new File("plugins/SimpleAlias/aliases/");
	private static final IndependantConfigurationSection GENERAL_INFORMATION = new IndependantConfigurationSection("General_Information");
	private static final IndependantConfigurationSection EXECUTION_SETTINGS = new IndependantConfigurationSection("Execution_Settings");
	private static final IndependantConfigurationSection COOLDOWN_SETTINGS = new IndependantConfigurationSection("Cooldown_Settings");
	private static final IndependantConfigurationSection PERMISSION_SETTINGS = new IndependantConfigurationSection("Permission_Settings");
	private static final IndependantConfigurationSection PERMITTED_GROUPS = new IndependantConfigurationSection("Permitted_Groups");

	private Converter() {};

	public static void convertAliases() {
		int amount = 0;
		if (DIRECTORY.exists() && DIRECTORY.isDirectory())
			for (File f : DIRECTORY.listFiles()) {
				String name = f.getName();
				int index = name.indexOf(".yml");
				if (index != -1) {
					String aliasName = name.replace(".yml", "");
					ConfigurationReader reader = new ConfigurationReader(SimpleAlias.getTemplateReader(), name, "plugins/SimpleAlias/aliases/");
					if (!reader.readConfiguration()) {
						SimpleAlias.logger().info("Failed to read and convert " + name + "!");
						continue;
					}
					Configuration c = reader.getConfiguration();
					ConfigurationSection generalInformation;
					try {
						generalInformation = GENERAL_INFORMATION.getConfigurationSection(c);
					} catch (SectionNotFoundException e) {
						continue;
					}
					String typeString = generalInformation.getString("Type");
					String description = generalInformation.getString("Description");
					ConfigurationSection executionSettings;
					try {
						executionSettings = EXECUTION_SETTINGS.getConfigurationSection(c);
					} catch (SectionNotFoundException e) {
						continue;
					}
					boolean cooldownEnabled = false;
					int cooldownDuration = 0;
					try {
						ConfigurationSection cooldownSettings = COOLDOWN_SETTINGS.getConfigurationSection(c);
						cooldownEnabled = cooldownSettings.getBoolean("Enabled");
						cooldownDuration = cooldownSettings.getInt("Cooldown");
					} catch (SectionNotFoundException e) {}
					boolean permissionEnabled = false;
					String permissionNode = "";
					try {
						ConfigurationSection permissionSettings = PERMISSION_SETTINGS.getConfigurationSection(c);
						permissionEnabled = permissionSettings.getBoolean("Enabled");
						permissionNode = permissionSettings.getString("Permission");
					} catch (SectionNotFoundException e) {}
					boolean permissionGroupsEnabled = false;
					Set<String> permissionGroups = new HashSet<String>();
					try {
						ConfigurationSection permittedGroups = PERMITTED_GROUPS.getConfigurationSection(c);
						permissionGroupsEnabled = permittedGroups.getBoolean("Enabled");
						if (permissionGroupsEnabled) {
							String permissionGroupsString = permittedGroups.getString("Groups");
							if (permissionGroupsString != null) {
								for (String group : permissionGroupsString.split(", ")) {
									permissionGroups.add(group);
								}
							}
						}
					} catch (SectionNotFoundException e) {}
					File backupFile = new File("plugins/SimpleAlias/aliases/backup " + name);
					try {
						FileUtils.copyFile(f, backupFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					f.delete();
					Alias alias;
					try {
						alias = SimpleAlias.getAliasManager().createAlias(aliasName);
					} catch (Exception e) {
						SimpleAlias.logger().info("Failed to convert " + name + "! Cause: " + e.getMessage());
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						try {
							FileUtils.copyFile(backupFile, f);
						} catch (IOException e1) {
							if (Settings.isDebugEnabled()) {
								e1.printStackTrace();
							}
						}
						backupFile.delete();
						continue;
					}
					if (description != null) {
						alias.setDescription(description);
					}
					alias.setCooldownEnabled(cooldownEnabled);
					if (cooldownEnabled) {
						alias.setCooldownDuration(cooldownDuration);
					}
					alias.setPermissionEnabled(permissionEnabled);
					if (permissionEnabled) {
						alias.setPermissionNode(permissionNode);
					}
					if (permissionGroupsEnabled) {
						alias.setPermissionEnabled(true);
						Set<String> aliasPermissionGroups = alias.getPermissionGroups();
						aliasPermissionGroups.clear();
						aliasPermissionGroups.addAll(permissionGroups);
					}
					List<Action> aliasActions = alias.getActions();
					aliasActions.clear();
					List<String> aliasExecutionOrder = alias.getExecutionOrder();
					aliasExecutionOrder.clear();
					if (typeString.equalsIgnoreCase("Text")) {
						String textString = executionSettings.getString("Lines");
						if (textString == null) {
							SimpleAlias.logger().info("Failed to convert " + name + "!");
							continue;
						}
						String text = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(textString.replace("#", "\n")));
						aliasActions.add(new MessageAction("DisplayMessage", new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashMap<Integer, String>(), 0, false, text, false));
						aliasExecutionOrder.add("DisplayMessage");
					} else if (typeString.equalsIgnoreCase("Single")) {
						String executorString = executionSettings.getString("Executor");
						Executor executor = executorString == null ? Executor.SENDER : Executor.fromName(executorString);
						boolean executableAsConsole = executionSettings.getBoolean("Executable_As_Console");
						alias.setExecutableAsConsole(executableAsConsole);
						String command = executionSettings.getString("Command");
						if (command == null) {
							SimpleAlias.logger().info("Failed to convert " + name + "!");
							continue;
						}
						aliasActions.add(new CommandAction("ExecuteCommand", new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashMap<Integer, String>(), 0, false, command, executor, false));
						aliasExecutionOrder.add("ExecuteCommand");
					} else {
						String executorString = executionSettings.getString("Executor");
						Executor executor = executorString == null ? Executor.SENDER : Executor.fromName(executorString);
						boolean executableAsConsole = executionSettings.getBoolean("Executable_As_Console");
						alias.setExecutableAsConsole(executableAsConsole);
						String commands = executionSettings.getString("Commands");
						if (commands == null) {
							SimpleAlias.logger().info("Failed to convert " + name + "!");
							continue;
						}
						int actionIndex = 1;
						for (String command : commands.split("#")) {
							String actionName = "ExecuteCommand" + actionIndex;
							aliasActions.add(new CommandAction(actionName, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashMap<Integer, String>(), 0, false, command, executor, false));
							aliasExecutionOrder.add(actionName);
							actionIndex++;
						}
					}
					try {
						alias.save();
					} catch (Exception e) {
						SimpleAlias.logger().info("Failed to convert " + name + "! Cause: " + e.getMessage());
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						f.delete();
						try {
							FileUtils.copyFile(backupFile, f);
						} catch (IOException e1) {
							if (Settings.isDebugEnabled()) {
								e1.printStackTrace();
							}
						}
						backupFile.delete();
						continue;
					}
					backupFile.delete();
					amount++;
				}
			}
		if (amount == 0) {
			SimpleAlias.logger().info("No aliases were converted.");
		} else {
			SimpleAlias.logger().info(amount + " alias" + (amount > 1 ? "es were" : " was") + " successfully converted!");
		}
	}
}