package com.darkblade12.simplealias.alias.action;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public enum Executor {
	SENDER {
		@Override
		public void dispatchCommand(CommandSender sender, String command, boolean grantPermission) {
			if (sender instanceof Player) {
				Player p = (Player) sender;
				boolean grant = grantPermission && !p.isOp();
				if (grant)
					p.setOp(true);
				try {
					PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(p, "/" + command);
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled())
						Bukkit.dispatchCommand(sender, StringUtils.removeStart(event.getMessage(), "/"));
				} catch (Exception e) {
					/* just for safety */
				}
				if (grant)
					p.setOp(false);
			} else
				CONSOLE.dispatchCommand(sender, command, grantPermission);
		}
	},
	CONSOLE {
		@Override
		public void dispatchCommand(CommandSender sender, String command, boolean grantPermission) {
			if (sender == null || !(sender instanceof ConsoleCommandSender))
				sender = Bukkit.getConsoleSender();
			ServerCommandEvent event = new ServerCommandEvent(sender, command);
			Bukkit.getPluginManager().callEvent(event);
			Bukkit.dispatchCommand(sender, event.getCommand());
		}
	};

	private static final Map<String, Executor> NAME_MAP = new HashMap<String, Executor>();

	static {
		for (Executor e : values())
			NAME_MAP.put(e.name(), e);
	}

	public abstract void dispatchCommand(CommandSender sender, String command, boolean grantPermission);

	public static Executor fromName(String name) {
		return name == null ? null : NAME_MAP.get(name.toUpperCase());
	}
}