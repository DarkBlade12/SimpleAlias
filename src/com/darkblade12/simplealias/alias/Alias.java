package com.darkblade12.simplealias.alias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.Executor;
import com.darkblade12.simplealias.alias.action.Type;
import com.darkblade12.simplealias.alias.action.types.CommandAction;
import com.darkblade12.simplealias.alias.action.types.MessageAction;
import com.darkblade12.simplealias.cooldown.Cooldown;
import com.darkblade12.simplealias.cooldown.CooldownManager;
import com.darkblade12.simplealias.hook.types.VaultHook;
import com.darkblade12.simplealias.nameable.Nameable;
import com.darkblade12.simplealias.nameable.NameableList;
import com.darkblade12.simplealias.permission.Permission;
import com.darkblade12.simplealias.permission.PermissionList;
import com.darkblade12.simplealias.reader.types.ConfigurationReader;
import com.darkblade12.simplealias.section.IndependantConfigurationSection;
import com.darkblade12.simplealias.section.exception.InvalidSectionException;
import com.darkblade12.simplealias.section.exception.InvalidValueException;
import com.darkblade12.simplealias.util.TimeUnit;

public final class Alias implements Nameable, Executable {
	private static final IndependantConfigurationSection GENERAL_SETTINGS = new IndependantConfigurationSection("General_Settings");
	private static final IndependantConfigurationSection USAGE_CHECK = new IndependantConfigurationSection(GENERAL_SETTINGS, "Usage_Check");
	private static final IndependantConfigurationSection ACTIONS = new IndependantConfigurationSection(GENERAL_SETTINGS, "Actions");
	private static final IndependantConfigurationSection PERMISSION = new IndependantConfigurationSection(GENERAL_SETTINGS, "Permission");
	private static final IndependantConfigurationSection DELAY = new IndependantConfigurationSection(GENERAL_SETTINGS, "Delay");
	private static final IndependantConfigurationSection COOLDOWN = new IndependantConfigurationSection(GENERAL_SETTINGS, "Cooldown");
	private static final IndependantConfigurationSection COST = new IndependantConfigurationSection(GENERAL_SETTINGS, "Cost");
	private static final IndependantConfigurationSection LOGGING = new IndependantConfigurationSection(GENERAL_SETTINGS, "Logging");
	private static final PermissionList ENABLED_WORLDS_BYPASS_PERMISSIONS = new PermissionList(Permission.SIMPLEALIAS_MASTER, Permission.BYPASS_MASTER, Permission.BYPASS_ENABLED_WORLDS);
	private static final PermissionList DELAY_BYPASS_PERMISSIONS = new PermissionList(Permission.SIMPLEALIAS_MASTER, Permission.BYPASS_MASTER, Permission.BYPASS_DELAY);
	private static final PermissionList COOLDOWN_BYPASS_PERMISSIONS = new PermissionList(Permission.SIMPLEALIAS_MASTER, Permission.BYPASS_MASTER, Permission.BYPASS_COOLDOWN);
	private static final PermissionList COST_BYPASS_PERMISSIONS = new PermissionList(Permission.SIMPLEALIAS_MASTER, Permission.BYPASS_MASTER, Permission.BYPASS_COST);
	private static final PermissionList USE_PERMISSIONS = new PermissionList(Permission.SIMPLEALIAS_MASTER, Permission.USE_MASTER);
	private static final Pattern ILLEGAL_CHARACTERS = Pattern.compile("[\\s\\/:*?\"<>|#]");
	private String name;
	private final ConfigurationReader configurationReader;
	private String description;
	private Set<String> enabledWorlds;
	private boolean executableAsConsole;
	private boolean usageCheckEnabled;
	private int usageCheckMinParams;
	private int usageCheckMaxParams;
	private String usageCheckMessage;
	private NameableList<Action> actions;
	private List<String> executionOrder;
	private boolean permissionEnabled;
	private String permissionNode;
	private Set<String> permissionGroups;
	private String permissionMessage;
	private boolean delayEnabled;
	private boolean delayCancelOnMove;
	private int delayDuration;
	private String delayMessage;
	private String delayCancelMessage;
	private boolean cooldownEnabled;
	private int cooldownDuration;
	private String cooldownMessage;
	private boolean costEnabled;
	private double costAmount;
	private String costMessage;
	private boolean loggingEnabled;
	private String loggingMessage;
	private AliasCommand command;

