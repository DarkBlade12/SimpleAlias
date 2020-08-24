package com.darkblade12.simplealias.settings;

enum Section {
    GENERAL_SETTINGS("General_Settings"),
    DISABLED_COMMANDS("Disabled_Commands", GENERAL_SETTINGS),
    SETTING_ABBREVIATIONS("Setting_Abbreviations", GENERAL_SETTINGS),
    ACTION_SETTING_ABBREVIATIONS("Action_Setting_Abbreviations", GENERAL_SETTINGS);

    private final String path;
    private final Section parent;

    Section(String path, Section parent) {
        this.path = path;
        this.parent = parent;
    }

    Section(String path) {
        this(path, null);
    }

    public String getPath() {
        return parent == null ? path : parent.getPath() + "." + path;
    }
}
