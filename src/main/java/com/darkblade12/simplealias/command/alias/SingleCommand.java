package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.alias.action.CommandAction;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public final class SingleCommand extends CommandBase<SimpleAlias> {
    public SingleCommand() {
        super("single", Permission.COMMAND_SINGLE, false, "<name>", "<command>");
    }

    @Override
    public void execute(SimpleAlias plugin, CommandSender sender, String label, String[] args) {
        String name = StringUtils.removeStart(args[0], "/");
        AliasManager manager = plugin.getAliasManager();
        if (manager.hasAlias(name)) {
            plugin.sendMessage(sender, "alias.alreadyExists", name);
            return;
        } else if (!Alias.isValid(name)) {
            plugin.sendMessage(sender, "alias.invalidName", name);
            return;
        } else if (StringUtils.removeStart(args[1], "/").equalsIgnoreCase(name)) {
            plugin.sendMessage(sender, "alias.noSelfExecution");
            return;
        }

        String[] commandParts = Arrays.copyOfRange(args, 1, args.length);
        String command = StringUtils.removeStart(StringUtils.join(commandParts, ' '), "/");
        Alias alias;
        try {
            alias = manager.createAlias(name);
        } catch (Exception e) {
            plugin.sendMessage(sender, "alias.creationFailed", name, e.getMessage());
            e.printStackTrace();
            return;
        }

        CommandAction action = new CommandAction("ExecuteCommand", command);
        alias.setActions(action);
        alias.setExecutionOrder(action.getName());

        try {
            alias.saveSettings();
            plugin.sendMessage(sender, "alias.creationSucceeded", name);
        } catch (AliasException e) {
            plugin.sendMessage(sender, "alias.creationFailed", name, e.getMessage());
            e.printStackTrace();
        }
    }
}
