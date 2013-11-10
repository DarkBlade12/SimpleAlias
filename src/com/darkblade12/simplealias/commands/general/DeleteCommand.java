package com.darkblade12.simplealias.commands.general;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.commands.CommandDetails;
import com.darkblade12.simplealias.commands.ICommand;
import com.darkblade12.simplealias.util.StringUtil;

@CommandDetails(name = "delete", usage = "/sa delete <name>", description = "Deletes an existing alias", executableAsConsole = true, permission = "SimpleAlias.delete")
public class DeleteCommand implements ICommand {
	@Override
	public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
		String name = StringUtil.stripFirstSlash(params[0]);
		Alias a = plugin.aliasHandler.get(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
			return;
		}
		plugin.aliasHandler.unregister(a);
		sender.sendMessage(SimpleAlias.PREFIX + "§aYou've deleted the alias §6" + name + "§a.");
	}
}