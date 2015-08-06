package com.darkblade12.simplealias;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.darkblade12.simplealias.section.IndependantConfigurationSection;
import com.darkblade12.simplealias.section.exception.InvalidValueException;
import com.darkblade12.simplealias.section.exception.SectionNotFoundException;

public final class Settings {
	private static final IndependantConfigurationSection GENERAL_SETTINGS = new IndependantConfigurationSection("General_Settings");
	private static boolean debugEnabled;
	private static boolean uncommentedTemplate;
	private static boolean converterEnabled;

	private Settings() {}

	public static void load() throws SectionNotFoundException, InvalidValueException {
		Configuration c = SimpleAlias.instance().loadConfig();
		ConfigurationSection generalSettings = GENERAL_SETTINGS.getConfigurationSection(c);
		debugEnabled = generalSettings.getBoolean("Debug_Enabled");
		uncommentedTemplate = generalSettings.getBoolean("Uncommented_Template");
		converterEnabled = generalSettings.getBoolean("Converter_Enabled");
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
}