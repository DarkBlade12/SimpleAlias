package com.darkblade12.simplealias.plugin.settings;

public class InvalidValueException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidValueException(String path, Object value, String description) {
        super(String.format("The value '%s' is invalid for setting '%s': %s", value, path, description));
    }

    public InvalidValueException(SettingInfo setting, Object value, String description) {
        this(setting.getPath(), value, description);
    }

    public InvalidValueException(String path, String description) {
        super(String.format("The value of setting '%s' is invalid: %s", path, description));
    }

    public InvalidValueException(SettingInfo setting, String description) {
        super(String.format("The value of setting '%s' is invalid: %s", setting.getPath(), description));
    }

    public InvalidValueException(String message) {
        super(message);
    }
}
