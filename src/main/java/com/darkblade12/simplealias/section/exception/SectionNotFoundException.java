package com.darkblade12.simplealias.section.exception;

import com.darkblade12.simplealias.section.IndependantConfigurationSection;

public final class SectionNotFoundException extends Exception {
	private static final long serialVersionUID = -4982432447190032314L;

	public SectionNotFoundException(String name, String path) {
		super("Cannot find section '" + name + "' (Path: '" + path + "')");
	}

	public SectionNotFoundException(IndependantConfigurationSection section) {
		this(section.getName(), section.getPath());
	}
}