package com.darkblade12.simplealias.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.permission.Permission;
import com.darkblade12.simplealias.permission.PermissionList;

public abstract class CommandHandler implements CommandExecutor {
	private String defaultLabel;
	private CommandList commands;
	private ICommand helpCommand;
	protected PermissionList masterPermissions;

	public CommandHandler(String defaultLabel, int commandsPerPage, Permission... masterPermissions) {
		this.defaultLabel = defaultLabel;
		SimpleAlias.instance().getCommand(defaultLabel).setExecutor(this);
		commands = new CommandList();
		registerCommands();
		helpCommand = new HelpCommand(new CommandHelpPage(this, commandsPerPage));
		commands.add(helpCommand);
		this.masterPermissions = new PermissionList(masterPermissions);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			showUsage(sender, label, helpCommand);
		} else {
			ICommand i = commands.get(args[0]);
			if (i == null) {
				showUsage(sender, label, helpCommand);
			} else {
				CommandDetails c = getDetails(i);
				String[] params = trimParams(args);
				if (!(sender instanceof Player) && !c.executableAsConsole())
					sender.sendMessage("§cThis command can't be executed as console!");
				else if (!c.permission().hasPermission(sender) && !masterPermissions.hasAnyPermission(sender))
					sender.sendMessage("§cYou don't have permission for this command!");
				else if (!checkUsage(i, params))
					showUsage(sender, label, i);
				else
					i.execute(this, sender, label, params);
			}
		}
		return true;
	}

	protected void register(Class<? extends ICommand> clazz) {
		if (clazz.getAnnotation(CommandDetails.class) != null)
			try {
				commands.add(clazz.newInstance());
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	protected abstract void registerCommands();

	private String[] trimParams(String[] args) {
		return (String[]) Arrays.copyOfRange(args, 1, args.length);
	}

	private boolean checkUsage(ICommand i, String[] params) {
		CommandDetails c = getDetails(i);
		String commandParams = c.params();
		if (commandParams.length() == 0)
			return params.length == 0;
		String[] p = commandParams.split(" ");
		int min = 0, max = c.infiniteParams() ? 100 : 0;
		for (int a = 0; a < p.length; a++) {
			max++;
			if (!p[a].matches("\\[.*\\]"))
				min++;
		}
		return params.length >= min && params.length <= max;
	}

	public void showUsage(CommandSender sender, String label, ICommand i) {
		sender.sendMessage("§cInvalid usage!\n§6" + getUsage(label, i));
	}

	public static CommandDetails getDetails(ICommand i) {
		return i.getClass().getAnnotation(CommandDetails.class);
	}

	public String getDefaultLabel() {
		return this.defaultLabel;
	}

	public List<ICommand> getCommands() {
		return Collections.unmodifiableList(commands);
	}

	public ICommand getCommand(String name) {
		return commands.get(name);
	}

	public List<Permission> getMasterPermissions() {
		return Collections.unmodifiableList(masterPermissions);
	}

	public String getUsage(String label, ICommand i) {
		CommandDetails c = getDetails(i);
		String params = c.params();
		return "/" + label + " " + c.name() + (params.length() > 0 ? " " + params : "");
	}

	public String getUsage(ICommand i) {
		return getUsage(defaultLabel, i);
	}

	@CommandDetails(name = "help", params = "[page]", description = "Shows the help pages", executableAsConsole = true)
	private final class HelpCommand implements ICommand {
		private CommandHelpPage helpPage;

		public HelpCommand(CommandHelpPage helpPage) {
			this.helpPage = helpPage;
		}

		@Override
		public void execute(CommandHandler handler, CommandSender sender, String label, String[] params) {
			int page = 1;
			if (params.length == 1) {
				String input = params[0];
				try {
					page = Integer.parseInt(input);
					if (!helpPage.hasPage(sender, page)) {
						sender.sendMessage(SimpleAlias.PREFIX + "§cThis help page doesn't exist!");
						return;
					}
				} catch (Exception e) {
					sender.sendMessage(SimpleAlias.PREFIX + "§6" + input + " §cisn't numeric!");
					return;
				}
			}
			helpPage.showPage(sender, label, page);
		}
	}
}