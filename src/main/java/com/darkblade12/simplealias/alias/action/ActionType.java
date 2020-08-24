package com.darkblade12.simplealias.alias.action;

import java.util.HashMap;
import java.util.Map;

public enum ActionType {
    COMMAND,
    MESSAGE;

    private static final Map<String, ActionType> NAME_MAP = new HashMap<>();

    static {
        for (ActionType type : values()) {
            NAME_MAP.put(type.name(), type);
        }
    }

    public static ActionType fromName(String name) {
        return name == null ? null : NAME_MAP.get(name.toUpperCase());
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
