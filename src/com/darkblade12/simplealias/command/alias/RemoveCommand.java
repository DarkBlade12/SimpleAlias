package com.darkblade12.simplealias.command.alias;

import net.minecraft.util.org.apache.commons.lang3.StringUtils;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "remove", params = "<name>", description = "Removes an existing alias", permission = Permission.REMOVE_COMMAND)
public final class RemoveCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		AliasManager manager = SimpleAlias.getAliasManager();
		Alias a = manager.getAlias(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
		} else {
			manager.unregister(a);
			sender.sendMessage(SimpleAlias.PREFIX + "§aThe alias §6" + name + " §awas removed.");
		}
	}
}