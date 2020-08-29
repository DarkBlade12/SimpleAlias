package com.darkblade12.simplealias.command.alias;

import com.darkblade12.simplealias.Permission;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.Alias;
import com.darkblade12.simplealias.alias.AliasSection;
import com.darkblade12.simplealias.alias.AliasSetting;
import com.darkblade12.simplealias.alias.action.Action;
import com.darkblade12.simplealias.alias.action.ActionSetting;
import com.darkblade12.simplealias.alias.action.CommandAction;
import com.darkblade12.simplealias.alias.action.MessageAction;
import com.darkblade12.simplealias.plugin.command.CommandBase;
import com.darkblade12.simplealias.util.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class DetailsCommand extends CommandBase<SimpleAlias> {
    public DetailsCommand() {
        super("details", Permission.COMMAND_DETAILS, "<name>");
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

        plugin.sendMessage(sender, "command.alias.details.message", name, getDetails(plugin, alias));
    }

    @Override
    public List<String> getSuggestions(SimpleAlias plugin, CommandSender sender, String[] args) {
        return args.length == 1 ? plugin.getAliasManager().getAliasNames() : null;
    }

    private String getDetails(SimpleAlias plugin, Alias alias) {
        StringBuilder builder = new StringBuilder();
        builder.append(getLine(plugin, 1, AliasSetting.DESCRIPTION, alias.getDescription()));
        builder.append(getLine(plugin, 1, AliasSetting.EXECUTABLE_AS_CONSOLE, alias.isExecutableAsConsole()));
        builder.append(getLine(plugin, 1, AliasSetting.CONSOLE_MESSAGE, alias.getConsoleMessage()));
        builder.append(getLine(plugin, 1, AliasSetting.ENABLED_WORLDS, alias.getEnabledWorlds()));
        builder.append(getLine(plugin, 1, AliasSetting.WORLD_MESSAGE, alias.getWorldMessage()));
        builder.append(getLine(plugin, 1, AliasSetting.EXECUTION_ORDER, alias.getExecutionOrder()));

        builder.append(getLine(plugin, 1, AliasSection.ACTIONS, ""));
        for (Action action : alias.getActions()) {
            builder.append(getLine(plugin, 2, action.getName(), ""));
            builder.append(getLine(plugin, 3, ActionSetting.TYPE, action.getType()));
            builder.append(getLine(plugin, 3, ActionSetting.ENABLED_WORLDS, action.getEnabledWorlds()));
            builder.append(getLine(plugin, 3, ActionSetting.ENABLED_PERMISSION_NODES, action.getEnabledPermissionNodes()));
            builder.append(getLine(plugin, 3, ActionSetting.ENABLED_PERMISSION_GROUPS, action.getEnabledPermissionGroups()));
            builder.append(getLine(plugin, 3, ActionSetting.ENABLED_PARAMS, action.getEnabledParams()));
            builder.append(getLine(plugin, 3, ActionSetting.PRIORITY, action.getPriority()));
            builder.append(getLine(plugin, 3, ActionSetting.TRANSLATE_COLOR_CODES, action.hasTranslateColorCodes()));

            if (action instanceof CommandAction) {
                CommandAction commandAction = (CommandAction) action;
                builder.append(getLine(plugin, 3, ActionSetting.COMMAND, commandAction.getCommand()));
                builder.append(getLine(plugin, 3, ActionSetting.EXECUTOR, commandAction.getExecutor()));
                builder.append(getLine(plugin, 3, ActionSetting.GRANT_PERMISSION, commandAction.hasGrantPermission()));
                builder.append(getLine(plugin, 3, ActionSetting.SILENT, commandAction.isSilent()));
            } else if (action instanceof MessageAction) {
                MessageAction messageAction = (MessageAction) action;
                builder.append(getLine(plugin, 3, ActionSetting.MESSAGE, messageAction.getMessage()));
                builder.append(getLine(plugin, 3, ActionSetting.BROADCAST, messageAction.isBroadcast()));
            }
        }

        builder.append(getLine(plugin, 1, AliasSection.USAGE_CHECK, ""));
        builder.append(getLine(plugin, 2, AliasSetting.USAGE_CHECK_ENABLED, alias.isUsageCheckEnabled()));
        builder.append(getLine(plugin, 2, AliasSetting.USAGE_CHECK_MIN_PARAMS, alias.getUsageCheckMinParams()));
        builder.append(getLine(plugin, 2, AliasSetting.USAGE_CHECK_MAX_PARAMS, alias.getUsageCheckMaxParams()));
        builder.append(getLine(plugin, 2, AliasSetting.USAGE_CHECK_MESSAGE, alias.getUsageCheckMessage()));

        builder.append(getLine(plugin, 1, AliasSection.PERMISSION, ""));
        builder.append(getLine(plugin, 2, AliasSetting.PERMISSION_ENABLED, alias.isPermissionEnabled()));
        builder.append(getLine(plugin, 2, AliasSetting.PERMISSION_NODE, alias.getPermissionNode()));
        builder.append(getLine(plugin, 2, AliasSetting.PERMISSION_GROUPS, alias.getPermissionGroups()));
        builder.append(getLine(plugin, 2, AliasSetting.PERMISSION_MESSAGE, alias.getPermissionMessage()));

        builder.append(getLine(plugin, 1, AliasSection.DELAY, ""));
        builder.append(getLine(plugin, 2, AliasSetting.DELAY_ENABLED, alias.isDelayEnabled()));
        builder.append(getLine(plugin, 2, AliasSetting.DELAY_CANCEL_ON_MOVE, alias.isDelayCancelOnMove()));
        builder.append(getLine(plugin, 2, AliasSetting.DELAY_DURATION, alias.getDelayDuration()));
        builder.append(getLine(plugin, 2, AliasSetting.DELAY_MESSAGE, alias.getDelayMessage()));
        builder.append(getLine(plugin, 2, AliasSetting.DELAY_CANCEL_MESSAGE, alias.getDelayCancelMessage()));

        builder.append(getLine(plugin, 1, AliasSection.COOLDOWN, ""));
        builder.append(getLine(plugin, 2, AliasSetting.COOLDOWN_ENABLED, alias.isCooldownEnabled()));
        builder.append(getLine(plugin, 2, AliasSetting.COOLDOWN_DURATION, alias.getCooldownDuration()));
        builder.append(getLine(plugin, 2, AliasSetting.COOLDOWN_MESSAGE, alias.getCooldownMessage()));

        builder.append(getLine(plugin, 1, AliasSection.COST, ""));
        builder.append(getLine(plugin, 2, AliasSetting.COST_ENABLED, alias.isCostEnabled()));
        builder.append(getLine(plugin, 2, AliasSetting.COST_AMOUNT, alias.getCostAmount()));
        builder.append(getLine(plugin, 2, AliasSetting.COST_MESSAGE, alias.getCostMessage()));

        builder.append(getLine(plugin, 1, AliasSection.LOGGING, ""));
        builder.append(getLine(plugin, 2, AliasSetting.LOGGING_ENABLED, alias.isLoggingEnabled()));
        builder.append(getLine(plugin, 2, AliasSetting.LOGGING_MESSAGE, alias.getLoggingMessage()));

        return builder.toString();
    }

    private String getLine(SimpleAlias plugin, int indentationLevel, String key, Object value) {
        String spacer = IntStream.range(0, indentationLevel).mapToObj(i -> " ").collect(Collectors.joining());
        return "\n§r" + spacer + plugin.formatMessage("command.alias.details.line", key, String.valueOf(value));
    }

    private String getLine(SimpleAlias plugin, int indentationLevel, Enum<?> key, Object value) {
        return getLine(plugin, indentationLevel, MessageUtils.formatName(key, true, ""), value);
    }

    private String getLine(SimpleAlias plugin, int indentationLevel, Enum<?> key, Collection<?> value) {
        return getLine(plugin, indentationLevel, key, value.isEmpty() ? "[]" : StringUtils.join(value, ", "));
    }

    private String getLine(SimpleAlias plugin, int indentationLevel, Enum<?> key, Map<?, ?> value) {
        List<String> values = value.entrySet().stream().map(e -> e.getValue() + "@" + e.getKey()).collect(Collectors.toList());
        return getLine(plugin, indentationLevel, key, values);
    }

    private String getLine(SimpleAlias plugin, int indentationLevel, Enum<?> key, Enum<?> value) {
        return getLine(plugin, indentationLevel, MessageUtils.formatName(key, true, ""), MessageUtils.formatName(value, true, ""));
    }
}
