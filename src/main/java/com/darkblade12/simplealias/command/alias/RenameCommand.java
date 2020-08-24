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

public final class RenameCommand extends CommandBase<SimpleAlias> {
    public RenameCommand() {
        super("rename", Permission.COMMAND_RENAME, "<name>", "<new_name>");
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

        String newName = StringUtils.removeStart(args[1], "/");
        if (manager.hasAlias(newName)) {
            plugin.sendMessage(sender, "alias.alreadyExists", newName);
            return;
        } else if (!Alias.isValid(newName)) {
            plugin.sendMessage(sender, "alias.invalidName", newName);
            return;
        }

        try {
            alias.rename(newName);
            plugin.sendMessage(sender, "command.alias.rename.succeeded", name, newName);
        } catch (AliasException e) {
            plugin.sendMessage(sender, "command.alias.rename.failed", name, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSuggestions(SimpleAlias plugin, CommandSender sender, String[] args) {
        return args.length == 1 ? plugin.getAliasManager().getAliasNames() : null;
    }
}
