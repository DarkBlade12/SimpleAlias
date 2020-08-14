package com.darkblade12.simplealias.command.alias;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.types.MessageAction;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "message", params = "<name> <message>", description = "Creates a new alias for a message", permission = Permission.MESSAGE_COMMAND, infiniteParams = true)
public final class MessageCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		String name = StringUtils.removeStart(params[0], "/");
		AliasManager manager = SimpleAlias.getAliasManager();
		if (manager.hasAlias(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name already exists!");
		} else if (!Alias.isValid(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cThe name of this alias contains illegal characters!");
		} else if (StringUtils.removeStart(params[1], "/").equalsIgnoreCase(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cYou can't create an alias which executes itself!");
		} else {
			String[] textArray = (String[]) Arrays.copyOfRange(params, 1, params.length);
			String text = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(StringUtils.join(textArray, ' ')));
			Alias alias;
			try {
				alias = manager.createAlias(name);
			} catch (Exception e) {
				sender.sendMessage(SimpleAlias.PREFIX + "§cThe alias creation failed! Cause: " + e.getMessage());
				if(Settings.isDebugEnabled()) {
					e.printStackTrace();
				}
				return;
			}
			List<Action> actions = alias.getActions();
			actions.clear();
			actions.add(new MessageAction("DisplayMessage", new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), new HashMap<Integer, String>(), 0, false, text, false));
			List<String> executionOrder = alias.getExecutionOrder();
			executionOrder.clear();
			executionOrder.add("DisplayMessage");
			try {
				alias.save();
				sender.sendMessage(SimpleAlias.PREFIX + "§aThe message alias with the name §6" + name + " §awas successfully created.");
			} catch (Exception e) {
				sender.sendMessage(SimpleAlias.PREFIX + "§cThe alias creation failed! Cause: " + e.getMessage());
				if(Settings.isDebugEnabled()) {
					e.printStackTrace();
				}
			}
		}
	}
}