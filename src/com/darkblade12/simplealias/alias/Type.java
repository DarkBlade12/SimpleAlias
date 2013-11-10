package com.darkblade12.simplealias.alias;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public enum Type {
	SINGLE("Single"),
	MULTIPLE("Multiple"),
	TEXT("Text");

	private String name;
	private static final Map<String, Type> NAME_MAP = new HashMap<String, Type>();

	static {
		for (Type t : values())
			NAME_MAP.put(t.getName(), t);
	}

	private Type(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public static Type fromName(String name) {
		if (name != null)
			for (Entry<String, Type> e : NAME_MAP.entrySet())
				if (e.getKey().equalsIgnoreCase(name))
					return e.getValue();
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}