	public Alias(String name) throws Exception {
		if (!isValid(name))
			throw new IllegalArgumentException("Name cannot contain illegal characters");
		this.name = name;
		configurationReader = new ConfigurationReader(SimpleAlias.getTemplateReader(), name + ".yml", "plugins/SimpleAlias/aliases/");
		if (!configurationReader.readConfiguration())
			throw new Exception("Failed to read " + configurationReader.getOuputFileName());
		Configuration c = configurationReader.getConfiguration();
		ConfigurationSection generalSettings = GENERAL_SETTINGS.getConfigurationSection(c);
		description = generalSettings.getString("Description");
		if (description == null)
			throw new InvalidValueException("Description", GENERAL_SETTINGS, "is null");
		enabledWorlds = new HashSet<String>();
		String enabledWorldsString = generalSettings.getString("Enabled_Worlds");
		if (enabledWorldsString != null)
			for (String world : enabledWorldsString.split(", ")) {
				World w = Bukkit.getWorld(world);
				if (w == null)
					throw new InvalidValueException("Enabled_Worlds", GENERAL_SETTINGS, "contains the invalid world name '" + world + "'");
				enabledWorlds.add(w.getName());
			}
		executableAsConsole = generalSettings.getBoolean("Executable_As_Console");
		ConfigurationSection usageCheck = USAGE_CHECK.getConfigurationSection(c, false);
		if (usageCheck != null) {
			usageCheckEnabled = usageCheck.getBoolean("Enabled");
			usageCheckMinParams = usageCheck.getInt("Min_Params");
			if (usageCheckEnabled && usageCheckMinParams < 0)
				throw new InvalidValueException("Min_Params", USAGE_CHECK, "is lower than 0");
			usageCheckMaxParams = usageCheck.getInt("Max_Params");
			if (usageCheckEnabled && usageCheckMaxParams < usageCheckMinParams)
				throw new InvalidValueException("Max_Params", USAGE_CHECK, "is invalid (lower than 'Min_Params' value)");
			String message = usageCheck.getString("Message");
			if (usageCheckEnabled && message == null)
				throw new InvalidValueException("Message", USAGE_CHECK, "is null");
			if (message != null)
				usageCheckMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message));
		}
		VaultHook v = SimpleAlias.getVaultHook();
		boolean vaultGroupsEnabled = v.isEnabled() && v.hasPermissionGroupSupport();
		this.actions = new NameableList<Action>(true);
		ConfigurationSection actions = ACTIONS.getConfigurationSection(c);
		for (String action : actions.getKeys(false)) {
			if (this.actions.contains(name))
				throw new InvalidSectionException(action, ACTIONS, "is invalid (duplicate name)");
			ConfigurationSection section = actions.getConfigurationSection(action);
			Type type = Type.fromName(section.getString("Type"));
			if (type == null)
				throw new InvalidSectionException(action, ACTIONS, "is invalid (unknown type)");
			Set<String> worlds = new HashSet<String>();
			String worldsString = section.getString("Enabled_Worlds");
			if (worldsString != null)
				for (String world : worldsString.split(", ")) {
					World w = Bukkit.getWorld(world);
					if(w == null)
						throw new InvalidSectionException(action, ACTIONS, "is invalid ('Enabled_Worlds' contains an invalid world name)");
					worlds.add(w.getName());
				}
			Set<String> nodes = new HashSet<String>();
			String nodesString = section.getString("Enabled_Permission_Nodes");
			if (nodesString != null)
				for (String node : nodesString.split(", "))
					nodes.add(node);
			Set<String> groups = new HashSet<String>();
			String groupsString = section.getString("Enabled_Permission_Groups");
			if (groupsString != null)
				for (String group : groupsString.split(", ")) {
					String exactGroup = v.getExactGroupName(group);
					if (vaultGroupsEnabled && exactGroup == null)
						throw new InvalidSectionException(action, ACTIONS, "is invalid ('Enabled_Permission_Groups' contains an invalid group name)");
					groups.add(vaultGroupsEnabled ? exactGroup : group);
				}
			Map<Integer, String> params = new HashMap<Integer, String>();
			String paramsString = section.getString("Enabled_Params");
			if (paramsString != null)
				for (String param : paramsString.split(", ")) {
					String[] s = param.split("@");
					int index;
					try {
						index = Integer.parseInt(s[1]);
					} catch (Exception e) {
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
						throw new InvalidSectionException(action, ACTIONS, "is invalid (invalid 'Enabled_Params' format)");
					}
					if (params.containsKey(index))
						throw new InvalidSectionException(action, ACTIONS, "is invalid (duplicate index in 'Enabled_Params')");
					params.put(index, s[0]);
				}
			int priority = section.getInt("Priority");
			boolean translateColorCodes = section.getBoolean("Translate_Color_Codes");
			if (type == Type.COMMAND) {
				String command = section.getString("Command");
				if (command == null)
					throw new InvalidSectionException(action, ACTIONS, "is invalid (command is null)");
				Executor executor = Executor.fromName(section.getString("Executor"));
				if (executor == null)
					throw new InvalidSectionException(action, ACTIONS, "is invalid (unknown executor)");
				this.actions.add(new CommandAction(action, worlds, nodes, groups, params, priority, translateColorCodes, StringUtils.removeStart(command, "/"), executor, section.getBoolean("Grant_Permission")));
			} else if (type == Type.MESSAGE) {
				String text;
				if (section.isList("Text")) {
					List<String> lines = section.getStringList("Text");
					if (lines == null || lines.size() == 0)
						throw new InvalidSectionException(action, ACTIONS, "is invalid (text is null)");
					StringBuilder b = new StringBuilder();
					for (String line : lines) {
						if (b.length() > 0)
							b.append("\n");
						b.append(line);
					}
					text = b.toString();
				} else {
					text = section.getString("Text");
					if (text == null)
						throw new InvalidSectionException(action, ACTIONS, "is invalid (text is null)");
				}
				this.actions.add(new MessageAction(action, worlds, nodes, groups, params, priority, translateColorCodes, ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(text)), section.getBoolean("Broadcast")));
			}
		}
		executionOrder = new ArrayList<String>();
		String executionOrderString = generalSettings.getString("Execution_Order");
		if (executionOrderString == null)
			throw new InvalidValueException("Execution_Order", GENERAL_SETTINGS, "is null");
		for (String action : executionOrderString.split(", "))
			if (!this.actions.contains(action))
				throw new InvalidValueException("Execution_Order", GENERAL_SETTINGS, "contains an unkown action name");
			else
				executionOrder.add(action);
		ConfigurationSection permission = PERMISSION.getConfigurationSection(c, false);
		permissionGroups = new HashSet<String>();
		if (permission != null) {
			permissionEnabled = permission.getBoolean("Enabled");
			permissionNode = permission.getString("Node");
			if (permissionEnabled && permissionNode == null)
				throw new InvalidValueException("Node", PERMISSION, "is null");
			String permissionGroupsString = permission.getString("Groups");
			if (permissionGroupsString != null)
				for (String group : permissionGroupsString.split(", ")) {
					String exactGroup = v.getExactGroupName(group);
					if (vaultGroupsEnabled && exactGroup == null)
						throw new InvalidValueException("Groups", PERMISSION, "contains an invalid group name");
					permissionGroups.add(vaultGroupsEnabled ? exactGroup : group);
				}
			String message = permission.getString("Message");
			if (permissionEnabled && message == null)
				throw new InvalidValueException("Message", PERMISSION, "is null");
			if (message != null)
				permissionMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message));
		}
		ConfigurationSection delay = DELAY.getConfigurationSection(c, false);
		if (delay != null) {
			delayEnabled = delay.getBoolean("Enabled");
			delayCancelOnMove = delay.getBoolean("Cancel_On_Move");
			delayDuration = delay.getInt("Duration");
			if (delayEnabled && delayDuration < 1)
				throw new InvalidValueException("Duration", DELAY, "is lower than 1");
			String message = delay.getString("Message");
			if (delayEnabled && message == null)
				throw new InvalidValueException("Message", DELAY, "is null");
			if (message != null)
				delayMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message));
			String cancelMessage = delay.getString("Cancel_Message");
			if (delayEnabled && delayCancelOnMove && cancelMessage == null)
				throw new InvalidValueException("Cancel_Message", DELAY, "is null");
			if (cancelMessage != null)
				delayCancelMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(cancelMessage));
		}
		ConfigurationSection cooldown = COOLDOWN.getConfigurationSection(c, false);
		if (cooldown != null) {
			cooldownEnabled = cooldown.getBoolean("Enabled");
			cooldownDuration = cooldown.getInt("Duration");
			if (cooldownEnabled && cooldownDuration < 1)
				throw new InvalidValueException("Duration", COOLDOWN, "is lower than 1");
			String message = cooldown.getString("Message");
			if (cooldownEnabled && message == null)
				throw new InvalidValueException("Message", COOLDOWN, "is null");
			if (message != null)
				cooldownMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message));
		}
		ConfigurationSection cost = COST.getConfigurationSection(c, false);
		if (cost != null) {
			costEnabled = v.isEnabled() && v.isEconomyEnabled() && cost.getBoolean("Enabled");
			costAmount = cost.getDouble("Amount");
			if (costEnabled && costAmount == 0)
				throw new InvalidValueException("Amount", COST, "is equal to 0");
			else if (costEnabled && costAmount < 0)
				throw new InvalidValueException("Amount", COST, "is lower than 0");
			String message = cost.getString("Message");
			if (costEnabled && message == null)
				throw new InvalidValueException("Message", COST, "is null");
			if (message != null)
				costMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message));
		}
		ConfigurationSection logging = LOGGING.getConfigurationSection(c, false);
		if (logging != null) {
			loggingEnabled = logging.getBoolean("Enabled");
			loggingMessage = logging.getString("Message");
			if (loggingEnabled && loggingMessage == null)
				throw new InvalidValueException("Message", LOGGING, "is null");
		}
		command = new AliasCommand(this);
		if (!command.register())
			throw new Exception("Failed to register the alias as a command");
	}

	public void save() throws Exception {
		Configuration c = configurationReader.getConfiguration();
		ConfigurationSection generalSettings = GENERAL_SETTINGS.getConfigurationSection(c);
		generalSettings.set("Description", description);
		if (enabledWorlds.isEmpty()) {
			generalSettings.set("Enabled_Worlds", null);
		} else {
			String enabledWorldsString = "";
			for (String world : enabledWorlds) {
				if (enabledWorldsString.length() > 0) {
					enabledWorldsString += ", ";
				}
				enabledWorldsString += world;
			}
			generalSettings.set("Enabled_Worlds", enabledWorldsString);
		}
		generalSettings.set("Executable_As_Console", executableAsConsole);
		ConfigurationSection usageCheck = USAGE_CHECK.getConfigurationSection(c, false);
		usageCheck.set("Enabled", usageCheckEnabled);
		usageCheck.set("Min_Params", usageCheckMinParams);
		usageCheck.set("Max_Params", usageCheckMaxParams);
		usageCheck.set("Message", usageCheckMessage == null ? null : StringEscapeUtils.escapeJava(usageCheckMessage.replace('§', '&')));
		ConfigurationSection actions = ACTIONS.getConfigurationSection(c, false);
		if (this.actions.isEmpty()) {
			generalSettings.set("Actions", null);
		} else {
			for (Action action : this.actions) {
				String name = action.getName();
				Type type = action.getType();
				actions.set(name + ".Type", type.name());
				Set<String> worlds = action.getEnabledWorlds();
				actions.set(name + ".Enabled_Worlds", worlds.isEmpty() ? null : StringUtils.join(worlds, ", "));
				Set<String> permissionNodes = action.getEnabledPermissionNodes();
				actions.set(name + ".Enabled_Permission_Nodes", permissionNodes.isEmpty() ? null : StringUtils.join(permissionNodes, ", "));
				Set<String> permissionGroups = action.getEnabledPermissionGroups();
				actions.set(name + ".Enabled_Permission_Groups", permissionGroups.isEmpty() ? null : StringUtils.join(permissionGroups, ", "));
				Map<Integer, String> params = action.getEnabledParams();
				if (params.isEmpty()) {
					actions.set(name + ".Enabled_Params", null);
				} else {
					String paramsString = "";
					for (Entry<Integer, String> param : params.entrySet()) {
						if (paramsString.length() > 0) {
							paramsString += ", ";
						}
						paramsString += param.getValue() + "@" + param.getKey();
					}
					actions.set(name + ".Enabled_Params", paramsString);
				}
				actions.set(name + ".Priority", action.getPriority());
				actions.set(name + ".Translate_Color_Codes", action.getTranslateColorCodes());
				if (type == Type.COMMAND) {
					CommandAction commandAction = (CommandAction) action;
					actions.set(name + ".Command", commandAction.getCommand());
					actions.set(name + ".Executor", commandAction.getExecutor().name());
					actions.set(name + ".Grant_Permission", commandAction.getGrantPermission());
				} else {
					MessageAction messageAction = (MessageAction) action;
					actions.set(name + ".Text", messageAction.getText().replace('§', '&'));
					actions.set(name + ".Broadcast", messageAction.getBroadcast());
				}
			}
			for (String savedAction : actions.getKeys(false)) {
				if (this.actions.contains(savedAction)) {
					continue;
				}
				actions.set(savedAction, null);
			}
		}
		generalSettings.set("Execution_Order", executionOrder.isEmpty() ? null : StringUtils.join(executionOrder, ", "));
		ConfigurationSection permission = PERMISSION.getConfigurationSection(c);
		permission.set("Enabled", permissionEnabled);
		permission.set("Node", permissionNode);
		permission.set("Groups", permissionGroups.isEmpty() ? null : StringUtils.join(permissionGroups, ", "));
		permission.set("Message", permissionMessage == null ? null : StringEscapeUtils.escapeJava(permissionMessage.replace('§', '&')));
		ConfigurationSection delay = DELAY.getConfigurationSection(c, false);
		delay.set("Enabled", delayEnabled);
		delay.set("Cancel_On_Move", delayCancelOnMove);
		delay.set("Duration", delayDuration);
		delay.set("Message", delayMessage == null ? null : StringEscapeUtils.escapeJava(delayMessage.replace('§', '&')));
		delay.set("Cancel_Message", delayCancelMessage == null ? null : StringEscapeUtils.escapeJava(delayCancelMessage.replace('§', '&')));
		ConfigurationSection cooldown = COOLDOWN.getConfigurationSection(c, false);
		cooldown.set("Enabled", cooldownEnabled);
		cooldown.set("Duration", cooldownDuration);
		cooldown.set("Message", cooldownMessage == null ? null : StringEscapeUtils.escapeJava(cooldownMessage.replace('§', '&')));
		ConfigurationSection cost = COST.getConfigurationSection(c, false);
		cost.set("Enabled", costEnabled);
		cost.set("Amount", costAmount);
		cost.set("Message", costMessage == null ? null : StringEscapeUtils.escapeJava(costMessage.replace('§', '&')));
		ConfigurationSection logging = LOGGING.getConfigurationSection(c, false);
		logging.set("Enabled", loggingEnabled);
		logging.set("Message", loggingMessage == null ? null : StringEscapeUtils.escapeJava(loggingMessage));
		configurationReader.saveConfiguration();
	}

	private void executeActions(CommandSender sender, String[] params) {
		for (String name : getExecutionOrder(sender, params))
			actions.get(name).execute(sender, params);
	}

	@Override
	public void execute(final CommandSender sender, final String[] params) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (loggingEnabled)
				Bukkit.getLogger().info(loggingMessage.replace("<player_name>", p.getName()).replace("<alias>", "/" + name + (params.length > 0 ? " " + StringUtils.join(params, " ") : "")));
			if (!isEnabled(p.getWorld()) && !ENABLED_WORLDS_BYPASS_PERMISSIONS.hasAnyPermission(p)) {
				p.sendMessage("§cThis alias isn't enabled in your world!");
				return;
			}
			if (permissionEnabled && !hasPermission(p)) {
				p.sendMessage(permissionMessage);
				return;
			}
			if (usageCheckEnabled) {
				if (params.length < usageCheckMinParams || params.length > usageCheckMaxParams) {
					p.sendMessage(usageCheckMessage);
					return;
				}
			}
			if (cooldownEnabled && !COOLDOWN_BYPASS_PERMISSIONS.hasAnyPermission(p)) {
				CooldownManager manager = SimpleAlias.getCooldownManager();
				Cooldown c = manager.getCooldown(p, name);
				if (c != null)
					if (c.isExpired()) {
						manager.unregister(p, c);
					} else {
						p.sendMessage(cooldownMessage.replace("<remaining_time>", c.getRemainingTimeString()));
						return;
					}
				int duration = cooldownDuration;
				if (delayEnabled && !DELAY_BYPASS_PERMISSIONS.hasAnyPermission(sender))
					duration += delayDuration;
				manager.register(p, Cooldown.fromDuration(name, duration));
			}
			VaultHook v = SimpleAlias.getVaultHook();
			if (costEnabled && !COST_BYPASS_PERMISSIONS.hasAnyPermission(p) && !v.withdrawMoney(p, costAmount)) {
				p.sendMessage(costMessage.replace("<cost_amount>", costAmount + " " + v.getCurrencyName(costAmount)));
				return;
			}
		} else if (!executableAsConsole) {
			sender.sendMessage("§cThis alias can't be executed as console!");
			return;
		}
		if (delayEnabled && !DELAY_BYPASS_PERMISSIONS.hasAnyPermission(sender)) {
			final long durationTicks = delayDuration * 20L;
			final BukkitTask execution = new BukkitRunnable() {
				@Override
				public void run() {
					executeActions(sender, params);
				}
			}.runTaskLater(SimpleAlias.instance(), durationTicks);
			sender.sendMessage(delayMessage.replace("<remaining_time>", TimeUnit.convertToString(delayDuration * 1000)));
			if (sender instanceof Player) {
				final Player p = (Player) sender;
				new BukkitRunnable() {
					private final Location position = delayCancelOnMove ? p.getLocation() : null;
					private int ticks = 0;

					@Override
					public void run() {
						if (ticks > durationTicks) {
							cancel();
						} else if (!p.isOnline()) {
							cancel();
							execution.cancel();
						} else if (delayCancelOnMove) {
							Location l = p.getLocation();
							if (!position.getWorld().getName().equals(l.getWorld().getName()) || position.distanceSquared(l) > 0.1) {
								cancel();
								execution.cancel();
								p.sendMessage(delayCancelMessage);
								if (cooldownEnabled && !COOLDOWN_BYPASS_PERMISSIONS.hasAnyPermission(p)) {
									CooldownManager manager = SimpleAlias.getCooldownManager();
									Cooldown c = manager.getCooldown(p, name);
									if (c != null)
										manager.unregister(p, c);
									manager.register(p, Cooldown.fromDuration(name, cooldownDuration));
								}
							}
						}
						ticks++;
					}
				}.runTaskTimer(SimpleAlias.instance(), 1, 1);
			}
		} else
			executeActions(sender, params);
	}

	public void deleteConfiguration() {
		configurationReader.deleteFile();
	}

	public static boolean isValid(String name) {
		return !ILLEGAL_CHARACTERS.matcher(name).find();
	}

	public void setName(String name) {
		if (!isValid(name))
			throw new IllegalArgumentException("Name cannot contain illegal characters");
		this.name = name;
		configurationReader.setOutputFileName(name + ".yml");
		command.unregister();
		command = new AliasCommand(this);
		command.register();
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setExecutableAsConsole(boolean executableAsConsole) {
		this.executableAsConsole = executableAsConsole;
	}

	public void setEnabledWorlds(Set<String> enabledWorlds) {
		this.enabledWorlds = enabledWorlds;
	}

	public void setExecutionOrder(List<String> executionOrder) {
		this.executionOrder = executionOrder;
	}

	public void setUsageCheckEnabled(boolean usageCheckEnabled) {
		this.usageCheckEnabled = usageCheckEnabled;
	}

	public void setUsageCheckMinParams(int usageCheckMinParams) {
		this.usageCheckMinParams = usageCheckMinParams;
	}

	public void setUsageCheckMaxParams(int usageCheckMaxParams) {
		this.usageCheckMaxParams = usageCheckMaxParams;
	}

	public void setUsageCheckMessage(String usageCheckMessage) {
		this.usageCheckMessage = usageCheckMessage;
	}

	public void setPermissionEnabled(boolean permissionEnabled) {
		this.permissionEnabled = permissionEnabled;
	}

	public void setPermissionNode(String permissionNode) {
		this.permissionNode = permissionNode;
	}

	public void setPermissionGroups(Set<String> permissionGroups) {
		this.permissionGroups = permissionGroups;
	}

	public void setPermissionMessage(String permissionMessage) {
		this.permissionMessage = permissionMessage;
	}

	public void setDelayEnabled(boolean delayEnabled) {
		this.delayEnabled = delayEnabled;
	}

	public void setDelayCancelOnMove(boolean delayCancelOnMove) {
		this.delayCancelOnMove = delayCancelOnMove;
	}

	public void setDelayDuration(int delayDuration) {
		this.delayDuration = delayDuration;
	}

	public void setDelayMessage(String delayMessage) {
		this.delayMessage = delayMessage;
	}

	public void setDelayCancelMessage(String delayCancelMessage) {
		this.delayCancelMessage = delayCancelMessage;
	}

	public void setCooldownEnabled(boolean cooldownEnabled) {
		this.cooldownEnabled = cooldownEnabled;
	}

	public void setCooldownDuration(int cooldownDuration) {
		this.cooldownDuration = cooldownDuration;
	}

	public void setCooldownMessage(String cooldownMessage) {
		this.cooldownMessage = cooldownMessage;
	}

	public void setCostEnabled(boolean costEnabled) {
		this.costEnabled = costEnabled;
	}

	public void setCostAmount(double costAmount) {
		this.costAmount = costAmount;
	}

	public void setCostMessage(String costMessage) {
		this.costMessage = costMessage;
	}

	public void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}

	public void setLoggingMessage(String loggingMessage) {
		this.loggingMessage = loggingMessage;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public Set<String> getEnabledWorlds() {
		return this.enabledWorlds;
	}

	public boolean isEnabled(World w) {
		return enabledWorlds.isEmpty() || enabledWorlds.contains(w.getName());
	}

	public boolean isExecutableAsConsole() {
		return this.executableAsConsole;
	}

	public boolean isUsageCheckEnabled() {
		return this.usageCheckEnabled;
	}

	public int getUsageCheckMinParams() {
		return this.usageCheckMinParams;
	}

	public int getUsageCheckMaxParams() {
		return this.usageCheckMaxParams;
	}

	public String getUsageCheckMessage() {
		return this.usageCheckMessage;
	}

	public NameableList<Action> getActions() {
		return actions;
	}

	public List<Action> getActions(CommandSender sender, String[] params) {
		List<Action> enabled = new ArrayList<Action>();
		Action highestPriority = null;
		for (Action a : actions) {
			if (a.isEnabled(sender, params)) {
				if (highestPriority == null || a.compareTo(highestPriority) == 1) {
					highestPriority = a;
					if (!enabled.isEmpty())
						enabled.clear();
					enabled.add(a);
				} else if (a.compareTo(highestPriority) == 0)
					enabled.add(a);
			}
		}
		return Collections.unmodifiableList(enabled);
	}

	public Action getAction(String name) {
		return actions.get(name);
	}

	public boolean hasAction(String name) {
		return actions.contains(name);
	}

	public List<String> getExecutionOrder() {
		return executionOrder;
	}

	public List<String> getExecutionOrder(CommandSender sender, String[] params) {
		List<String> order = new ArrayList<String>();
		Action highestPriority = null;
		for (String name : executionOrder) {
			Action a = actions.get(name);
			if (a.isEnabled(sender, params)) {
				if (highestPriority == null || a.compareTo(highestPriority) == 1) {
					highestPriority = a;
					if (!order.isEmpty())
						order.clear();
					order.add(name);
				} else if (a.compareTo(highestPriority) == 0)
					order.add(name);
			}
		}
		return Collections.unmodifiableList(order);
	}

	public boolean isPermissionEnabled() {
		return this.permissionEnabled;
	}

	public String getPermissionNode() {
		return this.permissionNode;
	}

	public Set<String> getPermissionGroups() {
		return permissionGroups;
	}

	public boolean hasPermission(Player p) {
		return !permissionEnabled || Permission.hasPermission(p, permissionNode) || USE_PERMISSIONS.hasAnyPermission(p) || SimpleAlias.getVaultHook().isInAnyGroup(p, permissionGroups);
	}

	public String getPermissionMessage() {
		return this.permissionMessage;
	}

	public boolean isDelayEnabled() {
		return this.delayEnabled;
	}

	public boolean getDelayCancelOnMove() {
		return this.delayCancelOnMove;
	}

	public int getDelayDuration() {
		return this.delayDuration;
	}

	public String getDelayMessage() {
		return this.delayMessage;
	}

	public String getDelayCancelMessage() {
		return this.delayCancelMessage;
	}

	public boolean isCooldownEnabled() {
		return this.cooldownEnabled;
	}

	public long getCooldownDuration() {
		return this.cooldownDuration;
	}

	public String getCooldownMessage() {
		return this.cooldownMessage;
	}

	public boolean isCostEnabled() {
		return this.costEnabled;
	}

	public double getCostAmount() {
		return this.costAmount;
	}

	public String getCostMessage() {
		return this.costMessage;
	}

	public boolean isLoggingEnabled() {
		return this.loggingEnabled;
	}

	public String getLoggingMessage() {
		return this.loggingMessage;
	}

	public AliasCommand getCommand() {
		return command;
	}

	public String getDetails() {
		StringBuilder b = new StringBuilder();
		b.append("\n§r §8\u25A9 §7§lDescription: §a" + description);
		b.append("\n§r §8\u25A9 §7§lExecutable as Console: §a" + executableAsConsole);
		if (!enabledWorlds.isEmpty())
			b.append("\n§r §8\u25A9 §7§lEnabled Worlds: §a" + StringUtils.join(enabledWorlds, ", "));
		b.append("\n§r §8\u25A9 §7§lUsage Check:");
		b.append("\n§r  §3\u2022 §b§lEnabled: §a" + usageCheckEnabled);
		if (usageCheckEnabled) {
			b.append("\n§r  §3\u2022 §b§lMin Params: §a" + usageCheckMinParams);
			b.append("\n§r  §3\u2022 §b§lMax Params: §a" + usageCheckMaxParams);
			b.append("\n§r  §3\u2022 §b§lMessage: §r" + usageCheckMessage);
		}
		b.append("\n§r §8\u25A9 §7§lActions:");
		for (Action action : actions) {
			b.append("\n§r  §3\u2022 §b§l" + action.getName() + ":");
			Type t = action.getType();
			b.append("\n§r    §1\u25BB §9§lType: §a" + t.name());
			Set<String> actionEnabledWorlds = action.getEnabledWorlds();
			if (!actionEnabledWorlds.isEmpty())
				b.append("\n§r    §1\u25BB §9§lEnabled Worlds: §a" + StringUtils.join(actionEnabledWorlds, ", "));
			Set<String> actionEnabledPermissionNodes = action.getEnabledPermissionNodes();
			if (!actionEnabledPermissionNodes.isEmpty())
				b.append("\n§r    §1\u25BB §9§lEnabled Permission Nodes: §a" + StringUtils.join(actionEnabledPermissionNodes, ", "));
			Set<String> actionEnabledPermissionGroups = action.getEnabledPermissionGroups();
			if (!actionEnabledPermissionGroups.isEmpty())
				b.append("\n§r    §1\u25BB §9§lEnabled Permission Groups: §a" + StringUtils.join(actionEnabledPermissionGroups, ", "));
			Map<Integer, String> enabledParams = action.getEnabledParams();
			if (!enabledParams.isEmpty()) {
				String enabledParamsString = "";
				for (Entry<Integer, String> e : enabledParams.entrySet()) {
					if (enabledParamsString.length() > 0) {
						enabledParamsString += ", ";
					}
					enabledParamsString += e.getValue() + "@" + e.getKey();
				}
				b.append("\n§r    §1\u25BB §9§lEnabled Params: §a" + enabledParamsString);
			}
			b.append("\n§r    §1\u25BB §9§lPriority: §a" + action.getPriority());
			b.append("\n§r    §1\u25BB §9§lTranslate Color Codes: §a" + action.getTranslateColorCodes());
			if (t == Type.MESSAGE) {
				MessageAction message = (MessageAction) action;
				b.append("\n§r    §1\u25BB §9§lText: §r" + message.getText());
				b.append("\n§r    §1\u25BB §9§lBroadcast: §a" + message.getBroadcast());
			} else {
				CommandAction command = (CommandAction) action;
				b.append("\n§r    §1\u25BB §9§lCommand: §a" + command.getCommand());
				b.append("\n§r    §1\u25BB §9§lExecutor: §a" + command.getExecutor().name());
				b.append("\n§r    §1\u25BB §9§lGrant Permission: §a" + command.getGrantPermission());
			}
		}
		b.append("\n§r §8\u25A9 §7§lExecution Order: §a" + StringUtils.join(executionOrder, ", "));
		b.append("\n§r §8\u25A9 §7§lPermission:");
		b.append("\n§r   §3\u2022 §b§lEnabled: §a" + permissionEnabled);
		if (permissionEnabled) {
			b.append("\n§r   §3\u2022 §b§lNode: §a" + permissionNode);
			if (!permissionGroups.isEmpty())
				b.append("\n§r   §3\u2022 §b§lGroups: §a" + StringUtils.join(permissionGroups, ", "));
			b.append("\n§r   §3\u2022 §b§lMessage: §r" + permissionMessage);
		}
		b.append("\n§r §8\u25A9 §7§lDelay:");
		b.append("\n§r   §3\u2022 §b§lEnabled: §a" + delayEnabled);
		if (delayEnabled) {
			b.append("\n§r   §3\u2022 §b§lCancel on Move: §a" + delayCancelOnMove);
			b.append("\n§r   §3\u2022 §b§lDuration: §a" + delayDuration);
			b.append("\n§r   §3\u2022 §b§lMessage: §r" + delayMessage);
			b.append("\n§r   §3\u2022 §b§lCancel Message: §r" + delayCancelMessage);
		}
		b.append("\n§r §8\u25A9 §7§lCooldown:");
		b.append("\n§r   §3\u2022 §b§lEnabled: §a" + cooldownEnabled);
		if (cooldownEnabled) {
			b.append("\n§r   §3\u2022 §b§lDuration: §a" + cooldownDuration);
			b.append("\n§r   §3\u2022 §b§lMessage: §r" + cooldownMessage);
		}
		b.append("\n§r §8\u25A9 §7§lCost:");
		b.append("\n§r   §3\u2022 §b§lEnabled: §a" + costEnabled);
		if (costEnabled) {
			b.append("\n§r   §3\u2022 §b§lAmount: §a" + costAmount);
			b.append("\n§r   §3\u2022 §b§lMessage: §r" + costMessage);
		}
		b.append("\n§r §8\u25A9 §7§lLogging:");
		b.append("\n§r   §3\u2022 §b§lEnabled: §a" + loggingEnabled);
		if (loggingEnabled) {
			b.append("\n§r   §3\u2022 §b§lMessage: §r" + loggingMessage);
		}
		return b.toString();
	}
}