package com.darkblade12.simplealias.alias.action;

import com.darkblade12.simplealias.alias.AliasSection;
import com.darkblade12.simplealias.alias.ModifyOperation;
import com.darkblade12.simplealias.plugin.settings.SettingInfo;
import com.darkblade12.simplealias.util.MessageUtils;
import org.apache.commons.lang.NullArgumentException;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum ActionSetting implements SettingInfo {
    TYPE("Type"),
    ENABLED_WORLDS("Enabled_Worlds", ModifyOperation.ADD),
    ENABLED_PERMISSION_NODES("Enabled_Permission_Nodes", ModifyOperation.ADD),
    ENABLED_PERMISSION_GROUPS("Enabled_Permission_Groups", ModifyOperation.ADD),
    ENABLED_PARAMS("Enabled_Params", ModifyOperation.ADD),
    PRIORITY("Priority"),
    TRANSLATE_COLOR_CODES("Translate_Color_Codes"),
    COMMAND("Command"),
    EXECUTOR("Executor"),
    GRANT_PERMISSION("Grant_Permission"),
    SILENT("Silent"),
    MESSAGE("Message"),
    BROADCAST("Broadcast");

    private static final Map<String, ActionSetting> BY_NAME = new HashMap<>();
    private final String path;
    private final Set<ModifyOperation> supportedOperations;

    ActionSetting(String path, ModifyOperation... supportedOperations) {
        this.path = path;
        this.supportedOperations = EnumSet.of(ModifyOperation.SET, supportedOperations);
    }

    static {
        for (ActionSetting setting : values()) {
            String name = setting.name().toLowerCase();
            BY_NAME.put(name, setting);

            if (name.contains("_")) {
                BY_NAME.put(name.replace("_", ""), setting);
            }
        }
    }

    public static void registerName(String name, ActionSetting setting) {
        if (setting == null) {
            throw new NullArgumentException("setting");
        }

        String key = name.toLowerCase();
        if (BY_NAME.containsKey(key)) {
            throw new IllegalArgumentException("This name is already registered");
        }

        BY_NAME.put(key, setting);
    }

    public static void unregisterName(String name) {
        if (Arrays.stream(values()).anyMatch(s -> s.name().equalsIgnoreCase(name) || s.name().replace("_", "").equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("Cannot unregister a default name.");
        }

        BY_NAME.remove(name.toLowerCase());
    }

    public static ActionSetting fromName(String name) {
        return BY_NAME.get(name.toLowerCase());
    }

    public static Set<String> getNames() {
        return BY_NAME.keySet();
    }

    public String getPath() {
        return path;
    }

    public String getAbsolutePath(String action) {
        return AliasSection.ACTIONS.getPath() + "." + action + "." + path;
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
