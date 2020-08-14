package com.darkblade12.simplealias.section.exception;

import com.darkblade12.simplealias.section.IndependantConfigurationSection;

public final class InvalidSectionException extends Exception {
	private static final long serialVersionUID = 7273599698787710310L;

	public InvalidSectionException(String name, IndependantConfigurationSection parent, String description) {
		super("The section '" + name + "' in parent section '" + parent.getName() + "' " + description);
	}
}