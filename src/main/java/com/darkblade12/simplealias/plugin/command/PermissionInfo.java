package com.darkblade12.simplealias.plugin.command;

import org.bukkit.command.CommandSender;

public interface PermissionInfo {
    boolean test(CommandSender sender);

    String getName();
}
