package com.darkblade12.simplealias.command;

import org.bukkit.command.CommandSender;

public abstract interface ICommand {
	public abstract void execute(CommandHandler handler, CommandSender sender, String label, String[] params);
}