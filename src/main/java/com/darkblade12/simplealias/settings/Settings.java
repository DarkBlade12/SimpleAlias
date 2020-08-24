package com.darkblade12.simplealias.settings;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.AliasSetting;
import com.darkblade12.simplealias.alias.action.ActionSetting;
import com.darkblade12.simplealias.plugin.settings.InvalidValueException;
import com.darkblade12.simplealias.plugin.settings.SettingsBase;
import com.darkblade12.simplealias.util.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Settings extends SettingsBase<SimpleAlias> {
    private boolean debugEnabled;
    private boolean uncommentedTemplate;
    private boolean converterEnabled;
    private boolean commandSync;
    private final Map<String, String> disabledCommands;
    private final Set<String> aliasAbbreviations;
    private final Set<String> actionAbbreviations;

    public Settings(SimpleAlias plugin) {
        super(plugin);
        disabledCommands = new HashMap<>();
        aliasAbbreviations = new HashSet<>();
        actionAbbreviations = new HashSet<>();
    }

    @Override
    public void load() throws InvalidValueException {
        FileConfiguration config = plugin.getConfig();

        debugEnabled = config.getBoolean(Setting.DEBUG_ENABLED.getPath());
        uncommentedTemplate = config.getBoolean(Setting.UNCOMMENTED_TEMPLATE.getPath());
        converterEnabled = config.getBoolean(Setting.CONVERTER_ENABLED.getPath());
        commandSync = config.getBoolean(Setting.COMMAND_SYNC.getPath());

        ConfigurationSection commandSection = config.getConfigurationSection(Section.DISABLED_COMMANDS.getPath());
        if (commandSection != null) {
            for (String command : commandSection.getKeys(false)) {
                String commandName = StringUtils.removeStart(command.toLowerCase(), "/");
                if (disabledCommands.containsKey(commandName)) {
                    plugin.logInfo("Skipping disabled command {0}: multiple entries for the same command", commandName);
                    continue;
                }

                String message = MessageUtils.translateMessage(commandSection.getString(command, ""));
                disabledCommands.put(commandName, message);
            }
        }

        ConfigurationSection aliasSection = config.getConfigurationSection(Section.SETTING_ABBREVIATIONS.getPath());
        if (aliasSection != null) {
            for (String abbreviation : aliasSection.getKeys(false)) {
                String settingKey = aliasSection.getString(abbreviation);
                if (settingKey == null) {
                    plugin.logInfo("Skipping alias setting abbreviation {0}: setting name/path is not set", abbreviation);
                    continue;
                }

                AliasSetting setting = AliasSetting.fromNameOrPath(settingKey);
                if (setting == null) {
                    plugin.logInfo("Skipping alias setting abbreviation {0}: unknown alias setting", abbreviation);
                    continue;
                }

                aliasAbbreviations.add(abbreviation);
                AliasSetting.registerName(abbreviation, setting);
            }
        }

        ConfigurationSection actionSection = config.getConfigurationSection(Section.ACTION_SETTING_ABBREVIATIONS.getPath());
        if (actionSection != null) {
            for (String abbreviation : actionSection.getKeys(false)) {
                String settingName = actionSection.getString(abbreviation);
                if (settingName == null) {
                    plugin.logInfo("Skipping action setting abbreviation {0}: setting name/path is not set", abbreviation);
                    continue;
                }

                ActionSetting setting = ActionSetting.fromName(settingName);
                if (setting == null) {
                    plugin.logInfo("Skipping action setting abbreviation {0}: unknown action setting", abbreviation);
                    continue;
                }

                actionAbbreviations.add(abbreviation);
                ActionSetting.registerName(abbreviation, setting);
            }
        }
    }

    @Override
    public void unload() {
        for (String name : aliasAbbreviations) {
            AliasSetting.unregisterName(name);
        }

        for (String name : actionAbbreviations) {
            ActionSetting.unregisterName(name);
        }

        disabledCommands.clear();
        aliasAbbreviations.clear();
        actionAbbreviations.clear();
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public String getTemplatePath() {
        return uncommentedTemplate ? "template_uncommented.yml" : "template.yml";
    }

    public boolean isConverterEnabled() {
        return converterEnabled;
    }

    public boolean hasCommandSync() {
        return commandSync;
    }

    public boolean isCommandDisabled(String command) {
        return disabledCommands.containsKey(command.toLowerCase());
    }

    public String getDisabledMessage(String command) {
        return disabledCommands.get(command.toLowerCase());
    }
}
