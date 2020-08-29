package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.CommandAction;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import com.darkblade12.simplealias.plugin.settings.InvalidValueException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MultipleCommand extends CommandBase<SimpleAlias> {
    public MultipleCommand() {
        super("multiple", Permission.COMMAND_MULTIPLE, false, "<name>", "<command#command...>");
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
        }

        String[] commandParts = Arrays.copyOfRange(args, 1, args.length);
        String[] commands = StringUtils.join(commandParts, ' ').split("#");
        List<Action> actions = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();

        for (int i = 0; i < commands.length; i++) {
            String finalCommand = StringUtils.removeStart(commands[i], "/");
            if (finalCommand.split(" ")[0].equalsIgnoreCase(name)) {
                plugin.sendMessage(sender, "alias.noSelfExecution");
                return;
            }

            String actionName = "ExecuteCommand" + (i + 1);
            actions.add(new CommandAction(actionName, finalCommand));
            executionOrder.add(actionName);
        }

        Alias alias;
        try {
            alias = manager.createAlias(name);
        } catch (AliasException | InvalidValueException e) {
            plugin.sendMessage(sender, "alias.creationFailed", name, e.getMessage());
            e.printStackTrace();
            return;
        }

        alias.setActions(actions);
        alias.setExecutionOrder(executionOrder);

        try {
            alias.saveSettings();
            plugin.sendMessage(sender, "alias.creationSucceeded", name);
        } catch (AliasException e) {
            plugin.sendMessage(sender, "alias.creationFailed", name, e.getMessage());
            e.printStackTrace();
        }
    }
}
