package com.darkblade12.simplealias.section;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import com.darkblade12.simplealias.nameable.Nameable;
import com.darkblade12.simplealias.section.exception.SectionNotFoundException;

public final class IndependantConfigurationSection implements Nameable {
	private String path;
	private String name;

	public IndependantConfigurationSection(String path) {
		this.path = path;
		name = path.substring(path.lastIndexOf('.') + 1);
	}

	public IndependantConfigurationSection(IndependantConfigurationSection parent, String name) {
		this(parent.getPath() + "." + name);
	}

	public static IndependantConfigurationSection fromConfigurationSection(ConfigurationSection c) {
		return new IndependantConfigurationSection(c.getCurrentPath());
	}

	public String getPath() {
		return this.path;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public ConfigurationSection getConfigurationSection(Configuration root, boolean validate) throws SectionNotFoundException {
		ConfigurationSection section = root.getConfigurationSection(path);
		if (validate && section == null)
			throw new SectionNotFoundException(this);
		return section;
	}

	public ConfigurationSection getConfigurationSection(Configuration root) throws SectionNotFoundException {
		return getConfigurationSection(root, true);
	}
}