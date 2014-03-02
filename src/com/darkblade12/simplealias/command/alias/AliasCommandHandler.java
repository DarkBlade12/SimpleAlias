package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.command.CommandHandler;
import com.darkblade12.simplealias.permission.Permission;

public final class AliasCommandHandler extends CommandHandler {
	public AliasCommandHandler() {
		super("alias", 4, Permission.SIMPLEALIAS_MASTER);
	}

	@Override
	protected void registerCommands() {
		register(CreateCommand.class);
		register(RemoveCommand.class);
		register(RenameCommand.class);
		register(ListCommand.class);
		register(ReloadCommand.class);
	}
}