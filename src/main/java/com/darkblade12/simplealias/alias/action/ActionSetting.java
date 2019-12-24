package com.darkblade12.simplealias.alias.action;

import java.util.HashMap;
import java.util.Map;

public enum ActionSetting {
	TYPE("Type", Type.class),
	ENABLED_WORLDS("Enabled_Worlds", String.class),
	ENABLED_PERMISSION_NODES("Enabled_Permission_Nodes", String.class),
	ENABLED_PERMISSION_GROUPS("Enabled_Permission_Groups", String.class),
	ENABLED_PARAMS("Enabled_Params", String.class),
	PRIORITY("Priority", Integer.class),
	TRANSLATE_COLOR_CODES("Translate_Color_Codes", Boolean.class),
	COMMAND("Command", String.class),
	EXECUTOR("Executor", Executor.class),
	GRANT_PERMISSION("Grant_Permission", Boolean.class),
	TEXT("Text", String.class),
	BROADCAST("Broadcast", Boolean.class);

	private static final Map<String, ActionSetting> NAME_MAP = new HashMap<String, ActionSetting>();
	private final String path;
	private final Class<?> type;

	private ActionSetting(String path, Class<?> type) {
		this.path = path;
		this.type = type;
	}

	static {
		for (ActionSetting a : values()) {
			NAME_MAP.put(a.name().toLowerCase(), a);
			NAME_MAP.put(a.name().replace("_", "").toLowerCase(), a);
		}
	}

	public static void addNameEntry(String name, ActionSetting setting) {
		if (name != null && setting != null) {
			NAME_MAP.put(name.toLowerCase(), setting);
		}
	}

	public static ActionSetting fromName(String name) {
		return NAME_MAP.get(name.toLowerCase());
	}

	public String getPath() {
		return path;
	}

	public String getFullPath(String action) {
		return "General_Settings.Actions." + action + "." + path;
	}

	public Class<?> getType() {
		return type;
	}
}