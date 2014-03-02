package com.darkblade12.simplealias.alias.action.types;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.Type;
import com.darkblade12.simplealias.util.ColorCode;

public final class MessageAction extends Action {
	private final String text;
	private final boolean broadcast;

	public MessageAction(String name, Set<String> enabledWorlds, Set<String> enabledPermissionNodes, Set<String> enabledPermissionGroups, Map<Integer, String> enabledParams, int priority,
			boolean translateColorCodes, String text, boolean broadcast) {
		super(name, enabledWorlds, enabledPermissionNodes, enabledPermissionGroups, enabledParams, priority, translateColorCodes);
		this.text = text;
		this.broadcast = broadcast;
	}

	@Override
	public void execute(CommandSender sender, String[] params) {
		String message = applyReplacement(text, sender, translateColorCodes ? ColorCode.translateAlternateColorCodes('&', params) : params);
		if (broadcast)
			Bukkit.broadcastMessage(message);
		else
			sender.sendMessage(message);
	}

	@Override
	public Type getType() {
		return Type.MESSAGE;
	}

	public String getText() {
		return this.text;
	}

	public boolean getBroadcast() {
		return this.broadcast;
	}
}