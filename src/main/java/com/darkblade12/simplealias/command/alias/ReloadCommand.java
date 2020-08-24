package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import com.darkblade12.simplealias.plugin.settings.InvalidValueException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class ReloadCommand extends CommandBase<SimpleAlias> {
    public ReloadCommand() {
        super("reload", Permission.COMMAND_RELOAD, "[name]");
    }

    @Override
    public void execute(SimpleAlias plugin, CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            try {
                long start = System.currentTimeMillis();
                plugin.onReload();
                long duration = System.currentTimeMillis() - start;

                plugin.sendMessage(sender, "command.alias.reload.succeeded", plugin.getDescription().getVersion(), duration);
            } catch (Exception e) {
                plugin.sendMessage(sender, "command.alias.reload.failed");
                e.printStackTrace();
            }
            return;
        }

        String name = StringUtils.removeStart(args[0], "/");
        Alias alias = plugin.getAliasManager().getAlias(name);
        if (alias == null) {
            plugin.sendMessage(sender, "alias.notFound", name);
            return;
        }
        name = alias.getName();

        try {
            alias.reloadSettings();
            plugin.sendMessage(sender, "command.alias.reload.singleSucceeded", name);
        } catch (AliasException | InvalidValueException e) {
            plugin.sendMessage(sender, "command.alias.reload.singleFailed", name, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSuggestions(SimpleAlias plugin, CommandSender sender, String[] args) {
        return args.length == 1 ? plugin.getAliasManager().getAliasNames() : null;
    }
}
