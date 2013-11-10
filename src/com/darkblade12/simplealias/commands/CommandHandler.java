package com.darkblade12.simplealias.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.commands.general.AliasesCommand;
import com.darkblade12.simplealias.commands.general.DeleteCommand;
import com.darkblade12.simplealias.commands.general.DetailsCommand;
import com.darkblade12.simplealias.commands.general.HelpCommand;
import com.darkblade12.simplealias.commands.general.MultipleCommand;
import com.darkblade12.simplealias.commands.general.ReloadCommand;
import com.darkblade12.simplealias.commands.general.RenameCommand;
import com.darkblade12.simplealias.commands.general.SettingCommand;
import com.darkblade12.simplealias.commands.general.SingleCommand;
import com.darkblade12.simplealias.commands.general.TextCommand;

public class CommandHandler implements CommandExecutor, Iterable<ICommand> {
	private SimpleAlias plugin;
	private Map<String, ICommand> commands;
	private List<ICommand> sorted;
	public CommandHelpPage helpPage;

	public CommandHandler(SimpleAlias plugin) {
		this.plugin = plugin;
		plugin.getCommand("sa").setExecutor(this);
		registerCommands();
		helpPage = new CommandHelpPage(this, SimpleAlias.PREFIX + "§9Command help page for SimpleAlias v" + plugin.getDescription().getVersion() + ":",
				"§8§m------------------§8[§7Page <current_page> §7of §6§l<page_amount>§8]§m------------------§r", "§a\u2022 <command>\n  §7\u25BB <description>\n  §7\u25BB Permission: §2<permission>", 4);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			showUsage(sender, commands.get("help"));
		} else {
			ICommand c = getMatchingCommand(args[0].toLowerCase());
			if (c == null) {
				showUsage(sender, commands.get("help"));
			} else {
				CommandDetails cd = getDetails(c);
				String[] params = trimParams(args);
				if (!(sender instanceof Player) && !cd.executableAsConsole())
					sender.sendMessage("§cThis command can't be executed as console!");
				else if (!cd.permission().equals("None") && !sender.hasPermission(cd.permission()) && !sender.hasPermission(SimpleAlias.MASTER_PERMISSION))
					sender.sendMessage("§cYou don't have permission for this command!");
				else if (!checkUsage(c, params))
					showUsage(sender, c);
				else
					c.execute(plugin, sender, params);
			}
		}
		return true;
	}

	private ICommand getMatchingCommand(String name) {
		for (Entry<String, ICommand> e : commands.entrySet())
			if (e.getKey().equals(name))
				return e.getValue();
		return null;
	}

	private CommandDetails getDetails(ICommand cmd) {
		return cmd.getClass().getAnnotation(CommandDetails.class);
	}

	private String[] trimParams(String[] args) {
		return (String[]) Arrays.copyOfRange(args, 1, args.length);
	}

	private boolean checkUsage(ICommand cmd, String[] params) {
		CommandDetails cd = getDetails(cmd);
		String n = cd.name();
		String[] p = cd.usage().split(" ");
		p = (String[]) Arrays.copyOfRange(p, 2, p.length);
		int min = 0, max = n.equals("single") || n.equals("multiple") || n.equals("text") || n.equals("setting") ? 100 : 0;
		for (int i = 0; i < p.length; i++) {
			max++;
			if (!p[i].matches("\\[.*\\]"))
				min++;
		}
		return params.length >= min && params.length <= max;
	}

	public void showUsage(CommandSender sender, ICommand cmd) {
		CommandDetails cd = getDetails(cmd);
		sender.sendMessage("§cInvalid usage!\n§6" + cd.usage());
	}

	private void registerCommands() {
		commands = new HashMap<String, ICommand>();
		sorted = new ArrayList<ICommand>();
		register(SingleCommand.class);
		register(MultipleCommand.class);
		register(TextCommand.class);
		register(DeleteCommand.class);
		register(RenameCommand.class);
		register(SettingCommand.class);
		register(AliasesCommand.class);
		register(DetailsCommand.class);
		register(ReloadCommand.class);
		register(HelpCommand.class);
	}

	private void register(Class<? extends ICommand> cmd) {
		CommandDetails cd = cmd.getAnnotation(CommandDetails.class);
		if (cd != null)
			try {
				ICommand i = cmd.newInstance();
				commands.put(cd.name(), i);
				sorted.add(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@Override
	public Iterator<ICommand> iterator() {
		return sorted.iterator();
	}
}