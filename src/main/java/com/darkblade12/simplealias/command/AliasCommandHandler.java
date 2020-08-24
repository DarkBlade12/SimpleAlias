package com.darkblade12.simplealias.command;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.command.alias.CreateActionCommand;
import com.darkblade12.simplealias.command.alias.CreateCommand;
import com.darkblade12.simplealias.command.alias.DetailsCommand;
import com.darkblade12.simplealias.command.alias.ListCommand;
import com.darkblade12.simplealias.command.alias.MessageCommand;
import com.darkblade12.simplealias.command.alias.ModifyActionCommand;
import com.darkblade12.simplealias.command.alias.ModifyCommand;
import com.darkblade12.simplealias.command.alias.MultipleCommand;
import com.darkblade12.simplealias.command.alias.ReloadCommand;
import com.darkblade12.simplealias.command.alias.RemoveActionCommand;
import com.darkblade12.simplealias.command.alias.RemoveCommand;
import com.darkblade12.simplealias.command.alias.RenameActionCommand;
import com.darkblade12.simplealias.command.alias.RenameCommand;
import com.darkblade12.simplealias.command.alias.SingleCommand;
import com.darkblade12.simplealias.plugin.command.CommandHandler;
import com.darkblade12.simplealias.plugin.command.CommandRegistrationException;

public final class AliasCommandHandler extends CommandHandler<SimpleAlias> {
    public AliasCommandHandler(SimpleAlias plugin) {
        super(plugin, "alias");
    }

    @Override
    protected void registerCommands() throws CommandRegistrationException {
        registerCommand(CreateCommand.class);
        registerCommand(SingleCommand.class);
        registerCommand(MultipleCommand.class);
        registerCommand(MessageCommand.class);
        registerCommand(RemoveCommand.class);
        registerCommand(RenameCommand.class);
        registerCommand(ModifyCommand.class);
        registerCommand(CreateActionCommand.class);
        registerCommand(RemoveActionCommand.class);
        registerCommand(RenameActionCommand.class);
        registerCommand(ModifyActionCommand.class);
        registerCommand(ListCommand.class);
        registerCommand(DetailsCommand.class);
        registerCommand(ReloadCommand.class);
    }
}
