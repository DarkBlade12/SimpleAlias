package com.darkblade12.simplealias.plugin.command;

import com.darkblade12.simplealias.plugin.PluginBase;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

final class HelpIndex<T extends PluginBase> {
    private final CommandHandler<T> handler;
    private final int commandsPerPage;

    public HelpIndex(CommandHandler<T> handler, int commandsPerPage) {
        if (commandsPerPage < 1) {
            throw new IllegalArgumentException("The value of commandsPerPage cannot be lower than 1.");
        }

        this.handler = handler;
        this.commandsPerPage = commandsPerPage;
    }

    public void displayPage(CommandSender sender, String label, int page) {
        T plugin = handler.getPlugin();
        List<CommandBase<T>> visible = getVisibleCommands(sender);
        StringBuilder message = new StringBuilder(plugin.formatMessage("command.help.header"));
        for (int index = (page - 1) * commandsPerPage; index <= page * commandsPerPage - 1; index++) {
            if (index > visible.size() - 1) {
                break;
            }
            message.append("\n\u00A7r").append(getInfo(visible.get(index), label));
        }

        int pages = getPages(sender);
        String currentPage = (page == pages ? "\u00A76\u00A7l" : "\u00A7a\u00A7l") + page;
        message.append("\n\u00A7r").append(plugin.formatMessage("command.help.footer", currentPage, pages));
        sender.sendMessage(message.toString());
    }

    public boolean hasPage(CommandSender sender, int page) {
        return page > 0 && page <= getPages(sender);
    }

    public int getPages(CommandSender sender) {
        int total = getVisibleCommands(sender).size();
        int pages = total / commandsPerPage;
        return total % commandsPerPage == 0 ? pages : ++pages;
    }

    private List<CommandBase<T>> getVisibleCommands(CommandSender sender) {
        List<CommandBase<T>> visible = new ArrayList<>();
        for (CommandBase<T> command : handler) {
            if (command.testPermission(sender)) {
                visible.add(command);
            }
        }
        return visible;
    }

    private String getInfo(CommandBase<T> command, String label) {
        T plugin = handler.getPlugin();
        String usage = command.getUsage(label);
        String commandName = command.getName();
        String defaultLabel = handler.getDefaultLabel();
        String description;

        if (commandName.equals("help")) {
            description = plugin.formatMessage("command.help.description", defaultLabel);
        } else {
            description = plugin.formatMessage("command." + defaultLabel + "." + commandName + ".description");
        }

        String permission = command.getPermission().getName();
        return plugin.formatMessage("command.help.commandInfo", usage, description, permission);
    }
}
