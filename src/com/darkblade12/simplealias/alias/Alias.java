package com.darkblade12.simplealias.alias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.minecraft.util.org.apache.commons.lang3.StringEscapeUtils;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;

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
	private static final IndependantConfigurationSection LOGGING = new IndependantConfigurationSection("Logging");
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
			for (String world : enabledWorldsString.split(", "))
				enabledWorlds.add(world);
		else
			for (World w : Bukkit.getWorlds())
				enabledWorlds.add(w.getName());
		executableAsConsole = generalSettings.getBoolean("Executable_As_Console");
		ConfigurationSection usageCheck = USAGE_CHECK.getConfigurationSection(c, false);
		if (usageCheck != null) {
			usageCheckEnabled = usageCheck.getBoolean("Enabled");
			if (usageCheckEnabled) {
				usageCheckMinParams = usageCheck.getInt("Min_Params");
				if (usageCheckMinParams < 0)
					throw new InvalidValueException("Min_Params", USAGE_CHECK, "is invalid (lower than 0)");
				usageCheckMaxParams = usageCheck.getInt("Max_Params");
				if (usageCheckMaxParams < usageCheckMinParams)
					throw new InvalidValueException("Max_Params", USAGE_CHECK, "is invalid (lower than 'Min_Params' value)");
				usageCheckMessage = usageCheck.getString("Message");
				if (usageCheckMessage == null)
					throw new InvalidValueException("Message", USAGE_CHECK, "is null");
				usageCheckMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(usageCheckMessage));
			}
		}
		this.actions = new NameableList<Action>();
		ConfigurationSection actions = ACTIONS.getConfigurationSection(c, false);
		if (actions != null) {
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
					for (String world : worldsString.split(", "))
						worlds.add(world);
				Set<String> nodes = new HashSet<String>();
				String nodesString = section.getString("Enabled_Permission_Nodes");
				if (nodesString != null)
					for (String node : nodesString.split(", "))
						nodes.add(node);
				Set<String> groups = new HashSet<String>();
				String groupsString = section.getString("Enabled_Permission_Groups");
				if (groupsString != null)
					for (String group : groupsString.split(", "))
						groups.add(group);
				Map<Integer, String> params = new HashMap<Integer, String>();
				String paramsString = section.getString("Enabled_Params");
				if (paramsString != null)
					for (String param : paramsString.split(", ")) {
						String[] s = param.split("@");
						int index;
						try {
							index = Integer.parseInt(s[1]);
						} catch (Exception e) {
							throw new InvalidSectionException(action, ACTIONS, "is invalid (enabled params list format)");
						}
						if (params.containsKey(index))
							throw new InvalidSectionException(action, ACTIONS, "is invalid (enabled params duplicate index)");
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
					this.actions.add(new CommandAction(action, worlds, nodes, groups, params, priority, translateColorCodes, StringUtils.removeStart(command, "/"), executor, section
							.getBoolean("Grant_Permission")));
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
					this.actions.add(new MessageAction(action, worlds, nodes, groups, params, priority, translateColorCodes, ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(text)),
							section.getBoolean("Broadcast")));
				}
			}
		}
		executionOrder = new ArrayList<String>();
		String executionOrderString = generalSettings.getString("Execution_Order");
		if (executionOrderString != null)
			for (String action : executionOrderString.split(", "))
				if (!this.actions.contains(action))
					throw new InvalidValueException("Execution_Order", GENERAL_SETTINGS, "contains an unkown action name");
				else
					executionOrder.add(action);
		ConfigurationSection permission = PERMISSION.getConfigurationSection(c, false);
		VaultHook v = SimpleAlias.getVaultHook();
		if (permission != null) {
			permissionEnabled = permission.getBoolean("Enabled");
			if (permissionEnabled) {
				permissionNode = permission.getString("Node");
				if (permissionNode == null)
					throw new InvalidValueException("Node", PERMISSION, "is null");
				permissionGroups = new HashSet<String>();
				String permissionGroupsString = permission.getString("Groups");
				if (permissionGroupsString != null)
					for (String group : permissionGroupsString.split(", "))
						permissionGroups.add(group);
				String message = permission.getString("Message");
				if (message == null)
					throw new InvalidValueException("Message", PERMISSION, "is null");
				permissionMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message));
			}
		}
		ConfigurationSection delay = DELAY.getConfigurationSection(c, false);
		if (delay != null) {
			delayEnabled = delay.getBoolean("Enabled");
			if (delayEnabled) {
				delayCancelOnMove = delay.getBoolean("Cancel_On_Move");
				delayDuration = delay.getInt("Duration");
				if (delayDuration < 1)
					throw new InvalidValueException("Duration", DELAY, "is invalid (lower than 1)");
				String message = delay.getString("Message");
				if (message == null)
					throw new InvalidValueException("Message", DELAY, "is null");
				delayMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message));
				String cancelMessage = delay.getString("Cancel_Message");
				if (cancelMessage == null)
					throw new InvalidValueException("Cancel_Message", DELAY, "is null");
				delayCancelMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(cancelMessage));
			}
		}
		ConfigurationSection cooldown = COOLDOWN.getConfigurationSection(c, false);
		if (cooldown != null) {
			cooldownEnabled = cooldown.getBoolean("Enabled");
			if (cooldownEnabled) {
				cooldownDuration = cooldown.getInt("Duration");
				if (cooldownDuration < 1)
					throw new InvalidValueException("Duration", COOLDOWN, "is invalid (lower than 1)");
				String message = cooldown.getString("Message");
				if (message == null)
					throw new InvalidValueException("Message", COOLDOWN, "is null");
				cooldownMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message));
			}
		}
		ConfigurationSection cost = COST.getConfigurationSection(c, false);
		if (cost != null) {
			costEnabled = v.isEnabled() && v.isEconomyEnabled() && cost.getBoolean("Enabled");
			if (costEnabled) {
				costAmount = cost.getDouble("Amount");
				if (costAmount == 0)
					throw new InvalidValueException("Amount", COST, "is invalid (equals 0)");
				else if (costAmount < 0)
					throw new InvalidValueException("Amount", COST, "is invalid (lower than 0)");
				String message = cost.getString("Message");
				if (message == null)
					throw new InvalidValueException("Message", COST, "is null");
				costMessage = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message));
			}
		}
		ConfigurationSection logging = LOGGING.getConfigurationSection(c, false);
		if (logging != null) {
			loggingEnabled = logging.getBoolean("Enabled");
			if (loggingEnabled) {
				loggingMessage = logging.getString("Message");
				if (loggingMessage == null)
					throw new InvalidValueException("Message", LOGGING, "is null");
			}
		}
		command = new AliasCommand(this);
		if (!command.register())
			throw new Exception("Failed to register the alias as a command");
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
				manager.register(p, Cooldown.fromDuration(name, cooldownDuration));
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
		return enabledWorlds.contains(w.getName());
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

	public List<Action> getActions() {
		return Collections.unmodifiableList(actions);
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

	public List<String> getExecutionOrder() {
		return Collections.unmodifiableList(executionOrder);
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
		return Collections.unmodifiableSet(permissionGroups);
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
}