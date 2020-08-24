package com.darkblade12.simplealias.settings;

import com.darkblade12.simplealias.plugin.settings.SettingInfo;
import com.darkblade12.simplealias.util.MessageUtils;

enum Setting implements SettingInfo {
    DEBUG_ENABLED("Debug_Enabled", Section.GENERAL_SETTINGS),
    UNCOMMENTED_TEMPLATE("Uncommented_Template", Section.GENERAL_SETTINGS),
    CONVERTER_ENABLED("Converter_Enabled", Section.GENERAL_SETTINGS),
    COMMAND_SYNC("Command_Sync", Section.GENERAL_SETTINGS),
    DISABLED_COMMANDS("Disabled_Commands", Section.GENERAL_SETTINGS);

    private final String path;

    Setting(String path, Section section) {
        this.path = section.getPath() + "." + path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return MessageUtils.formatName(this, true, "_");
    }
}
