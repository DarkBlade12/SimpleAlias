package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.alias.AliasSetting;
import com.darkblade12.simplealias.alias.ModifyOperation;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import com.darkblade12.simplealias.plugin.hook.VaultHook;
import com.darkblade12.simplealias.util.ConvertUtils;
import com.darkblade12.simplealias.util.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModifyCommand extends CommandBase<SimpleAlias> {
    public ModifyCommand() {
        super("modify", Permission.COMMAND_MODIFY, false, "<name>", "<setting>", "<set|add>", "<value>");
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

        String settingKey = args[1];
        AliasSetting setting = AliasSetting.fromNameOrPath(settingKey);
        if (setting == null) {
            plugin.sendMessage(sender, "command.alias.modify.settingNotFound", settingKey);
            return;
        }
        settingKey = MessageUtils.formatName(setting, true, "");

        String operationName = args[2];
        ModifyOperation operation = ModifyOperation.fromName(operationName);
        if (operation == null) {
            plugin.sendMessage(sender, "command.alias.modify.operationNotFound", operationName);
            return;
        } else if (!setting.isSupported(operation)) {
            plugin.sendMessage(sender, "command.alias.modify.operationNotSupported", operationName, settingKey);
            return;
        }

        String value = StringUtils.join(args, ' ', 3, args.length);
        boolean add = operation == ModifyOperation.ADD;
        boolean empty = ConvertUtils.isEmpty(value);
        if (empty) {
            if (add) {
                plugin.sendMessage(sender, "value.noEmptyAdd");
                return;
            }

            value = "";
        }

        switch (setting) {
            case DESCRIPTION:
                if (empty) {
                    value = Alias.DEFAULT_DESCRIPTION;
                }

                alias.setDescription(value);
            case EXECUTABLE_AS_CONSOLE:
                boolean executableAsConsole;
                try {
                    executableAsConsole = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(executableAsConsole);
                alias.setExecutableAsConsole(executableAsConsole);
                break;
            case CONSOLE_MESSAGE:
                value = empty ? "" : MessageUtils.translateMessage(value);
                alias.setConsoleMessage(value);
                break;
            case ENABLED_WORLDS:
                Set<String> enabledWorlds = add ? alias.getEnabledWorlds() : new HashSet<>();
                if (!empty) {
                    int newValues = 0;

                    for (String worldName : ConvertUtils.split(value)) {
                        World world = Bukkit.getWorld(worldName);
                        if (world == null) {
                            plugin.sendMessage(sender, "value.worldNotFound", worldName);
                            return;
                        }

                        if (enabledWorlds.add(world.getName())) {
                            newValues++;
                        }
                    }
                    value = StringUtils.join(enabledWorlds, ", ");

                    if (add && newValues == 0) {
                        plugin.sendMessage(sender, "value.noNewValues", value, settingKey);
                        return;
                    }
                }

                alias.setEnabledWorlds(enabledWorlds);
                break;
            case WORLD_MESSAGE:
                value = MessageUtils.translateMessage(value);
                alias.setWorldMessage(value);
                break;
            case EXECUTION_ORDER:
                if (empty) {
                    plugin.sendMessage(sender, "value.noEmpty", settingKey);
                    return;
                }

                List<String> executionOrder = add ? alias.getExecutionOrder() : new ArrayList<>();
                for (String actionName : ConvertUtils.split(value)) {
                    Action action = alias.getAction(actionName);
                    if (action == null) {
                        plugin.sendMessage(sender, "action.notFound", actionName);
                        return;
                    }

                    executionOrder.add(action.getName());
                }

                value = StringUtils.join(executionOrder, ", ");
                alias.setExecutionOrder(executionOrder);
                break;
            case USAGE_CHECK_ENABLED:
                boolean usageCheckEnabled;
                try {
                    usageCheckEnabled = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(usageCheckEnabled);
                alias.setUsageCheckEnabled(usageCheckEnabled);
                break;
            case USAGE_CHECK_MIN_PARAMS:
                int minParams;
                try {
                    minParams = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    plugin.sendMessage(sender, "value.noInteger", value);
                    return;
                }

                if (minParams < 0) {
                    plugin.sendMessage(sender, "value.notLowerThan", settingKey, 0);
                } else if (minParams > alias.getUsageCheckMaxParams()) {
                    plugin.sendMessage(sender, "value.notHigherThanSetting", settingKey,
                                       MessageUtils.formatName(AliasSetting.USAGE_CHECK_MAX_PARAMS, true, ""));
                    return;
                }

                alias.setUsageCheckMinParams(minParams);
                break;
            case USAGE_CHECK_MAX_PARAMS:
                int maxParams;
                try {
                    maxParams = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    plugin.sendMessage(sender, "value.noInteger", value);
                    return;
                }

                if (maxParams < alias.getUsageCheckMinParams()) {
                    plugin.sendMessage(sender, "value.notLowerThanSetting", settingKey,
                                       MessageUtils.formatName(AliasSetting.USAGE_CHECK_MIN_PARAMS, true, ""));
                    return;
                }

                alias.setUsageCheckMaxParams(maxParams);
                break;
            case USAGE_CHECK_MESSAGE:
                value = MessageUtils.translateMessage(value);
                alias.setUsageCheckMessage(value);
                break;
            case PERMISSION_ENABLED:
                boolean permissionEnabled;
                try {
                    permissionEnabled = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(permissionEnabled);
                if (permissionEnabled && alias.getPermissionNode() == null) {
                    alias.setPermissionNode("simplealias.use." + name);
                }
                alias.setPermissionEnabled(permissionEnabled);
                break;
            case PERMISSION_NODE:
                alias.setPermissionNode(value);
                break;
            case PERMISSION_GROUPS:
                Set<String> permissionGroups = add ? alias.getPermissionGroups() : new HashSet<>();
                if (!empty) {
                    int newValues = 0;
                    VaultHook vault = plugin.getVaultHook();

                    for (String groupName : ConvertUtils.split(value)) {
                        if (vault.isEnabled()) {
                            groupName = vault.getExactGroupName(groupName);
                            if (groupName == null) {
                                plugin.sendMessage(sender, "value.groupNotFound");
                                return;
                            }
                        }

                        if (permissionGroups.add(groupName)) {
                            newValues++;
                        }
                    }
                    value = StringUtils.join(permissionGroups, ", ");

                    if (add && newValues == 0) {
                        plugin.sendMessage(sender, "value.noNewValues", value, settingKey);
                        return;
                    }
                }

                alias.setPermissionGroups(permissionGroups);
                break;
            case PERMISSION_MESSAGE:
                value = MessageUtils.translateMessage(value);
                alias.setPermissionMessage(value);
                break;
            case DELAY_ENABLED:
                boolean delayEnabled;
                try {
                    delayEnabled = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(delayEnabled);
                alias.setDelayEnabled(delayEnabled);
                break;
            case DELAY_CANCEL_ON_MOVE:
                boolean delayCancelOnMove;
                try {
                    delayCancelOnMove = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(delayCancelOnMove);
                alias.setDelayCancelOnMove(delayCancelOnMove);
                break;
            case DELAY_DURATION:
                int delayDuration;
                try {
                    delayDuration = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    plugin.sendMessage(sender, "value.noInteger", value);
                    return;
                }

                if (delayDuration < 1) {
                    plugin.sendMessage(sender, "value.notLowerThan", settingKey, 1);
                    return;
                }

                alias.setDelayDuration(delayDuration);
                break;
            case DELAY_MESSAGE:
                value = MessageUtils.translateMessage(value);
                alias.setDelayMessage(value);
                break;
            case DELAY_CANCEL_MESSAGE:
                value = MessageUtils.translateMessage(value);
                alias.setDelayCancelMessage(value);
                break;
            case COOLDOWN_ENABLED:
                boolean cooldownEnabled;
                try {
                    cooldownEnabled = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(cooldownEnabled);
                alias.setCooldownEnabled(cooldownEnabled);
                break;
            case COOLDOWN_DURATION:
                int cooldownDuration;
                try {
                    cooldownDuration = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    plugin.sendMessage(sender, "value.noInteger", value);
                    return;
                }

                if (cooldownDuration < 1) {
                    plugin.sendMessage(sender, "value.notLowerThan", settingKey, 1);
                    return;
                }

                alias.setCooldownDuration(cooldownDuration);
                break;
            case COOLDOWN_MESSAGE:
                value = MessageUtils.translateMessage(value);
                alias.setCooldownMessage(value);
                break;
            case COST_ENABLED:
                boolean costEnabled;
                try {
                    costEnabled = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(costEnabled);
                alias.setCostEnabled(costEnabled);
                break;
            case COST_AMOUNT:
                double costAmount;
                try {
                    costAmount = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    plugin.sendMessage(sender, "value.noDouble", value);
                    return;
                }

                if (costAmount < 0) {
                    plugin.sendMessage(sender, "value.notLowerThan", settingKey, 0);
                    return;
                } else if (costAmount == 0) {
                    plugin.sendMessage(sender, "value.notEqualTo", settingKey, 0);
                    return;
                }

                value = String.valueOf(costAmount);
                alias.setCostAmount(costAmount);
                break;
            case COST_MESSAGE:
                value = MessageUtils.translateMessage(value);
                alias.setCostMessage(value);
                break;
            case LOGGING_ENABLED:
                boolean loggingEnabled;
                try {
                    loggingEnabled = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(loggingEnabled);
                alias.setLoggingEnabled(loggingEnabled);
                break;
            case LOGGING_MESSAGE:
                value = MessageUtils.translateMessage(value);
                alias.setLoggingMessage(value);
                break;
            default:
                plugin.sendMessage(sender, "value.noModify", settingKey);
                return;
        }

        if (empty) {
            value = "empty";
        }

        try {
            alias.saveSettings();
            plugin.sendMessage(sender, "command.alias.modify." + operation + "Succeeded", settingKey, name, value);
        } catch (AliasException e) {
            plugin.sendMessage(sender, "command.alias.modify." + operation + "Failed", settingKey, name, value, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSuggestions(SimpleAlias plugin, CommandSender sender, String[] args) {
        AliasSetting setting = null;
        if (args.length >= 3) {
            setting = AliasSetting.fromNameOrPath(args[1]);
        }

        switch (args.length) {
            case 1:
                return plugin.getAliasManager().getAliasNames();
            case 2:
                return new ArrayList<>(AliasSetting.getNames());
            case 3:
                if (setting != null) {
                    return setting.getSupportedOperations().stream().map(ModifyOperation::toString).collect(Collectors.toList());
                }

                return new ArrayList<>(ModifyOperation.getNames());
            default:
                if (args.length >= 4 && setting != null) {
                    switch (setting) {
                        case EXECUTABLE_AS_CONSOLE:
                        case USAGE_CHECK_ENABLED:
                        case PERMISSION_ENABLED:
                        case DELAY_ENABLED:
                        case DELAY_CANCEL_ON_MOVE:
                        case COOLDOWN_ENABLED:
                        case COST_ENABLED:
                        case LOGGING_ENABLED:
                            return args.length > 4 ? null : Arrays.asList("true", "false");
                        case ENABLED_WORLDS:
                            return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
                        case EXECUTION_ORDER:
                            Alias alias = plugin.getAliasManager().getAlias(args[0]);
                            return alias == null ? null : alias.getActionNames();
                        case PERMISSION_GROUPS:
                            return Arrays.asList(plugin.getVaultHook().getGroups());
                        case USAGE_CHECK_MIN_PARAMS:
                        case USAGE_CHECK_MAX_PARAMS:
                        case DELAY_DURATION:
                        case COOLDOWN_DURATION:
                            return args.length > 4 ? null : Arrays.asList("1", "2", "5", "10");
                        case COST_AMOUNT:
                            return args.length > 4 ? null : Arrays.asList("10.0", "20.5", "50.0", "100.0");
                    }
                }

                return null;
        }
    }
}
