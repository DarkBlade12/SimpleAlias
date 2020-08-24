package com.darkblade12.simplealias.alias;

import com.darkblade12.simplealias.plugin.settings.SettingInfo;
import com.darkblade12.simplealias.util.MessageUtils;
import org.apache.commons.lang.NullArgumentException;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum AliasSetting implements SettingInfo {
    DESCRIPTION("Description", AliasSection.GENERAL_SETTINGS),
    EXECUTABLE_AS_CONSOLE("Executable_As_Console", AliasSection.GENERAL_SETTINGS),
    CONSOLE_MESSAGE("Console_Message", AliasSection.GENERAL_SETTINGS),
    ENABLED_WORLDS("Enabled_Worlds", AliasSection.GENERAL_SETTINGS, ModifyOperation.ADD),
    WORLD_MESSAGE("World_Message", AliasSection.GENERAL_SETTINGS),
    EXECUTION_ORDER("Execution_Order", AliasSection.GENERAL_SETTINGS, ModifyOperation.ADD),
    USAGE_CHECK_ENABLED("Enabled", AliasSection.USAGE_CHECK),
    USAGE_CHECK_MIN_PARAMS("Min_Params", AliasSection.USAGE_CHECK),
    USAGE_CHECK_MAX_PARAMS("Max_Params", AliasSection.USAGE_CHECK),
    USAGE_CHECK_MESSAGE("Message", AliasSection.USAGE_CHECK),
    PERMISSION_ENABLED("Enabled", AliasSection.PERMISSION),
    PERMISSION_NODE("Node", AliasSection.PERMISSION),
    PERMISSION_GROUPS("Groups", AliasSection.PERMISSION, ModifyOperation.ADD),
    PERMISSION_MESSAGE("Message", AliasSection.PERMISSION),
    DELAY_ENABLED("Enabled", AliasSection.DELAY),
    DELAY_CANCEL_ON_MOVE("Cancel_On_Move", AliasSection.DELAY),
    DELAY_DURATION("Duration", AliasSection.DELAY),
    DELAY_MESSAGE("Message", AliasSection.DELAY),
    DELAY_CANCEL_MESSAGE("Cancel_Message", AliasSection.DELAY),
    COOLDOWN_ENABLED("Enabled", AliasSection.COOLDOWN),
    COOLDOWN_DURATION("Duration", AliasSection.COOLDOWN),
    COOLDOWN_MESSAGE("Message", AliasSection.COOLDOWN),
    COST_ENABLED("Enabled", AliasSection.COST),
    COST_AMOUNT("Amount", AliasSection.COST),
    COST_MESSAGE("Message", AliasSection.COST),
    LOGGING_ENABLED("Enabled", AliasSection.LOGGING),
    LOGGING_MESSAGE("Message", AliasSection.LOGGING);

    private static final Map<String, AliasSetting> BY_NAME = new HashMap<>();
    private static final Map<String, AliasSetting> BY_PATH = new HashMap<>();
    private final String path;
    private final Set<ModifyOperation> supportedOperations;

    AliasSetting(String path, AliasSection section, ModifyOperation... supportedOperations) {
        this.path = section.getAbsolutePath(path);
        this.supportedOperations = EnumSet.of(ModifyOperation.SET, supportedOperations);
    }

    static {
        for (AliasSetting setting : values()) {
            String name = setting.name().toLowerCase();
            BY_NAME.put(name, setting);
            BY_PATH.put(setting.path.toLowerCase(), setting);

            if (name.contains("_")) {
                BY_NAME.put(name.replace("_", ""), setting);
            }
        }
    }

    public static void registerName(String name, AliasSetting setting) {
        if (setting == null) {
            throw new NullArgumentException("setting");
        }

        String key = name.toLowerCase();
        if (BY_NAME.containsKey(key)) {
            throw new IllegalArgumentException("This name is already registered.");
        }

        BY_NAME.put(key, setting);
    }

    public static void unregisterName(String name) {
        if (Arrays.stream(values()).anyMatch(s -> s.name().equalsIgnoreCase(name) || s.name().replace("_", "").equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("Cannot unregister a default name.");
        }

        BY_NAME.remove(name.toLowerCase());
    }

    public static AliasSetting fromName(String name) {
        return BY_NAME.get(name.toLowerCase());
    }

    public static AliasSetting fromNameOrPath(String key) {
        String lowerKey = key.toLowerCase();
        return BY_NAME.containsKey(lowerKey) ? BY_NAME.get(lowerKey) : BY_PATH.get(lowerKey);
    }

    public static Set<String> getNames() {
        return BY_NAME.keySet();
    }

    public String getPath() {
        return path;
    }

    public Set<ModifyOperation> getSupportedOperations() {
        return Collections.unmodifiableSet(supportedOperations);
    }

    public boolean isSupported(ModifyOperation operation) {
        return supportedOperations.contains(operation);
    }

    @Override
    public String toString() {
        return MessageUtils.formatName(this, true, "_");
    }
}
