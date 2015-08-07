package com.darkblade12.simplealias.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;

public final class CommandHelpPage {
	private static final String HEADER = SimpleAlias.PREFIX + "§eHelp page for the §6<label> §ecommand:";
	private static final String FOOTER = "§8§m--------------------§8[§7Page <current_page> §7of §6§l<page_amount>§8]§m---------------------";
	private static final String COMMAND_FORMAT = "§a\u2022 <command>\n  §7\u25BB <description>\n  §7\u25BB Permission: §2<permission>";
	private CommandHandler handler;
	private int commandsPerPage;

	public CommandHelpPage(CommandHandler handler, int commandsPerPage) {
		this.handler = handler;
		this.commandsPerPage = commandsPerPage;
	}

	private String insertIntoFormat(String label, ICommand i) {
		CommandDetails c = CommandHandler.getDetails(i);
		return COMMAND_FORMAT.replace("<command>", handler.getUsage(label, i)).replace("<description>", c.description()).replace("<permission>", c.permission().getNode());
	}

	public void showPage(CommandSender sender, String label, int page) {
		List<ICommand> visible = getVisibleCommands(sender);
		String header = HEADER.replace("<label>", label);
		StringBuilder b = new StringBuilder(header);
		for (int i = (page - 1) * commandsPerPage; i <= page * commandsPerPage - 1; i++)
			if (i > visible.size() - 1)
				break;
			else
				b.append("\n§r" + insertIntoFormat(label, visible.get(i)));
		int pages = getPages(sender);
		b.append("\n§r" + FOOTER.replace("<current_page>", (page == pages ? "§6§l" : "§a§l") + Integer.toString(page)).replace("<page_amount>", Integer.toString(pages)));
		sender.sendMessage(b.toString());
	}

	public boolean hasPage(CommandSender sender, int page) {
		return page > 0 && page <= getPages(sender);
	}

	public int getPages(CommandSender sender) {
		int total = getVisibleCommands(sender).size();
		int pages = total / commandsPerPage;
		return total % commandsPerPage == 0 ? pages : pages + 1;
	}

	public List<ICommand> getVisibleCommands(CommandSender sender) {
		List<ICommand> visible = new ArrayList<ICommand>();
		for (ICommand i : handler.getCommands())
			if (CommandHandler.getDetails(i).permission().hasPermission(sender) || handler.masterPermissions.hasAnyPermission(sender))
				visible.add(i);
		return visible;
	}

	public CommandHandler getHandler() {
		return this.handler;
	}

	public int getCommandsPerPage() {
		return this.commandsPerPage;
	}
}