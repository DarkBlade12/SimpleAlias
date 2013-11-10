package com.darkblade12.simplealias.commands.general;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.commands.CommandDetails;
import com.darkblade12.simplealias.commands.ICommand;
import com.darkblade12.simplealias.util.StringUtil;

@CommandDetails(name = "rename", usage = "/sa rename <name> <new_name>", description = "Renames an alias", executableAsConsole = true, permission = "SimpleAlias.rename")
public class RenameCommand implements ICommand {
	@Override
	public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
		String name = StringUtil.stripFirstSlash(params[0]);
		Alias a = plugin.aliasHandler.get(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
			return;
		}
		String newName = StringUtil.stripFirstSlash(params[1]);
		a.setName(newName);
		sender.sendMessage(SimpleAlias.PREFIX + "§aThe alias §6" + name + " §awas renamed to §e" + newName + "§a.");
	}
}