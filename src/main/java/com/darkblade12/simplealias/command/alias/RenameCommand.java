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

@CommandDetails(name = "rename", params = "<name> <new_name>", description = "Renames an existing alias", permission = Permission.RENAME_COMMAND)
public final class RenameCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		AliasManager manager = SimpleAlias.getAliasManager();
		Alias a = manager.getAlias(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
		} else {
			String newName = StringUtils.removeStart(params[1], "/");
			if (manager.hasAlias(newName)) {
				sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name already exists!");
			} else if (!Alias.isValid(newName)) {
				sender.sendMessage(SimpleAlias.PREFIX + "§cThe new name of this alias contains illegal characters!");
			} else {
				a.setName(newName);
				sender.sendMessage(SimpleAlias.PREFIX + "§aThe alias §6" + name + " §awas renamed to §e" + newName + "§a.");
			}
		}
	}
}