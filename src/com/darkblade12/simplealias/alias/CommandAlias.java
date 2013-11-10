package com.darkblade12.simplealias.alias;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.cooldown.Cooldown;
import com.darkblade12.simplealias.hook.FactionsHook;
import com.darkblade12.simplealias.hook.VaultHook;
import com.darkblade12.simplealias.util.StringUtil;

public abstract class CommandAlias extends Alias {
	protected Executor executor;
	protected boolean executableAsConsole;
	protected String message;
	protected boolean cooldownEnabled;
	protected long cooldown;

	protected CommandAlias(SimpleAlias plugin, String name, Type type, String description, Executor executor, boolean executableAsConsole, String message, boolean cooldownEnabled, long cooldown,
			boolean permissionEnabled, String permission, boolean permittedGroupsEnabled, Set<String> permittedGroups) {
		super(plugin, name, type, description, permissionEnabled, permission, permittedGroupsEnabled, permittedGroups);
		this.executor = executor;
		this.executableAsConsole = executableAsConsole;
		this.message = message;
		this.cooldownEnabled = cooldownEnabled;
		this.cooldown = cooldown;
	}

	protected String prepareCommand(CommandSender s, String command, String[] args) {
		String c = command;
		for (int i = 0; i < args.length; i++)
			c = c.replace("<args:" + (i + 1) + ">", args[i]);
		if (s instanceof Player) {
			Player p = (Player) s;
			c = c.replace("<world_name>", p.getWorld().getName()).replace("<sender_group>", VaultHook.getGroup(p)).replace("<balance>", VaultHook.getBalance(p) + "")
					.replace("<faction>", FactionsHook.getFaction(p));
		}
		return c.replace("<args>", StringUtils.join(args, " ")).replace("<sender_name>", s.getName()).replaceAll(ARGUMENT_PATTERN, "");
	}

	protected void performCommand(CommandSender sender, String command) {
		if (sender instanceof Player) {
			if (executor == Executor.SENDER) {
				Player p = (Player) sender;
				boolean grant = !p.isOp();
				if (grant)
					p.setOp(true);
				try {
					PlayerCommandPreprocessEvent e = new PlayerCommandPreprocessEvent(p, "/" + command);
					Bukkit.getPluginManager().callEvent(e);
					if (!e.isCancelled())
						p.performCommand(StringUtil.stripFirstSlash(e.getMessage()));
				} catch (Exception ex) {
					// should not get here, just for safety
				}
				if (grant)
					p.setOp(false);
			} else {
				CommandSender c = Bukkit.getConsoleSender();
				ServerCommandEvent e = new ServerCommandEvent(c, "/" + command);
				Bukkit.getPluginManager().callEvent(e);
				Bukkit.dispatchCommand(c, StringUtil.stripFirstSlash(e.getCommand()));
			}
		} else {
			ServerCommandEvent e = new ServerCommandEvent(sender, "/" + command);
			Bukkit.getPluginManager().callEvent(e);
			Bukkit.dispatchCommand(sender, StringUtil.stripFirstSlash(e.getCommand()));
		}
	}

	public Cooldown createCooldown() {
		return new Cooldown(name, cooldown >= 0 ? System.currentTimeMillis() + cooldown * 1000 : -1);
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
		setConfigValue("Execution_Settings.Executor", executor.getName());
	}

	public void setExecutableAsConsole(boolean executableAsConsole) {
		this.executableAsConsole = executableAsConsole;
		setConfigValue("Execution_Settings.Executable_As_Console", executableAsConsole);
	}

	public void setMessage(String message) {
		this.message = message;
		setConfigValue("Execution_Settings.Message", message);
	}

	public void setCooldownEnabled(boolean cooldownEnabled) {
		this.cooldownEnabled = cooldownEnabled;
		setConfigValue("Cooldown_Settings.Enabled", cooldownEnabled);
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
		setConfigValue("Cooldown_Settings.Cooldown", cooldown);
	}

	public Executor getExecutor() {
		return this.executor;
	}

	public boolean isExecutableAsConsole() {
		return this.executableAsConsole;
	}

	public String getMessage() {
		return this.message;
	}

	public boolean isCooldownEnabled() {
		return this.cooldownEnabled;
	}

	public long getCooldown() {
		return this.cooldown;
	}
}