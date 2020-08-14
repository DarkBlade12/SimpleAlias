package com.darkblade12.simplealias.permission;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.command.CommandSender;

public final class PermissionList extends ArrayList<Permission> {
	private static final long serialVersionUID = 6558418046023338567L;

	public PermissionList() {
		super();
	}

	public PermissionList(Collection<Permission> c) {
		super(c);
	}

	public PermissionList(Permission... c) {
		super();
		for (Permission p : c)
			add(p);
	}

	public boolean hasAnyPermission(CommandSender sender) {
		for (int i = 0; i < size(); i++)
			if (get(i).hasPermission(sender))
				return true;
		return false;
	}

	public boolean hasAllPermissions(CommandSender sender) {
		for (int i = 0; i < size(); i++)
			if (!get(i).hasPermission(sender))
				return false;
		return true;
	}
}