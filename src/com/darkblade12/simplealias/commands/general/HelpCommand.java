package com.darkblade12.simplealias.commands.general;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.commands.CommandDetails;
import com.darkblade12.simplealias.commands.ICommand;

@CommandDetails(name = "help", usage = "/sa help [page]", description = "Shows a help page", executableAsConsole = true, permission = "None")
public class HelpCommand implements ICommand {
	@Override
	public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
		int page = 1;
		if (params.length == 1)
			try {
				page = Integer.parseInt(params[0]);
				if (!plugin.commandHandler.helpPage.hasPage(sender, page)) {
					sender.sendMessage("§cThis help page doesn't exist!");
					return;
				}
			} catch (Exception e) {
				sender.sendMessage("§cThe entered number is invalid, please type in a normal number!");
				return;
			}
		plugin.commandHandler.helpPage.showPage(sender, page);
	}
}