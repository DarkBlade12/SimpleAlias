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

public final class RemoveActionCommand extends CommandBase<SimpleAlias> {
    public RemoveActionCommand() {
        super("removeaction", Permission.COMMAND_REMOVE_ACTION, "<name>", "<action_name>");
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
        if (!alias.hasAction(actionName)) {
            plugin.sendMessage(sender, "action.notFound", actionName);
            return;
        }
        actionName = action.getName();

        if (alias.getActions().size() == 1) {
            plugin.sendMessage(sender, "command.alias.removeaction.lastRemaining", actionName);
            return;
        }

        alias.removeAction(action);

        try {
            alias.saveSettings();
            plugin.sendMessage(sender, "command.alias.removeaction.succeeded", actionName, name);
        } catch (AliasException e) {
            plugin.sendMessage(sender, "command.alias.removeaction.failed", actionName, name, e.getMessage());
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
