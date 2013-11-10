package com.darkblade12.simplealias.commands;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;

public abstract interface ICommand {
	public abstract void execute(SimpleAlias plugin, CommandSender sender, String[] params);
}