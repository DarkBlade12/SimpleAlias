package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.alias.action.ActionType;
import com.darkblade12.simplealias.alias.action.CommandAction;
import com.darkblade12.simplealias.alias.action.MessageAction;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import com.darkblade12.simplealias.util.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CreateActionCommand extends CommandBase<SimpleAlias> {
    private static final String DEFAULT_COMMAND = "msg <sender_name> This action has not been setup yet";
    private static final String DEFAULT_MESSAGE = "This action has not been setup yet";

    public CreateActionCommand() {
        super("createaction", false, Permission.COMMAND_CREATE_ACTION, "<name>", "<action_name>", "<type>", "[value]");
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
        if (alias.hasAction(actionName)) {
            plugin.sendMessage(sender, "action.alreadyExists", actionName);
            return;
        }

        String typeName = args[2];
        ActionType type = ActionType.fromName(typeName);
        if (type == null) {
            plugin.sendMessage(sender, "command.alias.createaction.typeNotFound", typeName);
            return;
        }

        String value = args.length < 4 ? null : StringUtils.join(args, ' ', 3, args.length);

        switch (type) {
            case COMMAND:
                String command;
                if (value != null) {
                    command = StringUtils.removeStart(value, "/");
                    if (MessageUtils.isBlank(command)) {
                        plugin.sendMessage(sender, "command.alias.createaction.noEmptyValue");
                        return;
                    } else if (value.split(" ")[0].equalsIgnoreCase(name)) {
                        plugin.sendMessage(sender, "alias.noSelfExecution");
                        return;
                    }
                } else {
                    command = DEFAULT_COMMAND;
                }

                alias.addAction(new CommandAction(actionName, command));
                break;
            case MESSAGE:
                String message;
                if (value != null) {
                    message = MessageUtils.translateMessage(value);
                    if (MessageUtils.isBlank(message)) {
                        plugin.sendMessage(sender, "command.alias.createaction.noEmptyValue");
                        return;
                    }
                } else {
                    message = DEFAULT_MESSAGE;
                }

                alias.addAction(new MessageAction(actionName, message));
                break;
        }

        try {
            alias.saveSettings();
            plugin.sendMessage(sender, "command.alias.createaction.succeeded", actionName, name);
        } catch (AliasException e) {
            plugin.sendMessage(sender, "command.alias.createaction.failed", actionName, name);
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSuggestions(SimpleAlias plugin, CommandSender sender, String[] args) {
        switch (args.length) {
            case 1:
                return plugin.getAliasManager().getAliasNames();
            case 3:
                return Arrays.stream(ActionType.values()).map(a -> a.name().toLowerCase()).collect(Collectors.toList());
            default:
                return null;
        }
    }
}
