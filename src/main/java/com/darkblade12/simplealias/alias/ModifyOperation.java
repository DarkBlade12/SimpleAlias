package com.darkblade12.simplealias.alias;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum ModifyOperation {
    SET,
    ADD;

    private static final Map<String, ModifyOperation> BY_NAME = new HashMap<>();

    static {
        for (ModifyOperation operation : values()) {
            BY_NAME.put(operation.name().toLowerCase(), operation);
        }
    }

    public static ModifyOperation fromName(String name) {
        return BY_NAME.get(name.toLowerCase());
    }

    public static Set<String> getNames() {
        return BY_NAME.keySet();
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
