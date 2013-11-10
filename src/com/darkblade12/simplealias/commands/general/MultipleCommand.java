package com.darkblade12.simplealias.commands.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.types.MultipleAlias;
import com.darkblade12.simplealias.commands.CommandDetails;
import com.darkblade12.simplealias.commands.ICommand;
import com.darkblade12.simplealias.util.StringUtil;

@CommandDetails(name = "multiple", usage = "/sa multiple <name> <command#command...>", description = "Creates a multiple command alias", executableAsConsole = true, permission = "SimpleAlias.create.multiple")
public class MultipleCommand implements ICommand {
	@Override
	public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
		String name = StringUtil.stripFirstSlash(params[0]);
		if (plugin.aliasHandler.exists(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name already exists!");
			return;
		} else if (StringUtil.containsIllegalCharacters(name)){
			sender.sendMessage(SimpleAlias.PREFIX + "§cThe name of this alias contains illegal characters!");
			return;
		}
		String[] c = StringUtils.join((String[]) Arrays.copyOfRange(params, 1, params.length), " ").split("#");
		List<String> commands = new ArrayList<String>();
		for (String command : c) {
			String cmd = StringUtil.stripFirstSlash(command);
			if (cmd.split(" ")[0].equalsIgnoreCase(name)) {
				sender.sendMessage(SimpleAlias.PREFIX + "§cYou can't create an alias that executes itself!");
				return;
			}
			commands.add(cmd);
		}
		plugin.aliasHandler.register(new MultipleAlias(plugin, name, commands));
		sender.sendMessage(SimpleAlias.PREFIX + "§aYou've successfully created a multiple command alias with the name §6" + name + "§a.");
	}
}