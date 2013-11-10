package com.darkblade12.simplealias.commands.general;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.commands.CommandDetails;
import com.darkblade12.simplealias.commands.ICommand;

@CommandDetails(name = "reload", usage = "/sa reload", description = "Reloads the plugin", executableAsConsole = true, permission = "SimpleAlias.reload")
public class ReloadCommand implements ICommand {
	@Override
	public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
		plugin.reload();
		sender.sendMessage(SimpleAlias.PREFIX + "§7Plugin version " + plugin.getDescription().getVersion() + " §7has been reloaded!");
	}
}