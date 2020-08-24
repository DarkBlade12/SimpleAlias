package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class RenameActionCommand extends CommandBase<SimpleAlias> {
    public RenameActionCommand() {
        super("renameaction", Permission.COMMAND_RENAME_ACTION, "<name>", "<action_name>", "<new_action_name>");
    }


    @Override
    public void execute(SimpleAlias plugin, CommandSender sender, String label, String[] args) {
        String name = StringUtils.removeStart(args[0], "/");
        Alias alias = plugin.getAliasManager().getAlias(name);
        if (alias == null) {
            plugin.sendMessage(sender, "alias.notFound", name);
            return;
        }
        name = alias.getName();

        String actionName = args[1];
        Action action = alias.getAction(actionName);
        if (action == null) {
            plugin.sendMessage(sender, "action.notFound", actionName);
            return;
        }
        actionName = action.getName();

        String newActionName = args[2];
        if (alias.hasAction(newActionName)) {
            plugin.sendMessage(sender, "action.alreadyExists", newActionName);
            return;
        }

        alias.renameAction(action, newActionName);

        try {
            alias.saveSettings();
            plugin.sendMessage(sender, "command.alias.renameaction.succeeded", actionName, name, newActionName);
        } catch (AliasException e) {
            plugin.sendMessage(sender, "command.alias.renameaction.failed", actionName, name, newActionName);
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSuggestions(SimpleAlias plugin, CommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                return plugin.getAliasManager().getAliasNames();
            case 2:
                Alias alias = plugin.getAliasManager().getAlias(args[0]);
                return alias == null ? null : alias.getActionNames();
            default:
                return null;
        }
    }
}
