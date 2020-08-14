package com.darkblade12.simplealias.command.alias;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "create", params = "<name>", description = "Creates a new alias with the default settings", permission = Permission.CREATE_COMMAND)
public final class CreateCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		AliasManager manager = SimpleAlias.getAliasManager();
		if (manager.hasAlias(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name already exists!");
		} else if (!Alias.isValid(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cThe name of this alias contains illegal characters!");
		} else {
			try {
				manager.createAlias(name);
				sender.sendMessage(SimpleAlias.PREFIX + "§aThe alias with the name §6" + name + " §awas successfully created.");
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(SimpleAlias.PREFIX + "§cThe alias creation failed! Cause: " + e.getMessage());
			}
		}
	}
}