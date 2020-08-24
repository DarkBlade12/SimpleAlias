package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.alias.action.MessageAction;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import com.darkblade12.simplealias.plugin.settings.InvalidValueException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public final class MessageCommand extends CommandBase<SimpleAlias> {
    public MessageCommand() {
        super("message", Permission.COMMAND_MESSAGE, "<name>", "<message>");
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

        Alias alias;
        try {
            alias = manager.createAlias(name);
        } catch (AliasException | InvalidValueException e) {
            plugin.sendMessage(sender, "alias.creationFailed", name, e.getMessage());
            e.printStackTrace();
            return;
        }

        String[] messageParts = Arrays.copyOfRange(args, 1, args.length);
        String message = ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(StringUtils.join(messageParts, ' ')));

        MessageAction action = new MessageAction("DisplayMessage", message);
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
