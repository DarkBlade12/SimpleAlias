package com.darkblade12.simplealias.plugin.command;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.plugin.PluginBase;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class HelpCommand<T extends PluginBase> extends CommandBase<T> {
    private final HelpIndex<T> help;

    public HelpCommand(CommandHandler<T> handler, int commandsPerPage) {
        super("help", Permission.NONE, "[page]");
        help = new HelpIndex<>(handler, commandsPerPage);
    }

    @Override
    public void execute(T plugin, CommandSender sender, String label, String[] args) {
        int page = 1;
        if (args.length == 1) {
            String input = args[0];
            try {
                page = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                plugin.sendMessage(sender, "command.help.pageInvalid", input);
                return;
            }

            if (!help.hasPage(sender, page)) {
                plugin.sendMessage(sender, "command.help.pageNotFound", page);
                return;
            }
        }

        help.displayPage(sender, label, page);
    }

    @Override
    public List<String> getSuggestions(T plugin, CommandSender sender, String[] args) {
        if (args.length != 1) {
            return null;
        }

        return IntStream.rangeClosed(1, help.getPages(sender)).mapToObj(String::valueOf).collect(Collectors.toList());
    }
}
