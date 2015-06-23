package com.darkblade12.simplealias.command.alias;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.command.CommandDetails;
import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.command.ICommand;
import com.darkblade12.simplealias.permission.Permission;

@CommandDetails(name = "reload", description = "Reloads the whole plugin", permission = Permission.RELOAD_COMMAND)
public final class ReloadCommand implements ICommand {
	@Override
	public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
		long start = System.currentTimeMillis();
		SimpleAlias.instance().onReload();
		sender.sendMessage(SimpleAlias.PREFIX + "§7Plugin was successfully reloaded. (" + (System.currentTimeMillis() - start) + " ms)");
	}
}