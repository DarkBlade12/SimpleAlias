package com.darkblade12.simplealias.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CommandDetails {
	public abstract String name();

	public abstract String usage();

	public abstract String description();

	public abstract boolean executableAsConsole();

	public abstract String permission();
}