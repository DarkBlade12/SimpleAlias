package com.darkblade12.simplealias.command.alias;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.permission.Permission;
import com.darkblade12.simplealias.util.ColorCode;

@CommandDetails(name = "list", description = "Shows a list of all available aliases", permission = Permission.LIST_COMMAND)
public final class ListCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		AliasManager manager = SimpleAlias.getAliasManager();
		if (manager.getAliasAmount() == 0)
			sender.sendMessage(SimpleAlias.PREFIX + "§cThere are no aliases, yet!");
		else
			sender.sendMessage(SimpleAlias.PREFIX + "§2All available aliases:" + ColorCode.convertToString(manager.getAliases(), '\u25A9'));
	}
}