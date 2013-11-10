package com.darkblade12.simplealias.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.util.StringUtil;

public class CommandHelpPage implements Iterable<CommandDetails> {
	private String header;
	private String footer;
	private String commandLabel;
	private int commandsPerPage;
	private List<CommandDetails> details;

	public CommandHelpPage(CommandHandler handler, String header, String footer, String commandLabel, int commandsPerPage) {
		this.header = header;
		this.footer = footer;
		this.commandLabel = commandLabel;
		this.commandsPerPage = commandsPerPage;
		details = new ArrayList<CommandDetails>();
		for (ICommand c : handler)
			details.add(c.getClass().getAnnotation(CommandDetails.class));
	}

	private String toString(CommandDetails c) {
		return commandLabel.replace("<random_color>", StringUtil.randomColorCode()).replace("<command>", c.usage()).replace("<console_check>", c.executableAsConsole() ? StringUtil.CHECK : StringUtil.MISSING)
				.replace("<description>", c.description()).replace("<permission>", c.permission());
	}

	public void showPage(CommandSender s, int page) {
		List<CommandDetails> v = getVisibleDetails(s);
		StringBuilder b = new StringBuilder();
		for (int i = (page - 1) * commandsPerPage; i <= page * commandsPerPage - 1; i++)
			if (i > v.size() - 1)
				break;
			else
				b.append((b.length() == 0 ? header != null && header.length() > 0 ? "\n§r" : "" : "\n§r") + toString(v.get(i)));
		if (header != null && header.length() > 0)
			b.insert(0, header);
		if (footer != null && footer.length() > 0) {
			int pages = getPages(s);
			b.append("\n§r" + footer.replace("<current_page>", (page == pages ? "§6§l" : "§a§l") + page).replace("<page_amount>", pages + ""));
		}
		s.sendMessage(b.toString());
	}

	public boolean hasPage(CommandSender s, int page) {
		return page > 0 && page <= getPages(s);
	}

	public int getPages(CommandSender s) {
		double p = (double) getVisibleDetails(s).size() / (double) commandsPerPage;
		int pr = (int) p;
		return p > (double) pr ? pr + 1 : pr;
	}

	public List<CommandDetails> getVisibleDetails(CommandSender s) {
		List<CommandDetails> visible = new ArrayList<CommandDetails>();
		for (CommandDetails c : this)
			if (s.hasPermission(c.permission()) || s.hasPermission(SimpleAlias.MASTER_PERMISSION))
				visible.add(c);
		return visible;
	}

	public String getHeader() {
		return this.header;
	}

	public String getFooter() {
		return this.footer;
	}

	public String getCommandLabel() {
		return this.commandLabel;
	}

	public int getCommandsPerPage() {
		return this.commandsPerPage;
	}

	@Override
	public Iterator<CommandDetails> iterator() {
		return details.iterator();
	}
}