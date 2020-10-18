package com.darkblade12.simplealias.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.ActionSetting;
import com.darkblade12.simplealias.alias.action.ActionType;
import com.darkblade12.simplealias.alias.action.CommandAction;
import com.darkblade12.simplealias.alias.action.Executor;
import com.darkblade12.simplealias.alias.action.MessageAction;
import com.darkblade12.simplealias.cooldown.Cooldown;
import com.darkblade12.simplealias.cooldown.CooldownManager;
import com.darkblade12.simplealias.nameable.Nameable;
import com.darkblade12.simplealias.nameable.NameableList;
import com.darkblade12.simplealias.plugin.hook.VaultHook;
import com.darkblade12.simplealias.plugin.reader.ConfigurationReader;
import com.darkblade12.simplealias.plugin.settings.InvalidValueException;
import com.darkblade12.simplealias.plugin.settings.SettingInfo;
import com.darkblade12.simplealias.replacer.Replacer;
import com.darkblade12.simplealias.util.ConvertUtils;
import com.darkblade12.simplealias.util.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Alias implements Nameable {
    private static final Pattern ILLEGAL_CHARACTERS = Pattern.compile("[\\s\\/:*?\"<>|#]");
    public static final String DEFAULT_DESCRIPTION = "No description set";
    private final SimpleAlias plugin;
    private final ConfigurationReader<SimpleAlias> reader;
    private AliasCommand command;
    private String name;
    private String description;
    private boolean executableAsConsole;
    private String consoleMessage;
    private Set<String> enabledWorlds;
    private String worldMessage;
    private boolean usageCheckEnabled;
    private int usageCheckMinParams;
    private int usageCheckMaxParams;
    private String usageCheckMessage;
    private NameableList<Action> actions;
    private List<String> executionOrder;
    private boolean permissionEnabled;
    private String permissionNode;
    private Set<String> permissionGroups;
    private String permissionMessage;
    private boolean delayEnabled;
    private boolean delayCancelOnMove;
    private int delayDuration;
    private String delayMessage;
    private String delayCancelMessage;
    private boolean cooldownEnabled;
    private int cooldownDuration;
    private String cooldownMessage;
    private boolean costEnabled;
    private double costAmount;
    private String costMessage;
    private boolean loggingEnabled;
    private String loggingMessage;

    public Alias(SimpleAlias plugin, String name, boolean copyTemplate) throws AliasException, InvalidValueException {
        if (!isValid(name)) {
            throw new IllegalArgumentException("Name cannot contain illegal characters");
        }

        this.plugin = plugin;
        this.name = name;

        File aliasFile = new File(plugin.getAliasManager().getDataDirectory(), name + ".yml");
        reader = new ConfigurationReader<>(plugin, plugin.getSettings().getTemplatePath(), aliasFile);
        if (copyTemplate && (!plugin.isTemplateValid() || !plugin.getTemplateReader().copyOutputFile(aliasFile)) &&
            !reader.saveResourceFile()) {
            throw new AliasException("Failed to copy alias template.");
        }
        loadSettings();

        command = new AliasCommand(this);
        command.setPermissionMessage(permissionMessage);

        registerCommand();
    }

    private Alias(SimpleAlias plugin, ConfigurationReader<SimpleAlias> reader) throws AliasException, InvalidValueException {
        this.plugin = plugin;
        name = ConfigurationReader.stripExtension(reader.getOutputFile());
        this.reader = reader;
        loadSettings();
    }

    public static void validateTemplate(SimpleAlias plugin)
            throws AliasException, InvalidValueException {
        new Alias(plugin, plugin.getTemplateReader());
    }

    private static List<String> getValues(ConfigurationSection section, SettingInfo setting) {
        String path = setting.getPath();
        if (section.isList(path)) {
            return section.getStringList(path);
        }

        String value = section.getString(path);
        if (value == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(ConvertUtils.split(value));
    }

    public static boolean isValid(String name) {
        return !ILLEGAL_CHARACTERS.matcher(name).find();
    }

    private void loadSettings() throws AliasException, InvalidValueException {
        if (!reader.readConfiguration()) {
            throw new AliasException("Failed to read alias configuration.");
        }

        FileConfiguration config = reader.getConfig();
        description = config.getString(AliasSetting.DESCRIPTION.getPath(), DEFAULT_DESCRIPTION);
        executableAsConsole = config.getBoolean(AliasSetting.EXECUTABLE_AS_CONSOLE.getPath());
        consoleMessage = MessageUtils.translateMessage(config.getString(AliasSetting.CONSOLE_MESSAGE.getPath(), ""));
        this.enabledWorlds = new HashSet<>();
        List<String> enabledWorlds = getValues(config, AliasSetting.ENABLED_WORLDS);
        for (String worldName : enabledWorlds) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new InvalidValueException(AliasSetting.ENABLED_WORLDS, worldName, "unknown world");
            }

            this.enabledWorlds.add(worldName);
        }
        worldMessage = MessageUtils.translateMessage(config.getString(AliasSetting.WORLD_MESSAGE.getPath(), ""));

        usageCheckEnabled = config.getBoolean(AliasSetting.USAGE_CHECK_ENABLED.getPath());
        usageCheckMinParams = config.getInt(AliasSetting.USAGE_CHECK_MIN_PARAMS.getPath());
        if (usageCheckMinParams < 0) {
            throw new InvalidValueException(AliasSetting.USAGE_CHECK_MIN_PARAMS.getPath(), usageCheckMinParams, "cannot be lower than 1");
        }
        usageCheckMaxParams = config.getInt(AliasSetting.USAGE_CHECK_MAX_PARAMS.getPath());
        if (usageCheckMaxParams >= 0 && usageCheckMaxParams < usageCheckMinParams) {
            throw new InvalidValueException(AliasSetting.USAGE_CHECK_MAX_PARAMS, usageCheckMinParams,
                                            "cannot be lower than value of " + AliasSetting.USAGE_CHECK_MIN_PARAMS);
        }
        usageCheckMessage = MessageUtils.translateMessage(config.getString(AliasSetting.USAGE_CHECK_MESSAGE.getPath(), ""));

        loadActionSettings(config);
        this.executionOrder = new ArrayList<>();
        List<String> executionOrder = getValues(config, AliasSetting.EXECUTION_ORDER);
        if (executionOrder.isEmpty()) {
            throw new InvalidValueException(AliasSetting.EXECUTION_ORDER, "cannot be empty");
        }
        for (String actionName : executionOrder) {
            Action action = actions.get(actionName);
            if (action == null) {
                throw new InvalidValueException(AliasSetting.EXECUTION_ORDER, actionName, "unknown action");
            }

            this.executionOrder.add(action.getName());
        }

        permissionEnabled = config.getBoolean(AliasSetting.PERMISSION_ENABLED.getPath());
        permissionNode = config.getString(AliasSetting.PERMISSION_NODE.getPath(), "");
        this.permissionGroups = new HashSet<>();
        List<String> permissionGroups = getValues(config, AliasSetting.PERMISSION_GROUPS);
        VaultHook vault = plugin.getVaultHook();
        for (String group : permissionGroups) {
            String exactName = vault.getExactGroupName(group);
            if (exactName == null) {
                throw new InvalidValueException(AliasSetting.PERMISSION_GROUPS, group, "unknown permission group");
            }

            this.permissionGroups.add(exactName);
        }
        permissionMessage = MessageUtils.translateMessage(config.getString(AliasSetting.PERMISSION_MESSAGE.getPath(), ""));

        delayEnabled = config.getBoolean(AliasSetting.DELAY_ENABLED.getPath());
        delayCancelOnMove = config.getBoolean(AliasSetting.DELAY_CANCEL_ON_MOVE.getPath());
        delayDuration = config.getInt(AliasSetting.DELAY_DURATION.getPath());
        if (delayDuration < 1) {
            throw new InvalidValueException(AliasSetting.DELAY_DURATION, delayDuration, "cannot be lower than 1");
        }
        delayMessage = MessageUtils.translateMessage(config.getString(AliasSetting.DELAY_MESSAGE.getPath(), ""));
        delayCancelMessage = MessageUtils.translateMessage(config.getString(AliasSetting.DELAY_CANCEL_MESSAGE.getPath(), ""));

        cooldownEnabled = config.getBoolean(AliasSetting.COOLDOWN_ENABLED.getPath());
        cooldownDuration = config.getInt(AliasSetting.COOLDOWN_DURATION.getPath());
        if (cooldownDuration < 1) {
            throw new InvalidValueException(AliasSetting.COOLDOWN_DURATION, cooldownDuration, "cannot be lower than 1");
        }
        cooldownMessage = MessageUtils.translateMessage(config.getString(AliasSetting.COOLDOWN_MESSAGE.getPath(), ""));

        costEnabled = config.getBoolean(AliasSetting.COST_ENABLED.getPath());
        costAmount = config.getDouble(AliasSetting.COST_AMOUNT.getPath());
        if (costAmount <= 0) {
            throw new InvalidValueException(AliasSetting.COST_AMOUNT, costAmount, "cannot be lower than or equal to 0");
        }
        costMessage = MessageUtils.translateMessage(config.getString(AliasSetting.COST_MESSAGE.getPath(), ""));

        loggingEnabled = config.getBoolean(AliasSetting.LOGGING_ENABLED.getPath());
        loggingMessage = MessageUtils.translateMessage(config.getString(AliasSetting.LOGGING_MESSAGE.getPath(), ""));
    }

    private void loadActionSettings(FileConfiguration config) throws AliasException, InvalidValueException {
        String actionsPath = AliasSection.ACTIONS.getPath();
        ConfigurationSection actionRoot = config.getConfigurationSection(actionsPath);
        if (actionRoot == null) {
            throw new AliasException(String.format("The alias '%s' is missing section '%s'.", name, actionsPath));
        }

        actions = new NameableList<>();
        for (String actionName : actionRoot.getKeys(false)) {
            if (actions.contains(actionName)) {
                throw new AliasException(String.format("The action name '%s' in alias '%s' is not unique.", actionName, name));
            }

            ConfigurationSection section = actionRoot.getConfigurationSection(actionName);
            if (section == null) {
                continue;
            }

            String typeName = section.getString(ActionSetting.TYPE.getPath());
            String typePath = ActionSetting.TYPE.getAbsolutePath(actionName);
            if (typeName == null) {
                throw new InvalidValueException(typePath, "no value set");
            }
            ActionType type = ActionType.fromName(typeName);
            if (type == null) {
                throw new InvalidValueException(typePath, typeName, "unknown action type");
            }

            Set<String> enabledWorlds = new HashSet<>(getValues(section, ActionSetting.ENABLED_WORLDS));
            for (String worldName : enabledWorlds) {
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    throw new InvalidValueException(ActionSetting.ENABLED_WORLDS.getAbsolutePath(actionName), worldName, "unknown world");
                }
            }

            Set<String> enabledPermissionNodes = new HashSet<>(getValues(section, ActionSetting.ENABLED_PERMISSION_NODES));
            Set<String> enabledPermissionGroups = new HashSet<>(getValues(section, ActionSetting.ENABLED_PERMISSION_GROUPS));
            VaultHook vault = plugin.getVaultHook();
            for (String group : enabledPermissionGroups) {
                String exactName = vault.getExactGroupName(group);
                if (exactName == null) {
                    throw new InvalidValueException(AliasSetting.PERMISSION_GROUPS, group, "unknown permission group");
                }
            }

            List<String> paramsList = getValues(section, ActionSetting.ENABLED_PARAMS);
            Map<Integer, String> enabledParams = new HashMap<>();
            for (String param : paramsList) {
                String[] values = param.split("@");
                String paramsPath = ActionSetting.ENABLED_PARAMS.getAbsolutePath(actionName);
                if (values.length != 2) {
                    throw new InvalidValueException(paramsPath, param, "format does not match <param>@<index>");
                }

                int index;
                try {
                    index = Integer.parseInt(values[1]);
                } catch (NumberFormatException e) {
                    throw new InvalidValueException(paramsPath, param, "not a valid number");
                }

                if (index < 0) {
                    throw new InvalidValueException(paramsPath, param, "index cannot be lower than 0");
                } else if (index > usageCheckMaxParams) {
                    throw new InvalidValueException(paramsPath, param, "index cannot be higher than value of "
                                                                       + AliasSetting.USAGE_CHECK_MAX_PARAMS);
                } else if (enabledParams.containsKey(index)) {
                    throw new InvalidValueException(paramsPath, param, "index " + index + " is duplicate");
                }

                enabledParams.put(index, values[0].replaceAll("\\s", ""));
            }

            int priority = section.getInt(ActionSetting.PRIORITY.getPath());
            boolean translateColorCodes = section.getBoolean(ActionSetting.TRANSLATE_COLOR_CODES.getPath());

            switch (type) {
                case COMMAND:
                    String command = section.getString(ActionSetting.COMMAND.getPath());
                    String commandPath = ActionSetting.COMMAND.getAbsolutePath(actionName);
                    if (command == null) {
                        throw new InvalidValueException(commandPath, "no value set");
                    }

                    command = StringUtils.removeStart(command, "/");
                    if (command.split(" ")[0].equalsIgnoreCase(name)) {
                        throw new InvalidValueException(commandPath, command, "alias cannot execute itself");
                    }

                    String executorName = section.getString(ActionSetting.EXECUTOR.getPath());
                    String executorPath = ActionSetting.EXECUTOR.getAbsolutePath(actionName);
                    if (executorName == null) {
                        throw new InvalidValueException(executorPath, "no value set");
                    }

                    Executor executor = Executor.fromName(executorName);
                    if (executor == null) {
                        throw new InvalidValueException(executorPath, executorName, "unknown executor");
                    }

                    boolean grantPermission = section.getBoolean(ActionSetting.GRANT_PERMISSION.getPath());
                    boolean silent = section.getBoolean(ActionSetting.SILENT.getPath());

                    actions.add(new CommandAction(actionName, enabledWorlds, enabledPermissionNodes, enabledPermissionGroups, enabledParams,
                                                  priority, translateColorCodes, command, executor, grantPermission, silent));
                    break;
                case MESSAGE:
                    List<String> lines = section.getStringList(ActionSetting.MESSAGE.getPath());
                    String message;
                    if (!lines.isEmpty()) {
                        message = StringUtils.join(lines, "\n§r");
                    } else {
                        message = section.getString(ActionSetting.MESSAGE.getPath());
                        if (MessageUtils.isBlank(message)) {
                            throw new InvalidValueException(ActionSetting.MESSAGE.getAbsolutePath(actionName), "cannot be blank");
                        }
                    }

                    message = MessageUtils.translateMessage(message);
                    boolean broadcast = section.getBoolean(ActionSetting.BROADCAST.getPath());

                    actions.add(new MessageAction(actionName, enabledWorlds, enabledPermissionNodes, enabledPermissionGroups, enabledParams,
                                                  priority, translateColorCodes, message, broadcast));
                    break;
            }
        }

        if (actions.isEmpty()) {
            throw new AliasException(String.format("The alias '%s' has no valid actions.", name));
        }
    }

    public void reloadSettings() throws AliasException, InvalidValueException {
        if (command.isRegistered()) {
            unregisterCommand();
        }

        loadSettings();

        command.setDescription(description);
        command.setUsage(usageCheckMessage);
        command.setPermissionMessage(permissionMessage);

        registerCommand();
    }

    public void saveSettings() throws AliasException {
        FileConfiguration config = reader.getConfig();

        config.set(AliasSetting.DESCRIPTION.getPath(), description);
        config.set(AliasSetting.EXECUTABLE_AS_CONSOLE.getPath(), executableAsConsole);
        config.set(AliasSetting.CONSOLE_MESSAGE.getPath(), MessageUtils.reverseTranslateMessage(consoleMessage));
        config.set(AliasSetting.ENABLED_WORLDS.getPath(), new ArrayList<>(enabledWorlds));
        config.set(AliasSetting.WORLD_MESSAGE.getPath(), MessageUtils.reverseTranslateMessage(worldMessage));
        config.set(AliasSetting.EXECUTION_ORDER.getPath(), executionOrder);

        String actionsPath = AliasSection.ACTIONS.getPath();
        ConfigurationSection actionRoot = config.getConfigurationSection(actionsPath);
        if (actionRoot == null) {
            throw new AliasException(String.format("The alias '%s' is missing the section '%s'.", name, actionsPath));
        }

        for (String actionName : actionRoot.getKeys(false)) {
            if (!actions.contains(actionName)) {
                actionRoot.set(actionName, null);
            }
        }

        for (Action action : actions) {
            String actionName = action.getName();
            ConfigurationSection section = actionRoot.getConfigurationSection(actionName);
            if (section == null) {
                section = actionRoot.createSection(actionName);
            }

            section.set(ActionSetting.TYPE.getPath(), action.getType().name());
            section.set(ActionSetting.ENABLED_WORLDS.getPath(), new ArrayList<>(action.getEnabledWorlds()));
            section.set(ActionSetting.ENABLED_PERMISSION_NODES.getPath(), new ArrayList<>(action.getEnabledPermissionNodes()));
            section.set(ActionSetting.ENABLED_PERMISSION_GROUPS.getPath(), new ArrayList<>(action.getEnabledPermissionGroups()));
            List<String> enabledParams = action.getEnabledParams().entrySet().stream().map(e -> e.getValue() + "@" + e.getKey())
                                               .collect(Collectors.toList());
            section.set(ActionSetting.ENABLED_PARAMS.getPath(), enabledParams);
            section.set(ActionSetting.PRIORITY.getPath(), action.getPriority());
            section.set(ActionSetting.TRANSLATE_COLOR_CODES.getPath(), action.hasTranslateColorCodes());

            if (action instanceof CommandAction) {
                CommandAction commandAction = (CommandAction) action;
                section.set(ActionSetting.COMMAND.getPath(), commandAction.getCommand());
                section.set(ActionSetting.EXECUTOR.getPath(), commandAction.getExecutor().name());
                section.set(ActionSetting.GRANT_PERMISSION.getPath(), commandAction.hasGrantPermission());
                section.set(ActionSetting.SILENT.getPath(), commandAction.isSilent());
            } else if (action instanceof MessageAction) {
                MessageAction messageAction = (MessageAction) action;
                section.set(ActionSetting.MESSAGE.getPath(), messageAction.getMessage());
                section.set(ActionSetting.BROADCAST.getPath(), messageAction.isBroadcast());
            }
        }

        config.set(AliasSetting.USAGE_CHECK_ENABLED.getPath(), usageCheckEnabled);
        config.set(AliasSetting.USAGE_CHECK_MIN_PARAMS.getPath(), usageCheckMinParams);
        config.set(AliasSetting.USAGE_CHECK_MAX_PARAMS.getPath(), usageCheckMaxParams);
        config.set(AliasSetting.USAGE_CHECK_MESSAGE.getPath(), MessageUtils.reverseTranslateMessage(usageCheckMessage));

        config.set(AliasSetting.PERMISSION_ENABLED.getPath(), permissionEnabled);
        config.set(AliasSetting.PERMISSION_NODE.getPath(), permissionNode);
        config.set(AliasSetting.PERMISSION_GROUPS.getPath(), new ArrayList<>(permissionGroups));
        config.set(AliasSetting.PERMISSION_MESSAGE.getPath(), MessageUtils.reverseTranslateMessage(permissionMessage));

        config.set(AliasSetting.DELAY_ENABLED.getPath(), delayEnabled);
        config.set(AliasSetting.DELAY_CANCEL_ON_MOVE.getPath(), delayCancelOnMove);
        config.set(AliasSetting.DELAY_DURATION.getPath(), delayDuration);
        config.set(AliasSetting.DELAY_MESSAGE.getPath(), MessageUtils.reverseTranslateMessage(delayMessage));
        config.set(AliasSetting.DELAY_CANCEL_MESSAGE.getPath(), MessageUtils.reverseTranslateMessage(delayCancelMessage));

        config.set(AliasSetting.COOLDOWN_ENABLED.getPath(), cooldownEnabled);
        config.set(AliasSetting.COOLDOWN_DURATION.getPath(), cooldownDuration);
        config.set(AliasSetting.COOLDOWN_MESSAGE.getPath(), MessageUtils.reverseTranslateMessage(cooldownMessage));

        config.set(AliasSetting.COST_ENABLED.getPath(), costEnabled);
        config.set(AliasSetting.COST_AMOUNT.getPath(), costAmount);
        config.set(AliasSetting.COST_MESSAGE.getPath(), MessageUtils.reverseTranslateMessage(costMessage));

        config.set(AliasSetting.LOGGING_ENABLED.getPath(), loggingEnabled);
        config.set(AliasSetting.LOGGING_MESSAGE.getPath(), MessageUtils.reverseTranslateMessage(loggingMessage));

        if (!reader.saveConfiguration()) {
            throw new AliasException("Failed to save alias configuration.");
        }
    }

    public void deleteSettings() throws AliasException {
        if (!reader.deleteOutputFile()) {
            throw new AliasException(String.format("Failed to remove configuration of alias '%s'.", name));
        }
    }

    public void registerCommand() throws AliasException {
        try {
            if (!command.register()) {
                throw new AliasException(String.format("Failed to register alias '%s' as a command.", name));
            }
        } catch (IllegalStateException e) {
            throw new AliasException(String.format("Alias '%s' is already registered as a command.", name));
        }
    }

    public void unregisterCommand() throws AliasException {
        try {
            if (!command.unregister()) {
                throw new AliasException(String.format("Failed to unregister alias '%s' from commands.", name));
            }
        } catch (IllegalStateException e) {
            throw new AliasException(String.format("Alias '%s' is not registered as a command.", name));
        }
    }

    public boolean testPermission(CommandSender sender) {
        if (!permissionEnabled || sender instanceof ConsoleCommandSender) {
            return true;
        }

        if (sender instanceof Player && plugin.getVaultHook().isInAnyGroup((Player) sender, permissionGroups)) {
            return true;
        }

        return permissionNode != null && sender.hasPermission(permissionNode) || Permission.USE_ALL.test(sender);
    }

    public void execute(final CommandSender sender, final String[] params) {
        if (sender instanceof ConsoleCommandSender && !executableAsConsole) {
            if (!MessageUtils.isBlank(consoleMessage)) {
                sender.sendMessage(consoleMessage);
            }
            return;
        }

        if (loggingEnabled && !MessageUtils.isBlank(loggingMessage)) {
            Replacer replacer = Replacer.builder()
                                        .with(DynamicVariable.ALIAS_NAME, name)
                                        .with(DynamicVariable.PARAMS, StringUtils.join(params, " "))
                                        .with(DynamicVariable.SENDER_NAME, sender.getName())
                                        .build();
            Bukkit.getLogger().info(replacer.replaceAll(loggingMessage));
        }

        if (!testPermission(sender)) {
            if (!MessageUtils.isBlank(permissionMessage)) {
                sender.sendMessage(permissionMessage);
            }
            return;
        }

        if (usageCheckEnabled && (params.length < usageCheckMinParams || usageCheckMaxParams >= 0 && params.length > usageCheckMaxParams)) {
            if (!MessageUtils.isBlank(usageCheckMessage)) {
                sender.sendMessage(usageCheckMessage);
            }
            return;
        }

        if (!(sender instanceof Player)) {
            executeActions(sender, params);
            return;
        }

        final Player player = (Player) sender;
        if (!isEnabled(player.getWorld()) && !Permission.BYPASS_ENABLED_WORLDS.test(player)) {
            if (!MessageUtils.isBlank(worldMessage)) {
                player.sendMessage(worldMessage);
            }
            return;
        }

        final boolean hasCooldown = cooldownEnabled && !Permission.BYPASS_COOLDOWN.test(player);
        final CooldownManager cooldownManager = plugin.getCooldownManager();
        Cooldown cooldown = hasCooldown ? cooldownManager.getCooldown(player, name) : null;
        if (cooldown != null) {
            if (!cooldown.isExpired()) {
                if (cooldownMessage != null && !cooldownMessage.isEmpty()) {
                    player.sendMessage(cooldownMessage.replace("<remaining_time>", cooldown.toString()));
                }
                return;
            }

            cooldownManager.unregister(player, name);
        }

        VaultHook vault = plugin.getVaultHook();
        if (costEnabled && !Permission.BYPASS_COST.test(sender) && !vault.withdrawPlayer(player, costAmount)) {
            if (!MessageUtils.isBlank(costMessage)) {
                Replacer replacer = Replacer.builder()
                                            .with(DynamicVariable.COST_AMOUNT, vault.formatCurrency(costAmount))
                                            .with(DynamicVariable.MONEY_BALANCE, vault.formatCurrency(vault.getBalance(player)))
                                            .build();
                player.sendMessage(replacer.replaceAll(costMessage));
            }
            return;
        }

        if (!delayEnabled || Permission.BYPASS_DELAY.test(sender)) {
            executeActions(sender, params);
            if (hasCooldown) {
                cooldownManager.register(player, name, new Cooldown(cooldownDuration));
            }
            return;
        }

        final long delayTicks = delayDuration * 20L;
        final BukkitTask execution = new BukkitRunnable() {
            @Override
            public void run() {
                executeActions(sender, params);
                if (hasCooldown) {
                    cooldownManager.register(player, name, new Cooldown(cooldownDuration));
                }
            }
        }.runTaskLater(plugin, delayTicks + 1);

        String remainingTime = MessageUtils.formatDuration(delayDuration * 1000);
        if (!MessageUtils.isBlank(delayMessage)) {
            sender.sendMessage(DynamicVariable.REMAINING_TIME.replaceAll(delayMessage, remainingTime));
        }

        new BukkitRunnable() {
            private final Location lastPosition = delayCancelOnMove ? player.getLocation() : null;
            private int ticks = 0;

            @Override
            public void run() {
                if (ticks >= delayTicks) {
                    cancel();
                    return;
                } else if (!player.isOnline()) {
                    cancel();
                    execution.cancel();
                    return;
                } else if (lastPosition != null) {
                    boolean moved;
                    try {
                        moved = lastPosition.distanceSquared(player.getLocation()) > 0.1;
                    } catch (IllegalArgumentException e) {
                        moved = true;
                    }

                    if (moved) {
                        cancel();
                        execution.cancel();

                        if (delayCancelMessage != null && !delayCancelMessage.isEmpty()) {
                            player.sendMessage(delayCancelMessage);
                        }
                    }
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    private void executeActions(CommandSender sender, String[] params) {
        List<Action> enabledActions = actions.stream().filter(a -> a.isEnabled(plugin, sender, params)).collect(Collectors.toList());
        if (enabledActions.isEmpty()) {
            return;
        }

        int maxPriority = enabledActions.stream().map(Action::getPriority).max(Integer::compareTo).orElse(0);
        List<Action> priorityActions = enabledActions.stream().filter(a -> a.getPriority() == maxPriority).collect(Collectors.toList());

        for (String actionName : executionOrder) {
            priorityActions.stream().filter(a -> a.getName().equalsIgnoreCase(actionName)).findFirst()
                           .ifPresent(a -> a.execute(plugin, sender, params));
        }
    }

    public void rename(String name) throws AliasException {
        if (!isValid(name)) {
            throw new IllegalArgumentException("Name cannot contain illegal characters");
        }

        String fileName = name + ".yml";
        if (!reader.renameOutputFile(fileName)) {
            throw new AliasException(String.format("Failed to rename alias configuration to '%s'.", fileName));
        }

        unregisterCommand();
        command.setName(name);
        try {
            registerCommand();
        } catch (AliasException e) {
            command.setName(this.name);
            registerCommand();
            throw e;
        }

        this.name = name;

        if (plugin.getSettings().hasCommandSync()) {
            AliasCommand.syncCommands();
        }
    }

    public void renameAction(Action action, String newName) {
        int index = executionOrder.indexOf(action.getName());
        if (index != -1) {
            executionOrder.set(index, newName);
        }
        action.setName(newName);
    }

    @Override
    public String toString() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
        command.setDescription(description);
    }

    public void setExecutableAsConsole(boolean executableAsConsole) {
        this.executableAsConsole = executableAsConsole;
    }

    public void setConsoleMessage(String consoleMessage) {
        this.consoleMessage = consoleMessage;
    }

    public void setEnabledWorlds(Set<String> enabledWorlds) {
        this.enabledWorlds = enabledWorlds;
    }

    public void setWorldMessage(String worldMessage) {
        this.worldMessage = worldMessage;
    }

    public void setActions(Collection<Action> actions) {
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("Actions cannot be empty.");
        }

        this.actions = new NameableList<>(actions);
    }

    public void setActions(Action... actions) {
        if (actions.length == 0) {
            throw new IllegalArgumentException("Actions cannot be empty.");
        }

        this.actions = new NameableList<>(Arrays.asList(actions));
    }

    public void addAction(Action action) {
        if (actions.contains(action.getName())) {
            throw new IllegalArgumentException("Action name is not unique.");
        }

        actions.add(action);
        executionOrder.add(action.getName());
    }

    public void removeAction(Action action) {
        actions.remove(action);
        String name = action.getName();
        executionOrder.removeIf(s -> s.equalsIgnoreCase(name));
    }

    public void setExecutionOrder(Collection<String> executionOrder) {
        if (executionOrder.isEmpty()) {
            throw new IllegalArgumentException("Execution order cannot be empty.");
        }

        this.executionOrder = new ArrayList<>(executionOrder);
    }

    public void setExecutionOrder(String... executionOrder) {
        if (executionOrder.length == 0) {
            throw new IllegalArgumentException("Execution order cannot be empty.");
        }


        this.executionOrder = new ArrayList<>(Arrays.asList(executionOrder));
    }

    public void setUsageCheckEnabled(boolean usageCheckEnabled) {
        this.usageCheckEnabled = usageCheckEnabled;
    }

    public void setUsageCheckMinParams(int usageCheckMinParams) {
        this.usageCheckMinParams = usageCheckMinParams;
    }

    public void setUsageCheckMaxParams(int usageCheckMaxParams) {
        this.usageCheckMaxParams = usageCheckMaxParams;
    }

    public void setUsageCheckMessage(String usageCheckMessage) {
        this.usageCheckMessage = usageCheckMessage;
        command.setUsage(usageCheckMessage);
    }

    public void setPermissionEnabled(boolean permissionEnabled) {
        this.permissionEnabled = permissionEnabled;
    }

    public void setPermissionNode(String permissionNode) {
        this.permissionNode = permissionNode;
    }

    public void setPermissionGroups(Set<String> permissionGroups) {
        this.permissionGroups = permissionGroups;
    }

    public void setPermissionMessage(String permissionMessage) {
        this.permissionMessage = permissionMessage;
        command.setPermissionMessage(permissionMessage);
    }

    public void setDelayEnabled(boolean delayEnabled) {
        this.delayEnabled = delayEnabled;
    }

    public void setDelayCancelOnMove(boolean delayCancelOnMove) {
        this.delayCancelOnMove = delayCancelOnMove;
    }

    public void setDelayDuration(int delayDuration) {
        this.delayDuration = delayDuration;
    }

    public void setDelayMessage(String delayMessage) {
        this.delayMessage = delayMessage;
    }

    public void setDelayCancelMessage(String delayCancelMessage) {
        this.delayCancelMessage = delayCancelMessage;
    }

    public void setCooldownEnabled(boolean cooldownEnabled) {
        this.cooldownEnabled = cooldownEnabled;
    }

    public void setCooldownDuration(int cooldownDuration) {
        this.cooldownDuration = cooldownDuration;
    }

    public void setCooldownMessage(String cooldownMessage) {
        this.cooldownMessage = cooldownMessage;
    }

    public void setCostEnabled(boolean costEnabled) {
        this.costEnabled = costEnabled;
    }

    public void setCostAmount(double costAmount) {
        this.costAmount = costAmount;
    }

    public void setCostMessage(String costMessage) {
        this.costMessage = costMessage;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public void setLoggingMessage(String loggingMessage) {
        this.loggingMessage = loggingMessage;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isExecutableAsConsole() {
        return executableAsConsole;
    }

    public String getConsoleMessage() {
        return consoleMessage;
    }

    public Set<String> getEnabledWorlds() {
        return enabledWorlds;
    }

    public boolean isEnabled(World world) {
        return enabledWorlds.isEmpty() || enabledWorlds.contains(world.getName());
    }

    public String getWorldMessage() {
        return worldMessage;
    }

    public boolean isUsageCheckEnabled() {
        return usageCheckEnabled;
    }

    public int getUsageCheckMinParams() {
        return usageCheckMinParams;
    }

    public int getUsageCheckMaxParams() {
        return usageCheckMaxParams;
    }

    public String getUsageCheckMessage() {
        return usageCheckMessage;
    }

    public List<Action> getActions() {
        return Collections.unmodifiableList(actions);
    }

    public List<String> getActionNames() {
        return actions.getNames();
    }

    public Action getAction(String name) {
        return actions.get(name);
    }

    public boolean hasAction(String name) {
        return actions.contains(name);
    }

    public List<String> getExecutionOrder() {
        return executionOrder;
    }

    public boolean isPermissionEnabled() {
        return permissionEnabled;
    }

    public String getPermissionNode() {
        return permissionNode;
    }

    public Set<String> getPermissionGroups() {
        return permissionGroups;
    }

    public String getPermissionMessage() {
        return permissionMessage;
    }

    public boolean isDelayEnabled() {
        return delayEnabled;
    }

    public boolean isDelayCancelOnMove() {
        return delayCancelOnMove;
    }

    public int getDelayDuration() {
        return delayDuration;
    }

    public String getDelayMessage() {
        return delayMessage;
    }

    public String getDelayCancelMessage() {
        return delayCancelMessage;
    }

    public boolean isCooldownEnabled() {
        return cooldownEnabled;
    }

    public long getCooldownDuration() {
        return cooldownDuration;
    }

    public String getCooldownMessage() {
        return cooldownMessage;
    }

    public boolean isCostEnabled() {
        return costEnabled;
    }

    public double getCostAmount() {
        return costAmount;
    }

    public String getCostMessage() {
        return costMessage;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public String getLoggingMessage() {
        return loggingMessage;
    }

    public AliasCommand getCommand() {
        return command;
    }
}
