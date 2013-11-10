package com.darkblade12.simplealias.alias;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.types.MultipleAlias;
import com.darkblade12.simplealias.alias.types.SingleAlias;
import com.darkblade12.simplealias.alias.types.TextAlias;
import com.darkblade12.simplealias.hook.VaultHook;
import com.darkblade12.simplealias.loader.ConfigLoader;
import com.darkblade12.simplealias.nameable.Nameable;
import com.darkblade12.simplealias.util.StringUtil;

public abstract class Alias implements Nameable {
	protected final static String DEFAULT_PERMISSION = "SimpleAlias.alias.<name>";
	protected final static String DEFAULT_PERMISSION_PATTERN = "SimpleAlias\\.alias\\..*";
	protected final static String ARGUMENT_PATTERN = " <.*?>";
	protected SimpleAlias plugin;
	protected ConfigLoader loader;
	protected YamlConfiguration config;
	protected String name;
	protected Type type;
	protected String description;
	protected boolean permissionEnabled;
	protected String permission;
	protected boolean permittedGroupsEnabled;
	protected Set<String> permittedGroups;

	protected Alias(SimpleAlias plugin, String name, Type type, String description, boolean permissionEnabled, String permission, boolean permittedGroupsEnabled, Set<String> permittedGroups) {
		this.plugin = plugin;
		loader = new ConfigLoader(plugin, name + ".yml", true);
		loader.loadConfig();
		config = loader.getConfig();
		this.name = name;
		this.type = type;
		this.description = description;
		this.permissionEnabled = permissionEnabled;
		this.permission = permission;
		this.permittedGroupsEnabled = permittedGroupsEnabled;
		this.permittedGroups = permittedGroups;
	}

	public static Alias fromFile(SimpleAlias plugin, String name) throws Exception {
		ConfigLoader l = new ConfigLoader(plugin, name + ".yml", true);
		if (!l.loadConfig())
			throw new Exception("Failed to load the alias config file");
		YamlConfiguration c = l.getConfig();
		Type type = Type.fromName(c.getString("General_Information.Type"));
		if (type == null)
			throw new Exception("Invalid alias type");
		String description = c.getString("General_Information.Description");
		boolean permissionEnabled = c.getBoolean("Permission_Settings.Enabled");
		String permission = c.getString("Permission_Settings.Permission");
		boolean permittedGroupsEnabled = c.getBoolean("Permitted_Groups.Enabled");
		Set<String> permittedGroups = StringUtil.asSet(c.getString("Permitted_Groups.Groups"), ", ");
		if (type == Type.TEXT) {
			String linesString = c.getString("Execution_Settings.Lines");
			if (linesString == null)
				throw new Exception("Lines can't be null");
			List<String> lines = StringUtil.asList(ChatColor.translateAlternateColorCodes('&', linesString), "#");
			return new TextAlias(plugin, name, lines, description, permissionEnabled, permission, permittedGroupsEnabled, permittedGroups);
		} else {
			Executor executor = Executor.fromName(c.getString("Execution_Settings.Executor"));
			if (executor == null)
				throw new Exception("Invalid executor name");
			boolean executableAsConsole = c.getBoolean("Execution_Settings.Executable_As_Console");
			String message = c.getString("Execution_Settings.Message");
			if (message != null)
				message = ChatColor.translateAlternateColorCodes('&', message);
			boolean cooldownEnabled = c.getBoolean("Cooldown_Settings.Enabled");
			long cooldown = c.getLong("Cooldown_Settings.Cooldown");
			if (type == Type.SINGLE) {
				String command = c.getString("Execution_Settings.Command");
				if (command == null)
					throw new Exception("Command can't be null");
				boolean disableCommand = c.getBoolean("Execution_Settings.Disable_Command");
				String disableMessage = c.getString("Execution_Settings.Disable_Message");
				if (disableCommand && disableMessage == null)
					throw new Exception("Disable message can't be null");
				return new SingleAlias(plugin, name, command, disableCommand, ChatColor.translateAlternateColorCodes('&', disableMessage), description, executor, executableAsConsole, message, cooldownEnabled,
						cooldown, permissionEnabled, permission, permittedGroupsEnabled, permittedGroups);
			} else {
				List<String> commands = StringUtil.asList(c.getString("Execution_Settings.Commands"), "#");
				if (commands.size() == 0)
					throw new Exception("Commands can't be null");
				return new MultipleAlias(plugin, name, commands, description, executor, executableAsConsole, message, cooldownEnabled, cooldown, permissionEnabled, permission, permittedGroupsEnabled,
						permittedGroups);
			}
		}
	}

