package com.darkblade12.simplealias.alias;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public enum Executor {
	SENDER("Sender"),
	CONSOLE("Console");

	private String name;
	private static final Map<String, Executor> NAME_MAP = new HashMap<String, Executor>();

	static {
		for (Executor e : values())
			NAME_MAP.put(e.getName(), e);
	}

	private Executor(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public static Executor fromName(String name) {
		if (name != null)
			for (Entry<String, Executor> e : NAME_MAP.entrySet())
				if (e.getKey().equalsIgnoreCase(name))
					return e.getValue();
		return null;
	}

	@Override
	public String toString() {
		return getName();
	}
}