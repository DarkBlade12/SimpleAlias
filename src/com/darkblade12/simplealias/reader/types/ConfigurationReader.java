package com.darkblade12.simplealias.reader.types;

import org.bukkit.configuration.file.YamlConfiguration;

import com.darkblade12.simplealias.reader.Reader;

public final class ConfigurationReader extends Reader<YamlConfiguration> {
	private ConfigurationTemplateReader templateReader;
	private YamlConfiguration configuration;

	public ConfigurationReader(String resourceFileName, String outputFileName, String outputPath) {
		super(resourceFileName, outputFileName, outputPath);
	}

	public ConfigurationReader(String fileName, String outputPath) {
		super(fileName, fileName, outputPath);
	}

	public ConfigurationReader(ConfigurationTemplateReader templateReader, String fileName, String outputPath) {
		this(fileName, outputPath);
		this.templateReader = templateReader;
	}

	@Override
	public YamlConfiguration readFromFile() {
		if (isOutputFileReadable()) {
			try {
				return YamlConfiguration.loadConfiguration(outputFile);
			} catch (Exception e) {
				return null;
			}
		} else
			return null;
	}

	public boolean readConfiguration() {
		return (configuration = readFromFile()) != null;
	}

	@Override
	public boolean saveToFile(YamlConfiguration data) {
		try {
			data.save(outputFile);
			if (!configuration.equals(data))
				configuration = data;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean saveConfiguration() {
		return saveToFile(configuration);
	}

	@Override
	public boolean isOutputFileReadable() {
		return super.isOutputFileReadable() || (templateReader == null ? saveResourceFile() : templateReader.copyFile(outputFile));
	}

	public ConfigurationTemplateReader getTemplateReader() {
		return this.templateReader;
	}

	public YamlConfiguration getConfiguration() {
		return this.configuration;
	}
}