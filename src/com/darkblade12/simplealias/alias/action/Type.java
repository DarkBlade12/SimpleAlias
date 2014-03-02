package com.darkblade12.simplealias.alias.action;

import java.util.HashMap;
import java.util.Map;

public enum Type {
	COMMAND,
	MESSAGE;

	private static final Map<String, Type> NAME_MAP = new HashMap<String, Type>();

	static {
		for (Type t : values())
			NAME_MAP.put(t.name(), t);
	}

	public static Type fromName(String name) {
		return name == null ? null : NAME_MAP.get(name.toUpperCase());
	}
}