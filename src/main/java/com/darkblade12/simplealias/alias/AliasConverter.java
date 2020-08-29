package com.darkblade12.simplealias.alias;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.action.ActionSetting;
import com.darkblade12.simplealias.plugin.reader.ConfigurationReader;
import com.darkblade12.simplealias.replacer.Replacer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

public final class AliasConverter {
    private static final Replacer VARIABLE_REPLACER;
    private final SimpleAlias plugin;
    private final File backupDirectory;

    static {
        VARIABLE_REPLACER = Replacer.builder()
                                    .with("<alias>", Pattern.CASE_INSENSITIVE, "<alias_name>")
                                    .build();
    }

    public AliasConverter(SimpleAlias plugin) {
        this.plugin = plugin;
        backupDirectory = new File(plugin.getDataFolder(), "/backups");
    }

    public void convertFiles() {
        int count = 0;

        File[] aliasFiles = plugin.getAliasManager().getDataDirectory().listFiles((dir, name) -> ConfigurationReader.isConfiguration(name));
        if (aliasFiles != null) {
            for (File file : aliasFiles) {
                ConfigurationReader<SimpleAlias> reader = new ConfigurationReader<>(plugin, file);
                if (!reader.readConfiguration()) {
                    plugin.logInfo("Failed to read alias file {0}.", file.getName());
                    continue;
                }

                String name = ConfigurationReader.stripExtension(file);
                if (convert(name, reader)) {
                    count++;
                }
            }
        }

        plugin.logInfo("{0} alias{1} been converted!", count, (count == 1 ? " has" : "es have"));
    }

    private boolean convert(String name, ConfigurationReader<?> reader) {
        boolean converted = false;
        FileConfiguration config = reader.getConfig();

        if (config.getString(AliasSetting.CONSOLE_MESSAGE.getPath()) == null) {
            config.set(AliasSetting.CONSOLE_MESSAGE.getPath(), "This alias cannot be executed as console!");
            converted = true;
        }

        if (config.getString(AliasSetting.WORLD_MESSAGE.getPath()) == null) {
            config.set(AliasSetting.WORLD_MESSAGE.getPath(), "&cThis alias is not enabled in your world!");
            converted = true;
        }

        ConfigurationSection actions = config.getConfigurationSection(AliasSection.ACTIONS.getPath());
        if (actions != null) {
            for (String actionName : actions.getKeys(false)) {
                ConfigurationSection action = actions.getConfigurationSection(actionName);
                if (action == null) {
                    continue;
                }

                String text = action.getString("Text");
                if (text != null) {
                    action.set("Text", null);
                    actions.set(ActionSetting.MESSAGE.getPath(), text);
                    converted = true;
                }
            }
        }

        String loggingMessage = config.getString(AliasSetting.LOGGING_MESSAGE.getPath());
        if (loggingMessage != null) {
            config.set(AliasSetting.LOGGING_MESSAGE.getPath(), VARIABLE_REPLACER.replaceAll(loggingMessage));
            converted = true;
        }

        return converted && updateConfig(name, reader);
    }

    private void logFail(String name, String description) {
        plugin.logInfo("Failed to convert alias {0}: {1})", name, description);
    }

    private boolean updateConfig(String name, ConfigurationReader<?> reader) {
        File backup = new File(backupDirectory, name + ".yml");
        if (!reader.copyOutputFile(backup)) {
            logFail(name, "backup file could not be created");
            return false;
        }

        if (!reader.saveConfiguration()) {
            logFail(name, "config could not be saved");
            restoreBackup(backup, reader.getOutputFile());
            return false;
        }

        return true;
    }

    private void restoreBackup(File backup, File target) {
        try {
            Files.copy(backup.toPath(), target.toPath());
        } catch (IOException | SecurityException e) {
            target.delete();
            if (plugin.isDebugEnabled()) {
                e.printStackTrace();
            }
            return;
        }

        backup.delete();
    }
}
