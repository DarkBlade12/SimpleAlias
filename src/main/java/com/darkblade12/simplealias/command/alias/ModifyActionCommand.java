package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasException;
import com.darkblade12.simplealias.alias.AliasSetting;
import com.darkblade12.simplealias.alias.ModifyOperation;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.ActionSetting;
import com.darkblade12.simplealias.alias.action.ActionType;
import com.darkblade12.simplealias.alias.action.CommandAction;
import com.darkblade12.simplealias.alias.action.Executor;
import com.darkblade12.simplealias.alias.action.MessageAction;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModifyActionCommand extends CommandBase<SimpleAlias> {
    public ModifyActionCommand() {
        super("modifyaction", Permission.COMMAND_MODIFY_ACTION, false, "<name>", "<action_name>", "<setting>", "<set|add>", "<value>");
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

        String actionName = args[1];
        Action action = alias.getAction(actionName);
        if (action == null) {
            plugin.sendMessage(sender, "action.notFound", actionName);
            return;
        }
        actionName = action.getName();

        String settingName = args[2];
        ActionSetting setting = ActionSetting.fromName(settingName);
        if (setting == null) {
            plugin.sendMessage(sender, "command.alias.modifyaction.settingNotFound", settingName);
            return;
        }
        settingName = MessageUtils.formatName(setting, true, "");

        String operationName = args[3];
        ModifyOperation operation = ModifyOperation.fromName(operationName);
        if (operation == null) {
            plugin.sendMessage(sender, "command.alias.modify.operationNotFound", operationName);
            return;
        } else if (!setting.isSupported(operation)) {
            plugin.sendMessage(sender, "command.alias.modify.operationNotSupported", operationName, settingName);
            return;
        }

        String value = StringUtils.join(args, ' ', 4, args.length);
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
            case ENABLED_WORLDS:
                Set<String> enabledWorlds = add ? action.getEnabledWorlds() : new HashSet<>();
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
                        plugin.sendMessage(sender, "value.noNewValues", value, settingName);
                        return;
                    }
                }

                action.setEnabledWorlds(enabledWorlds);
                break;
            case ENABLED_PERMISSION_NODES:
                Set<String> enabledPermissionNodes = add ? action.getEnabledPermissionNodes() : new HashSet<>();
                if (!empty) {
                    int newValues = 0;

                    for (String permissionNode : ConvertUtils.split(value)) {
                        if (enabledPermissionNodes.add(permissionNode)) {
                            newValues++;
                        }
                    }
                    value = StringUtils.join(enabledPermissionNodes, ", ");

                    if (add && newValues == 0) {
                        plugin.sendMessage(sender, "value.noNewValues", value, settingName);
                        return;
                    }
                }

                action.setEnabledPermissionNodes(enabledPermissionNodes);
                break;
            case ENABLED_PERMISSION_GROUPS:
                Set<String> enabledPermissionGroups = add ? action.getEnabledPermissionGroups() : new HashSet<>();
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

                        if (enabledPermissionGroups.add(groupName)) {
                            newValues++;
                        }
                    }
                    value = StringUtils.join(enabledPermissionGroups, ", ");

                    if (add && newValues == 0) {
                        plugin.sendMessage(sender, "value.noNewValues", value, settingName);
                        return;
                    }
                }

                action.setEnabledPermissionGroups(enabledPermissionGroups);
                break;
            case ENABLED_PARAMS:
                Map<Integer, String> enabledParams = add ? action.getEnabledParams() : new HashMap<>();
                if (!empty) {
                    int newValues = 0;

                    for (String param : ConvertUtils.split(value)) {
                        String[] values = param.split("@");
                        if (values.length != 2) {
                            return;
                        }

                        int index;
                        try {
                            index = Integer.parseInt(values[1]);
                        } catch (NumberFormatException e) {
                            plugin.sendMessage(sender, "value.noInteger", values[1]);
                            return;
                        }

                        if (index < 0) {
                            plugin.sendMessage(sender, "command.alias.modifyaction.indexLowerThan", settingName, 0);
                            return;
                        } else if (alias.isUsageCheckEnabled() && index > alias.getUsageCheckMaxParams()) {
                            plugin.sendMessage(sender, "command.alias.modifyaction.indexLowerThan", settingName,
                                               MessageUtils.formatName(AliasSetting.USAGE_CHECK_MAX_PARAMS, true, ""));
                            return;
                        }

                        if (!enabledParams.containsKey(index)) {
                            enabledParams.put(index, values[0]);
                            newValues++;
                        }
                    }

                    if (add && newValues == 0) {
                        plugin.sendMessage(sender, "value.noNewValues", value, settingName);
                        return;
                    }
                }

                action.setEnabledParams(enabledParams);
                break;
            case PRIORITY:
                int priority;
                try {
                    priority = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    plugin.sendMessage(sender, "value.noInteger", value);
                    return;
                }

                action.setPriority(priority);
                break;
            case TRANSLATE_COLOR_CODES:
                boolean translateColorCodes;
                try {
                    translateColorCodes = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(translateColorCodes);
                action.setTranslateColorCodes(translateColorCodes);
                break;
            case COMMAND:
                if (action.getType() != ActionType.COMMAND) {
                    plugin.sendMessage(sender, "command.alias.modifyaction.onlyCommand", settingName);
                    return;
                }

                value = StringUtils.removeStart(value, "/");
                if (value.isEmpty()) {
                    plugin.sendMessage(sender, "value.noEmpty", settingName);
                    return;
                } else if (value.split(" ")[0].equalsIgnoreCase(name)) {
                    plugin.sendMessage(sender, "alias.noSelfExecution");
                    return;
                }

                ((CommandAction) action).setCommand(value);
                break;
            case EXECUTOR:
                if (action.getType() != ActionType.COMMAND) {
                    plugin.sendMessage(sender, "command.alias.modifyaction.onlyCommand", settingName);
                    return;
                } else if (empty) {
                    plugin.sendMessage(sender, "value.noEmpty", settingName);
                    return;
                }

                Executor executor = Executor.fromName(value);
                if (executor == null) {
                    plugin.sendMessage(sender, "command.alias.modifyaction.executorNotFound", value);
                    return;
                }

                value = MessageUtils.formatName(executor);
                ((CommandAction) action).setExecutor(executor);
                break;
            case GRANT_PERMISSION:
                if (action.getType() != ActionType.COMMAND) {
                    plugin.sendMessage(sender, "command.alias.modifyaction.onlyCommand", settingName);
                    return;
                }

                boolean grantPermission;
                try {
                    grantPermission = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(grantPermission);
                ((CommandAction) action).setGrantPermission(grantPermission);
                break;
            case SILENT:
                if (action.getType() != ActionType.COMMAND) {
                    plugin.sendMessage(sender, "command.alias.modifyaction.onlyCommand", settingName);
                    return;
                }

                boolean silent;
                try {
                    silent = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(silent);
                ((CommandAction) action).setSilent(silent);
                break;
            case MESSAGE:
                if (action.getType() != ActionType.MESSAGE) {
                    plugin.sendMessage(sender, "command.alias.modifyaction.onlyMessage", settingName);
                    return;
                } else if (empty) {
                    plugin.sendMessage(sender, "value.noEmpty", settingName);
                    return;
                }

                value = MessageUtils.translateMessage(value);
                ((MessageAction) action).setMessage(value);
                break;
            case BROADCAST:
                if (action.getType() != ActionType.MESSAGE) {
                    plugin.sendMessage(sender, "command.alias.modifyaction.onlyMessage", settingName);
                    return;
                }

                boolean broadcast;
                try {
                    broadcast = ConvertUtils.convertToBoolean(value);
                } catch (Exception e) {
                    plugin.sendMessage(sender, "value.noBoolean", value);
                    return;
                }

                value = String.valueOf(broadcast);
                ((MessageAction) action).setBroadcast(broadcast);
                break;
            default:
                plugin.sendMessage(sender, "value.noModify", settingName);
                return;
        }

        if (empty) {
            value = "empty";
        }

        try {
            alias.saveSettings();
            plugin.sendMessage(sender, "command.alias.modifyaction." + operation + "Succeeded", settingName, actionName, name, value);
        } catch (AliasException e) {
            plugin.sendMessage(sender, "command.alias.modifyaction." + operation + "Failed", settingName, actionName, name, value,
                               e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getSuggestions(SimpleAlias plugin, CommandSender sender, String[] args) {
        ActionSetting setting = null;
        if (args.length >= 4) {
            setting = ActionSetting.fromName(args[2]);
        }

        switch (args.length) {
            case 1:
                return plugin.getAliasManager().getAliasNames();
            case 2:
                Alias alias = plugin.getAliasManager().getAlias(args[0]);
                return alias == null ? null : alias.getActionNames();
            case 3:
                return new ArrayList<>(ActionSetting.getNames());
            case 4:
                if (setting != null) {
                    return setting.getSupportedOperations().stream().map(ModifyOperation::toString).collect(Collectors.toList());
                }

                return new ArrayList<>(ModifyOperation.getNames());
            default:
                if (args.length >= 5 && setting != null) {
                    switch (setting) {
                        case ENABLED_WORLDS:
                            return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
                        case ENABLED_PERMISSION_GROUPS:
                            return Arrays.asList(plugin.getVaultHook().getGroups());
                        case TRANSLATE_COLOR_CODES:
                        case GRANT_PERMISSION:
                        case BROADCAST:
                            return args.length > 5 ? null : Arrays.asList("true", "false");
                        case PRIORITY:
                            return args.length > 5 ? null : Arrays.asList("0", "1", "2", "3");
                        case EXECUTOR:
                            return args.length > 5 ? null : new ArrayList<>(Executor.getNames());
                    }
                }

                return null;
        }
    }
}
