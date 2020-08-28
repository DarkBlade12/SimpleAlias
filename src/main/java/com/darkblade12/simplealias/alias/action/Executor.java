package com.darkblade12.simplealias.alias.action;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.AliasCommand;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum Executor {
    SENDER {
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

            if (grantPermission || silent) {
                try {

                    player = (Player) Proxy.newProxyInstance(player.getClass().getClassLoader(), new Class<?>[] { Player.class },
                                                             new PlayerProxy(player, grantPermission, silent));
                } catch (SecurityException e) {
                    e.printStackTrace();
                    return;
                }
            }

            PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, "/" + command);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                String commandLine = StringUtils.removeStart(event.getMessage(), "/");
                if (AliasCommand.isVanillaCommand(commandLine.split(" ")[0])) {
                    Bukkit.dispatchCommand(new ConsoleProxy(sender, grantPermission, silent), commandLine);
                } else {
                    Bukkit.dispatchCommand(player, commandLine);
                }
            }
        }
    },
    CONSOLE {
        @Override
        public void dispatchCommand(SimpleAlias plugin, CommandSender sender, String command, boolean grantPermission, boolean silent) {
            ConsoleCommandSender console;
            if (sender instanceof ConsoleCommandSender) {
                console = (ConsoleCommandSender) sender;
            } else {
                console = Bukkit.getConsoleSender();
            }

            if (silent) {
                console = new ConsoleProxy(console, false, true);
            }

            ServerCommandEvent event = new ServerCommandEvent(console, command);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                Bukkit.dispatchCommand(console, event.getCommand());
            }
        }
    };

    private static final Map<String, Executor> BY_NAME = new HashMap<>();

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
        return name().toLowerCase();
    }
}
