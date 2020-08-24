package com.darkblade12.simplealias.alias;

public enum AliasSection {
    GENERAL_SETTINGS("General_Settings"),
    USAGE_CHECK("Usage_Check", GENERAL_SETTINGS),
    ACTIONS("Actions", GENERAL_SETTINGS),
    PERMISSION("Permission", GENERAL_SETTINGS),
    DELAY("Delay", GENERAL_SETTINGS),
    COOLDOWN("Cooldown", GENERAL_SETTINGS),
    COST("Cost", GENERAL_SETTINGS),
    LOGGING("Logging", GENERAL_SETTINGS);

    private final String path;
    private final AliasSection parent;

    AliasSection(String path, AliasSection parent) {
        this.path = path;
        this.parent = parent;
    }

    AliasSection(String path) {
        this(path, null);
    }

    public String getPath() {
        return parent == null ? path : parent.getPath() + "." + path;
    }

    public String getAbsolutePath(String path) {
        return getPath() + "." + path;
    }
}
