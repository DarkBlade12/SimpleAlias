package com.darkblade12.simplealias.command.alias;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.Executor;
import com.darkblade12.simplealias.alias.action.types.CommandAction;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "multiple", params = "<name> <command#command...>", description = "Creates a new alias for multiple commands", permission = Permission.MULTIPLE_COMMAND, infiniteParams = true)
public final class MultipleCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		AliasManager manager = SimpleAlias.getAliasManager();
		if (manager.hasAlias(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name already exists!");
		} else if (!Alias.isValid(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cThe name of this alias contains illegal characters!");
		} else {
			String[] commandsArray = (String[]) Arrays.copyOfRange(params, 1, params.length);
			List<Action> actions = new ArrayList<Action>();
			List<String> executionOrder = new ArrayList<String>();
			int index = 1;
			for (String command : StringUtils.join(commandsArray, ' ').split("#")) {
				String finalCommand = StringUtils.removeStart(command, "/");
				if (finalCommand.split(" ")[0].equalsIgnoreCase(name)) {
					sender.sendMessage(SimpleAlias.PREFIX + "§cYou can't create an alias which executes itself!");
					return;
				}
				String actionName = "ExecuteCommand" + index;
				actions.add(new CommandAction("ExecuteCommand" + index, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashMap<Integer, String>(), 0, false, command, Executor.SENDER, false));
				executionOrder.add(actionName);
				index++;
			}
			Alias alias;
			try {
				alias = manager.createAlias(name);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(SimpleAlias.PREFIX + "§cThe alias creation failed! Cause: " + e.getMessage());
				return;
			}
			List<Action> aliasActions = alias.getActions();
			aliasActions.clear();
			aliasActions.addAll(actions);
			List<String> aliasExecutionOrder = alias.getExecutionOrder();
			aliasExecutionOrder.clear();
			aliasExecutionOrder.addAll(executionOrder);
			try {
				alias.save();
				sender.sendMessage(SimpleAlias.PREFIX + "§aThe multiple command alias with the name §6" + name + " §awas successfully created.");
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(SimpleAlias.PREFIX + "§cThe alias creation failed! Cause: " + e.getMessage());
			}
		}
	}
}