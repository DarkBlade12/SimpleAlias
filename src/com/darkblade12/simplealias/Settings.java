package com.darkblade12.simplealias;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.darkblade12.simplealias.alias.Setting;
import com.darkblade12.simplealias.section.IndependantConfigurationSection;
import com.darkblade12.simplealias.section.exception.InvalidValueException;
import com.darkblade12.simplealias.section.exception.SectionNotFoundException;

public final class Settings {
	private static final IndependantConfigurationSection GENERAL_SETTINGS = new IndependantConfigurationSection("General_Settings");
	private static final IndependantConfigurationSection DISABLED_COMMANDS = new IndependantConfigurationSection(GENERAL_SETTINGS, "Disabled_Commands");
	private static final IndependantConfigurationSection SETTING_ABBREVIATIONS = new IndependantConfigurationSection(GENERAL_SETTINGS, "Setting_Abbreviations");
	private static boolean debugEnabled;
	private static boolean uncommentedTemplate;
	private static boolean converterEnabled;
	private static Map<String, String> disabledCommands;

	private Settings() {}

	public static void load() throws SectionNotFoundException, InvalidValueException {
		Configuration c = SimpleAlias.instance().loadConfig();
		ConfigurationSection generalSettings = GENERAL_SETTINGS.getConfigurationSection(c);
		debugEnabled = generalSettings.getBoolean("Debug_Enabled");
		uncommentedTemplate = generalSettings.getBoolean("Uncommented_Template");
		converterEnabled = generalSettings.getBoolean("Converter_Enabled");
		Settings.disabledCommands = new HashMap<String, String>();
		ConfigurationSection disabledCommands = DISABLED_COMMANDS.getConfigurationSection(c, false);
		if (disabledCommands != null) {
			for (String command : disabledCommands.getKeys(false)) {
				String finalCommand = StringUtils.removeStart(command.toLowerCase(), "/");
				String message = disabledCommands.getString(command);
				if (message == null) {
					SimpleAlias.logger().info("Skipping disabled command '" + finalCommand + "'. Cause: message is null");
				} else {
					if (Settings.disabledCommands.containsKey(finalCommand)) {
						SimpleAlias.logger().info("Skipping disabled command '" + finalCommand + "'. Cause: duplicate entry for this command");
					} else {
						Settings.disabledCommands.put(finalCommand, ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message)));
					}
				}
			}
		}
		ConfigurationSection settingAbbreviations = SETTING_ABBREVIATIONS.getConfigurationSection(c, false);
		if (settingAbbreviations != null) {
			for (String abbreviation : settingAbbreviations.getKeys(false)) {
				String setting = settingAbbreviations.getString(abbreviation);
				if (setting == null) {
					SimpleAlias.logger().info("Skipping setting abbreviation '" + abbreviation + "'. Cause: setting name/path is null");
				} else {
					Setting s = Setting.fromName(setting);
					if (s == null) {
						SimpleAlias.logger().info("Skipping setting abbreviation '" + abbreviation + "'. Cause: setting name/path is invalid");
					} else {
						Setting.addNameEntry(abbreviation, s);
					}
				}
			}
		}
	}

	public static void reload() throws SectionNotFoundException, InvalidValueException {
		SimpleAlias.instance().reloadConfig();
		load();
	}

	public static boolean isDebugEnabled() {
		return debugEnabled;
	}

	public static boolean getUncommentedTemplate() {
		return uncommentedTemplate;
	}

	public static boolean isConverterEnabled() {
		return converterEnabled;
	}

	public static Map<String, String> getDisabledCommands() {
		return disabledCommands;
	}

	public static boolean isCommandDisabled(String command) {
		return disabledCommands.containsKey(command.toLowerCase());
	}

	public static String getDisabledMessage(String command) {
		return disabledCommands.get(command.toLowerCase());
	}
}