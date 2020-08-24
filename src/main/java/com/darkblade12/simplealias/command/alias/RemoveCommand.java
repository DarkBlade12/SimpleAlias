package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class RemoveCommand extends CommandBase<SimpleAlias> {
    public RemoveCommand() {
        super("remove", Permission.COMMAND_REMOVE, "<name>");
    }

    @Override
    public void execute(SimpleAlias plugin, CommandSender sender, String label, String[] args) {
        String name = StringUtils.removeStart(args[0], "/");
        AliasManager manager = plugin.getAliasManager();
        Alias alias = manager.getAlias(name);
        if (alias == null) {
            plugin.sendMessage(sender, "alias.notFound", name);
            return;
        }
        name = alias.getName();

        try {
            manager.removeAlias(alias);
            plugin.sendMessage(sender, "command.alias.remove.succeeded", name);
        } catch (AliasException e) {
            plugin.sendMessage(sender, "command.alias.remove.failed", name, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSuggestions(SimpleAlias plugin, CommandSender sender, String[] args) {
        return args.length == 1 ? plugin.getAliasManager().getAliasNames() : null;
    }
}
