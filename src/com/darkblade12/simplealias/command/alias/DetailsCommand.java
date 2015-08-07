package com.darkblade12.simplealias.command.alias;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "details", params = "<name>", description = "Shows detailed information about an alias", permission = Permission.DETAILS_COMMAND)
public final class DetailsCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		Alias a = SimpleAlias.getAliasManager().getAlias(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
		} else {
			sender.sendMessage(SimpleAlias.PREFIX + "§aDetailed information about the alias §6" + name + "§a:" + a.getDetails());
		}
	}
}