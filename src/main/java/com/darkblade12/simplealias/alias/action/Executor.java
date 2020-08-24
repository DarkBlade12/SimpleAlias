package com.darkblade12.simplealias.alias.action;

import com.darkblade12.simplealias.SimpleAlias;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum Executor {
    SENDER("Sender") {
        @Override
        public void dispatchCommand(SimpleAlias plugin, CommandSender sender, String command, boolean grantPermission, boolean silent) {
            if (!(sender instanceof Player)) {
                CONSOLE.dispatchCommand(plugin, sender, command, grantPermission, silent);
                return;
            }

            Player player = (Player) sender;
            if (!player.isOp()) {
                plugin.getAliasManager().addUncheckedPlayer(player);
            }

            PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/" + command);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                if (grantPermission || silent) {
                    sender = new CommandSenderProxy(sender, grantPermission, silent);
                }
                Bukkit.dispatchCommand(sender, StringUtils.removeStart(event.getMessage(), "/"));
            }
        }
    },
    CONSOLE("Console") {
        @Override
        public void dispatchCommand(SimpleAlias plugin, CommandSender sender, String command, boolean grantPermission, boolean silent) {
            if (!(sender instanceof ConsoleCommandSender)) {
                sender = Bukkit.getConsoleSender();
            } else if (silent) {
                sender = new CommandSenderProxy(sender, false, true);
            }

            ServerCommandEvent event = new ServerCommandEvent(sender, command);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                Bukkit.dispatchCommand(sender, event.getCommand());
            }
        }
    };

    private static final Map<String, Executor> BY_NAME = new HashMap<>();
    private final String name;

    Executor(String name) {
        this.name = name;
    }

    static {
        for (Executor executor : values()) {
            BY_NAME.put(executor.name().toLowerCase(), executor);
        }
    }

    public abstract void dispatchCommand(SimpleAlias plugin, CommandSender sender, String command, boolean grantPermission, boolean silent);

    public static Executor fromName(String name) {
        return BY_NAME.get(name.toLowerCase());
    }

    public static Set<String> getNames() {
        return BY_NAME.keySet();
    }

    @Override
    public String toString() {
        return name;
    }
}
