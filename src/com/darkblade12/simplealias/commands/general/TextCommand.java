package com.darkblade12.simplealias.commands.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.types.TextAlias;
import com.darkblade12.simplealias.commands.CommandDetails;
import com.darkblade12.simplealias.commands.ICommand;
import com.darkblade12.simplealias.util.StringUtil;

@CommandDetails(name = "text", usage = "/sa text <name> <line#line...>", description = "Creates a text alias", executableAsConsole = true, permission = "SimpleAlias.create.text")
public class TextCommand implements ICommand {
	@Override
	public void execute(SimpleAlias plugin, CommandSender sender, String[] params) {
		String name = StringUtil.stripFirstSlash(params[0]);
		if (plugin.aliasHandler.exists(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cAn alias with this name already exists!");
			return;
		} else if (StringUtil.containsIllegalCharacters(name)) {
			sender.sendMessage(SimpleAlias.PREFIX + "§cThe name of this alias contains illegal characters!");
			return;
		}
		List<String> lines = new ArrayList<String>();
		for (String line : StringUtils.join((String[]) Arrays.copyOfRange(params, 1, params.length), " ").split("#"))
			lines.add(ChatColor.translateAlternateColorCodes('&', line));
		plugin.aliasHandler.register(new TextAlias(plugin, name, lines));
		sender.sendMessage(SimpleAlias.PREFIX + "§aYou've successfully created a text alias with the name §6" + name + "§a.");
	}
}