	public void saveToFile() {
		config.set("General_Information.Type", type.getName());
		config.set("General_Information.Description", description);
		if (type == Type.TEXT) {
			config.set("Execution_Settings.Lines", StringUtils.join(((TextAlias) this).getLines(), "#").replace('§', '&'));
		} else {
			CommandAlias c = (CommandAlias) this;
			config.set("Execution_Settings.Executor", c.getExecutor().getName());
			config.set("Execution_Settings.Executable_As_Console", c.isExecutableAsConsole());
			String message = c.getMessage();
			config.set("Execution_Settings.Message", message == null ? null : message.replace('§', '&'));
			config.set("Cooldown_Settings.Enabled", c.isCooldownEnabled());
			config.set("Cooldown_Settings.Cooldown", c.getCooldown());
			if (type == Type.SINGLE) {
				SingleAlias s = (SingleAlias) this;
				config.set("Execution_Settings.Command", s.getCommand());
				config.set("Execution_Settings.Disable_Command", s.getDisableCommand());
				String disableMessage = s.getDisableMessage();
				config.set("Execution_Settings.Disable_Message", disableMessage == null ? null : disableMessage.replace('§', '&'));
			} else {
				config.set("Execution_Settings.Commands", StringUtils.join(((MultipleAlias) this).getCommands(), "#"));
			}
		}
		config.set("Permission_Settings.Enabled", permissionEnabled);
		config.set("Permission_Settings.Permission", permission);
		config.set("Permitted_Groups.Enabled", permittedGroupsEnabled);
		config.set("Permitted_Groups.Groups", permittedGroups != null ? StringUtils.join(permittedGroups, ", ") : null);
		loader.saveConfig(config);
	}

	public void execute(CommandSender sender, String[] args) {
		if (sender instanceof Player)
			Bukkit.getLogger().info(sender.getName() + " issued alias: /" + name + (args.length > 0 ? " " + StringUtils.join(args, " ") : ""));
	}

	public void deleteFile(SimpleAlias plugin) {
		loader.deleteConfig();
	}

	protected void setConfigValue(String section, Object value) {
		config.set(section, value);
		loader.saveConfig(config);
	}

	public void setName(String name) {
		ConfigLoader newLoader = new ConfigLoader(plugin, name + ".yml", true);
		loader.getOuputFile().renameTo(newLoader.getOuputFile());
		loader = newLoader;
		this.name = name;
		if (permission.matches(DEFAULT_PERMISSION_PATTERN))
			setPermission(DEFAULT_PERMISSION.replace("<name>", name));
	}

	public void setDescription(String description) {
		this.description = description;
		setConfigValue("General_Information.Description", description);
	}

	public void setPermissionEnabled(boolean permissionEnabled) {
		this.permissionEnabled = permissionEnabled;
		setConfigValue("Permission_Settings.Enabled", permissionEnabled);
	}

	public void setPermission(String permission) {
		this.permission = permission;
		setConfigValue("Permission_Settings.Permission", permission);
	}

	public void setPermittedGroupsEnabled(boolean permittedGroupsEnabled) {
		this.permittedGroupsEnabled = permittedGroupsEnabled;
		setConfigValue("Permitted_Groups.Enabled", permittedGroupsEnabled);
	}

	public void setPermittedGroups(Set<String> permittedGroups) {
		this.permittedGroups = permittedGroups;
		setConfigValue("Permitted_Groups.Groups", permittedGroups != null ? StringUtils.join(permittedGroups, ", ") : null);
	}

