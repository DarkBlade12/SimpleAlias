package com.darkblade12.simplealias.permission;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

public enum Permission {
	NONE("None") {
		@Override
		public boolean hasPermission(CommandSender sender) {
			return true;
		}
	},
	SIMPLEALIAS_MASTER("SimpleAlias.*"),
	CREATE_COMMAND("SimpleAlias.create"),
	SINGLE_COMMAND("SimpleAlias.create.single"),
	MULTIPLE_COMMAND("SimpleAlias.create.multiple"),
	MESSAGE_COMMAND("SimpleAlias.create.message"),
	REMOVE_COMMAND("SimpleAlias.remove"),
	RENAME_COMMAND("SimpleAlias.rename"),
	LIST_COMMAND("SimpleAlias.list"),
	DETAILS_COMMAND("SimpleAlias.details"),
	RELOAD_COMMAND("SimpleAlias.reload"),
	USE_MASTER("SimpleAlias.use.*"),
	BYPASS_MASTER("SimpleAlias.bypass.*"),
	BYPASS_ENABLED_WORLDS("SimpleAlias.bypass.enabledworlds"),
	BYPASS_DELAY("SimpleAlias.bypass.delays"),
	BYPASS_COOLDOWN("SimpleAlias.bypass.cooldown"),
	BYPASS_COST("SimpleAlias.bypass.cost");

	private static final Map<String, Permission> NAME_MAP = new HashMap<String, Permission>();
	private static final Map<String, Permission> NODE_MAP = new HashMap<String, Permission>();
	private final String node;

	static {
		for (Permission p : values()) {
			NAME_MAP.put(p.name(), p);
			if (!p.node.equals("None"))
				NODE_MAP.put(p.node, p);
		}
	}

	private Permission(String node) {
		this.node = node;
	}

	public String getNode() {
		return this.node;
	}

	public boolean hasPermission(CommandSender sender) {
		return sender.hasPermission(node) || sender.hasPermission(node.toLowerCase());
	}

	public static Permission fromName(String name) {
		return name == null ? null : NAME_MAP.get(name.toUpperCase());
	}

	public static Permission fromNode(String node) {
		return node == null ? null : NAME_MAP.get(node);
	}

	public static boolean hasPermission(CommandSender sender, String node) {
		return sender.hasPermission(node) || sender.hasPermission(node.toLowerCase());
	}
}