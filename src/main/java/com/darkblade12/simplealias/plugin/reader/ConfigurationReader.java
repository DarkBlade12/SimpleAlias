package com.darkblade12.simplealias.plugin.reader;

import com.darkblade12.simplealias.plugin.PluginBase;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class ConfigurationReader<T extends PluginBase> extends Reader<T, FileConfiguration> {
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("\\.yml$", Pattern.CASE_INSENSITIVE);
    private FileConfiguration config;

    public ConfigurationReader(T plugin, String resourcePath, File outputFile) {
        super(plugin, resourcePath, outputFile);
    }

    public ConfigurationReader(T plugin, File outputFile) {
        super(plugin, outputFile.getName(), outputFile);
    }

    public static boolean isConfiguration(String fileName) {
        return FILE_EXTENSION_PATTERN.matcher(fileName).find();
    }

    public static boolean isConfiguration(File file) {
        return isConfiguration(file.getName());
    }

    public static String stripExtension(String fileName) {
        return FILE_EXTENSION_PATTERN.matcher(fileName).replaceFirst("");
    }

    public static String stripExtension(File file) {
        return stripExtension(file.getName());
    }

    @Override
    public FileConfiguration read() {
        if (!outputFile.exists()) {
            return null;
        }

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(outputFile);
            return config;
        } catch (IOException | InvalidConfigurationException e) {
            if (plugin.isDebugEnabled()) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean save(FileConfiguration config) {
        try {
            config.save(outputFile);
            if (!config.equals(this.config)) {
                this.config = config;
            }
            return true;
        } catch (IOException e) {
            if (plugin.isDebugEnabled()) {
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean readConfiguration() {
        return (config = read()) != null;
    }

    public boolean saveConfiguration() {
        return save(config);
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
