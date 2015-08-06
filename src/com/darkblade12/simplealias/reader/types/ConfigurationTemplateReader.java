package com.darkblade12.simplealias.reader.types;

import org.bukkit.configuration.file.YamlConfiguration;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.reader.Reader;

public final class ConfigurationTemplateReader extends Reader<YamlConfiguration> {
	public ConfigurationTemplateReader(String resourceFileName, String outputFileName, String outputPath) {
		super(resourceFileName, outputFileName, outputPath);
	}

	public ConfigurationTemplateReader(String fileName, String outputPath) {
		super(fileName, fileName, outputPath);
	}

	@Override
	public YamlConfiguration readFromFile() {
		if (isOutputFileReadable()) {
			try {
				return YamlConfiguration.loadConfiguration(outputFile);
			} catch (Exception e) {
				if(Settings.isDebugEnabled()) {
					e.printStackTrace();
				}
				return null;
			}
		} else
			return null;
	}

	public boolean readConfigurationTemplate() {
		return readFromFile() != null;
	}

	@Override
	public boolean saveToFile(YamlConfiguration data) {
		try {
			data.save(outputFile);
			return true;
		} catch (Exception e) {
			if(Settings.isDebugEnabled()) {
				e.printStackTrace();
			}
			return false;
		}
	}

	@Override
	public boolean isOutputFileReadable() {
		return super.isOutputFileReadable() || saveResourceFile();
	}
}