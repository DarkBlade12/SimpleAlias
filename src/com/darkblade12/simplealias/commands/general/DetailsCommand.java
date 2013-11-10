package com.darkblade12.simplealias.commands.general;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.commands.CommandDetails;
import com.darkblade12.simplealias.commands.ICommand;
import com.darkblade12.simplealias.util.StringUtil;

@CommandDetails(name = "details", usage = "/sa details <name>", description = "Shows detailed information about an alias", executableAsConsole = true, permission = "SimpleAlias.details")
public class DetailsCommand implements ICommand {
	@Override
	public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
		String name = StringUtil.stripFirstSlash(params[0]);
		Alias a = plugin.aliasHandler.get(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
			return;
		}
		sender.sendMessage(SimpleAlias.PREFIX + "§eDetailed information about the alias §6" + a.getName() + "§8:" + a);
	}
}