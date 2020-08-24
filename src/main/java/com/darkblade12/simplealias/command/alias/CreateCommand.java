package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import com.darkblade12.simplealias.plugin.settings.InvalidValueException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public final class CreateCommand extends CommandBase<SimpleAlias> {
    public CreateCommand() {
        super("create", Permission.COMMAND_CREATE, "<name>");
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

        try {
            manager.createAlias(name);
            plugin.sendMessage(sender, "alias.creationSucceeded", name);
        } catch (AliasException | InvalidValueException e) {
            plugin.sendMessage(sender, "alias.creationFailed", name, e.getMessage());
            e.printStackTrace();
        }
    }
}
