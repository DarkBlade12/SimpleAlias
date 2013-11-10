package com.darkblade12.simplealias.commands.general;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.types.SingleAlias;
import com.darkblade12.simplealias.commands.CommandDetails;
import com.darkblade12.simplealias.commands.ICommand;
import com.darkblade12.simplealias.util.StringUtil;

@CommandDetails(name = "single", usage = "/sa single <name> <command>", description = "Creates a single command alias", executableAsConsole = true, permission = "SimpleAlias.create.single")
public class SingleCommand implements ICommand {
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
		String c = StringUtil.stripFirstSlash(StringUtils.join((String[]) Arrays.copyOfRange(params, 1, params.length), " "));
		if (c.split(" ")[0].equalsIgnoreCase(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cYou can't create an alias that executes itself!");
			return;
		}
		plugin.aliasHandler.register(new SingleAlias(plugin, name, c));
		sender.sendMessage(SimpleAlias.PREFIX + "§aYou've successfully created a single command alias with the name §6" + name + "§a.");
	}
}