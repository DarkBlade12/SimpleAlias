package com.darkblade12.simplealias.command.alias;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.Executor;
import com.darkblade12.simplealias.alias.action.Type;
import com.darkblade12.simplealias.alias.action.types.CommandAction;
import com.darkblade12.simplealias.alias.action.types.MessageAction;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "createaction", params = "<name> <action> <type>", description = "Creates a new action with default settings for an alias", permission = Permission.CREATE_ACTION_COMMAND)
public final class CreateActionCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		Alias a = SimpleAlias.getAliasManager().getAlias(name);
		if (a == null) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name doesn't exist!");
		} else {
			String action = params[1];
			if (a.hasAction(action)) {
				sender.sendMessage(SimpleAlias.PREFIX + "§cAn action with this name already exists!");
			} else {
				Type type = Type.fromName(params[2]);
				if (type == null) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cA type with this name doesn't exist!");
				} else {
					List<Action> actions = a.getActions();
					if (type == Type.COMMAND) {
						actions.add(new CommandAction(action, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashMap<Integer, String>(), 0, false, "msg <sender_name> Default action", Executor.SENDER, false));
					} else if (type == Type.MESSAGE) {
						actions.add(new MessageAction(action, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashMap<Integer, String>(), 0, false, "Default action", false));
					}
					a.getExecutionOrder().add(action);
					try {
						a.save();
						sender.sendMessage(SimpleAlias.PREFIX + "§aThe action §6" + action + " §awas successfully created for the alias with the name §6" + name + "§a.");
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