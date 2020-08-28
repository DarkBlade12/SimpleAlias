package com.darkblade12.simplealias.alias;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

public final class AliasCommand extends Command {
    private static final String FALLBACK_PREFIX = "alias";
    private static Field commandMapField;
    private static Field knownCommandsField;
    private static Method syncCommandsMethod;
    private final Alias alias;

    static {
        try {
            commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            syncCommandsMethod = Bukkit.getServer().getClass().getMethod("syncCommands");
            syncCommandsMethod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AliasCommand(Alias alias) {
        super(alias.getName(), alias.getDescription(), alias.getUsageCheckMessage(), Collections.emptyList());
        this.alias = alias;
    }

    public static void syncCommands() {
        try {
            syncCommandsMethod.invoke(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isVanillaCommand(String commandLine) {
        String commandName = commandLine.split(" ")[0].toLowerCase();

        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getServer());
            Command command = commandMap.getCommand(commandName);
            if (command == null || command instanceof PluginCommand) {
                return false;
            } else if (commandName.startsWith("minecraft:")) {
                return true;
            }

            Command fallbackCommand = commandMap.getCommand("minecraft:" + commandName);
            return fallbackCommand != null && command == fallbackCommand;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        alias.execute(sender, args);
        return true;
    }

    @Override
    public boolean testPermissionSilent(CommandSender target) {
        return alias.testPermission(target);
    }

    public boolean register() throws IllegalStateException {
        if (isRegistered()) {
            throw new IllegalStateException("Command is already registered.");
        }

        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getServer());
            return commandMap.register(getName(), FALLBACK_PREFIX, this);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean unregister() throws IllegalStateException {
        if (!isRegistered()) {
            throw new IllegalStateException("Command is not registered.");
        }

        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getServer());
            Map<String, Command> commands = (Map<String, Command>) knownCommandsField.get(commandMap);

            String name = getName();
            String[] labels = { name, FALLBACK_PREFIX + ":" + name };
            for (String label : labels) {
                commands.remove(label);
            }

            return unregister(commandMap);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}