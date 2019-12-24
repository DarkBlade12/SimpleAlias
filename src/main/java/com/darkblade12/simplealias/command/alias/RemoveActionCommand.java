package com.darkblade12.simplealias.command.alias;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.nameable.NameableList;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "removeaction", params = "<name> <action>", description = "Removes an existing action from an alias", permission = Permission.REMOVE_ACTION_COMMAND)
public final class RemoveActionCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		Alias a = SimpleAlias.getAliasManager().getAlias(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
		} else {
			String action = params[1];
			if (!a.hasAction(action)) {
				sender.sendMessage(SimpleAlias.PREFIX + "§cAn action with this name doesn't exist!");
			} else {
				NameableList<Action> actions = a.getActions();
				String exactAction = actions.get(action).getName();
				if (actions.size() == 1) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cThe action §6" + exactAction + " §ccan't be removed, because it's the only one left!");
				} else {
					actions.remove(exactAction);
					a.getExecutionOrder().remove(exactAction);
					try {
						a.save();
						sender.sendMessage(SimpleAlias.PREFIX + "§aThe action §6" + exactAction + " §awas removed from the alias with the name §6" + name + "§a.");
					} catch (Exception e) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cFailed to save the alias! Cause: " + e.getMessage());
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
					}
				}

			}
		}
	}
}