	public void addPermittedGroups(String... permittedGroups) {
		for (String group : permittedGroups)
			this.permittedGroups.add(group);
		setConfigValue("Permitted_Groups.Groups", permittedGroups != null ? StringUtils.join(permittedGroups, ", ") : null);
	}

	@Override
	public String getName() {
		return this.name;
	}

	public Type getType() {
		return this.type;
	}

	public String getDescription() {
		return this.description;
	}

	public boolean isPermissionEnabled() {
		return this.permissionEnabled;
	}

	public String getPermission() {
		return this.permission;
	}

	public boolean isPermittedGroupsEnabled() {
		return this.permittedGroupsEnabled;
	}

	public Set<String> getPermittedGroups() {
		return this.permittedGroups;
	}

	public boolean hasPermission(CommandSender sender) {
		return !permissionEnabled || sender.hasPermission(permission) || sender.hasPermission(SimpleAlias.MASTER_PERMISSION) || sender.hasPermission(SimpleAlias.ALIAS_MASTER_PERMISSION)
				|| permittedGroupsEnabled && sender instanceof Player && permittedGroups.contains(VaultHook.getGroup((Player) sender));
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("\n§r §8\u25A9 §7§oType: §a" + type);
		b.append("\n§r §8\u25A9 §7§oDescription: §a" + (description != null && description.length() > 0 ? description : "§4§oNone"));
		if (type == Type.TEXT) {
			b.append("\n§r §8\u25A9 §7§oLines:");
			for (String line : ((TextAlias) this).getLines())
				b.append("\n§r  §2\u25A9 §r" + line);
		} else {
			CommandAlias c = (CommandAlias) this;
			String message = c.getMessage();
			b.append("\n§r §8\u25A9 §7§oExecutor: §a" + c.getExecutor() + "\n§r §8\u25A9 §7§oExecutable as Console: §a" + c.isExecutableAsConsole() + "\n§r §8\u25A9 §7§oMessage: §r"
					+ (message != null && message.length() > 0 ? message : "§4§oNone"));
			boolean cooldownEnabled = c.isCooldownEnabled();
			b.append("\n§r §8\u25A9 §7§oCooldown Enabled: §a" + c.isCooldownEnabled());
			if (cooldownEnabled) {
				long cooldown = c.getCooldown();
				b.append("\n§r §8\u25A9 §7§oCooldown: §a" + (cooldown >= 0 ? cooldown + " seconds" + (cooldown > 60 ? " §8(§c" + StringUtil.parse(cooldown * 1000) + "§8)" : "") : "infinite"));
			}
			if (type == Type.SINGLE) {
				SingleAlias s = (SingleAlias) this;
				b.append("\n§r §8\u25A9 §7§oCommand: §a" + s.getCommand());
				boolean disableCommand = s.getDisableCommand();
				b.append("\n§r §8\u25A9 §7§oDisable Command: §a" + disableCommand);
				if (disableCommand) {
					String disableMessage = s.getDisableMessage();
					b.append("\n§r §8\u25A9 §7§oDisable Message: §r" + (disableMessage != null && disableMessage.length() > 0 ? disableMessage : "§4§oNone"));
				}
			} else {
				b.append("\n§r §8\u25A9 §7§oCommands:");
				for (String command : ((MultipleAlias) this).getCommands())
					b.append("\n§r  §2\u25A9 " + command);
			}
		}
		b.append("\n§r §8\u25A9 §7§oPermission Enabled: §a" + permissionEnabled);
		if (permissionEnabled)
			b.append("\n§r §8\u25A9 §7§oPermission: §a" + permission);
		b.append("\n§r §8\u25A9 §7§oPermitted Groups Enabled: §a" + permittedGroupsEnabled);
		if (permittedGroupsEnabled)
			b.append("\n§r §8\u25A9 §7§oPermitted Groups: §a" + StringUtils.join(permittedGroups, "§2, §a"));
		return b.toString();
	}
}