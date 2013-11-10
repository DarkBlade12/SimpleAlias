package com.darkblade12.simplealias.commands.general;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.commands.CommandDetails;
import com.darkblade12.simplealias.commands.ICommand;
import com.darkblade12.simplealias.util.StringUtil;

@CommandDetails(name = "aliases", usage = "/sa aliases", description = "Shows a list of aliases", executableAsConsole = true, permission = "SimpleAlias.aliases")
public class AliasesCommand implements ICommand {
	@Override
	public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
		sender.sendMessage(SimpleAlias.PREFIX + "§2List of all active aliases:" + StringUtil.toString(plugin.aliasHandler));
	}
}