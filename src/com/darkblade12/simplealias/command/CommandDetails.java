package com.darkblade12.simplealias.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.darkblade12.simplealias.permission.Permission;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandDetails {
	public abstract String name();

	public abstract String params() default "";

	public abstract String description() default "No description set";

	public abstract boolean executableAsConsole() default true;

	public abstract Permission permission() default Permission.NONE;

	public abstract boolean infiniteParams() default false;
}