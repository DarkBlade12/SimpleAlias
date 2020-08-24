package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import com.darkblade12.simplealias.util.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class ListCommand extends CommandBase<SimpleAlias> {
    public ListCommand() {
        super("list", Permission.COMMAND_LIST);
    }

    @Override
    public void execute(SimpleAlias plugin, CommandSender sender, String label, String[] args) {
        AliasManager manager = plugin.getAliasManager();
        if (manager.getAliasCount() == 0) {
            plugin.sendMessage(sender, "command.alias.list.noneAvailable");
            return;
        }

        StringBuilder list = new StringBuilder();
        for (Alias alias : manager.getAliases()) {
            ChatColor color1 = MessageUtils.randomColor();
            ChatColor color2 = MessageUtils.similarColor(color1);
            list.append('\n').append(plugin.formatMessage("command.alias.list.line", color1, alias.getName(), color2));
        }

        plugin.sendMessage(sender, "command.alias.list.message", list.toString());
    }
}
