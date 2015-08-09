package com.darkblade12.simplealias.alias;

import java.util.HashMap;
import java.util.Map;

public enum Setting {
	DESCRIPTION("General_Settings.Description", String.class),
	EXECUTABLE_AS_CONSOLE("General_Settings.Executable_As_Console", Boolean.class),
	ENABLED_WORLDS("General_Settings.Enabled_Worlds", String.class),
	EXECUTION_ORDER("General_Settings.Execution_Order", String.class),
	USAGE_CHECK_ENABLED("General_Settings.Usage_Check.Enabled", Boolean.class),
	USAGE_CHECK_MIN_PARAMS("General_Settings.Usage_Check.Min_Params", Integer.class),
	USAGE_CHECK_MAX_PARAMS("General_Settings.Usage_Check.Max_Params", Integer.class),
	USAGE_CHECK_MESSAGE("General_Settings.Usage_Check.Message", String.class),
	PERMISSION_ENABLED("General_Settings.Permission.Enabled", Boolean.class),
	PERMISSION_NODE("General_Settings.Permission.Node", String.class),
	PERMISSION_GROUPS("General_Settings.Permission.Groups", String.class),
	PERMISSION_MESSAGE("General_Settings.Permission.Message", String.class),
	DELAY_ENABLED("General_Settings.Delay.Enabled", Boolean.class),
	DELAY_CANCEL_ON_MOVE("General_Settings.Delay.Cancel_On_Move", Boolean.class),
	DELAY_DURATION("General_Settings.Delay.Duration", Integer.class),
	DELAY_MESSAGE("General_Settings.Delay.Message", String.class),
	DELAY_CANCEL_MESSAGE("General_Settings.Delay.Cancel_Message", String.class),
	COOLDOWN_ENABLED("General_Settings.Cooldown.Enabled", Boolean.class),
	COOLDOWN_DURATION("General_Settings.Cooldown.Duration", Integer.class),
	COOLDOWN_MESSAGE("General_Settings.Cooldown.Message", String.class),
	COST_ENABLED("General_Settings.Cost.Enabled", Boolean.class),
	COST_AMOUNT("General_Settings.Cost.Amount", Double.class),
	COST_MESSAGE("General_Settings.Cost.Message", String.class),
	LOGGING_ENABLED("General_Settings.Logging.Enabled", Boolean.class),
	LOGGING_MESSAGE("General_Settings.Logging.Message", String.class);

	private static final Map<String, Setting> NAME_MAP = new HashMap<String, Setting>();
	private final String path;
	private final Class<?> type;

	private Setting(String path, Class<?> type) {
		this.path = path;
		this.type = type;
	}

	static {
		for (Setting s : values()) {
			NAME_MAP.put(s.name().toLowerCase(), s);
			NAME_MAP.put(s.name().replace("_", "").toLowerCase(), s);
			NAME_MAP.put(s.path.toLowerCase(), s);
		}
	}

	public static void addNameEntry(String name, Setting setting) {
		if (name != null && setting != null) {
			NAME_MAP.put(name.toLowerCase(), setting);
		}
	}

	public static Setting fromName(String name) {
		return NAME_MAP.get(name.toLowerCase());
	}

	public String getPath() {
		return path;
	}

	public Class<?> getType() {
		return type;
	}
}