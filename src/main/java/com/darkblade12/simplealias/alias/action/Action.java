package com.darkblade12.simplealias.alias.action;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.DynamicVariable;
import com.darkblade12.simplealias.nameable.Nameable;
import com.darkblade12.simplealias.plugin.hook.FactionsHook;
import com.darkblade12.simplealias.plugin.hook.VaultHook;
import com.darkblade12.simplealias.replacer.Replacer;
import com.darkblade12.simplealias.util.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;

public abstract class Action implements Nameable, Comparable<Action> {
    protected String name;
    protected Set<String> enabledWorlds;
    protected Set<String> enabledPermissionNodes;
    protected Set<String> enabledPermissionGroups;
    protected Map<Integer, String> enabledParams;
    protected int priority;
    protected boolean translateColorCodes;

    protected Action(String name, Set<String> enabledWorlds, Set<String> enabledPermissionNodes, Set<String> enabledPermissionGroups,
                     Map<Integer, String> enabledParams, int priority, boolean translateColorCodes) {
        this.name = name;
        this.enabledWorlds = enabledWorlds;
        this.enabledPermissionNodes = enabledPermissionNodes;
        this.enabledPermissionGroups = enabledPermissionGroups;
        this.enabledParams = enabledParams;
        this.priority = priority;
        this.translateColorCodes = translateColorCodes;
    }

    public abstract void execute(SimpleAlias plugin, CommandSender sender, String[] params);

    protected String replaceVariables(String target, SimpleAlias plugin, CommandSender sender, String[] params) {
        if (translateColorCodes) {
            params = MessageUtils.translateArguments(params);
        }

        Replacer.ReplacerBuilder builder = Replacer.builder().with(DynamicVariable.SENDER_NAME, sender.getName());

        if (sender instanceof Player) {
            Player player = (Player) sender;
            builder.with(DynamicVariable.SENDER_UUID, player.getUniqueId())
                   .with(DynamicVariable.WORLD_NAME, player.getWorld().getName());

            VaultHook vault = plugin.getVaultHook();
            if (vault.isEnabled()) {
                if (vault.isEconomyEnabled()) {
                    builder.with(DynamicVariable.MONEY_BALANCE, vault.formatCurrency(vault.getBalance(player)));
                }
                if (vault.isPermissionEnabled() && vault.hasPermissionGroupSupport()) {
                    builder.with(DynamicVariable.GROUP_NAME, vault.getPrimaryGroup(player));
                }
            }

            FactionsHook factions = plugin.getFactionsHook();
            if (factions.isEnabled()) {
                builder.with(DynamicVariable.FACTION_NAME, factions.getFaction(player));
            }
        }

        if (params.length > 0) {
            builder.with(DynamicVariable.PARAMS, StringUtils.join(params, " "));

            Matcher matcher = DynamicVariable.PARAMS_INDEX.getPattern().matcher(target);
            while (matcher.find()) {
                String variable = matcher.group();
                String range = matcher.group(1);
                int separatorIndex = range.indexOf('-');
                if (separatorIndex == -1) {
                    int index = Integer.parseInt(range);
                    if (index < params.length) {
                        builder.with(variable, params[index]);
                    }
                    continue;
                }

                int rangeStart = separatorIndex == 0 ? 0 : Integer.parseInt(range.substring(0, separatorIndex));
                int rangeEnd = separatorIndex == range.length() - 1
                               ? params.length - 1
                               : Integer.parseInt(range.substring(separatorIndex + 1));
                if (rangeStart >= params.length) {
                    continue;
                }

                if (rangeStart == rangeEnd) {
                    builder.with(variable, params[rangeStart]);
                } else if (rangeStart < rangeEnd) {
                    String[] includedParams = Arrays.copyOfRange(params, rangeStart, Math.min(rangeEnd, params.length) + 1);
                    builder.with(variable, StringUtils.join(includedParams, " "));
                }
            }
        }

        String result = builder.build().replaceAll(target);
        return DynamicVariable.createEmptyReplacement().applyTo(result);
    }

    @Override
    public int compareTo(Action other) {
        return Integer.compare(priority, other.priority);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEnabledWorlds(Set<String> enabledWorlds) {
        this.enabledWorlds = enabledWorlds;
    }

    public void setEnabledPermissionNodes(Set<String> enabledPermissionNodes) {
        this.enabledPermissionNodes = enabledPermissionNodes;
    }

    public void setEnabledPermissionGroups(Set<String> enabledPermissionGroups) {
        this.enabledPermissionGroups = enabledPermissionGroups;
    }

    public void setEnabledParams(Map<Integer, String> enabledParams) {
        this.enabledParams = enabledParams;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setTranslateColorCodes(boolean translateColorCodes) {
        this.translateColorCodes = translateColorCodes;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Set<String> getEnabledWorlds() {
        return enabledWorlds;
    }

    public Set<String> getEnabledPermissionNodes() {
        return enabledPermissionNodes;
    }

    public Set<String> getEnabledPermissionGroups() {
        return enabledPermissionGroups;
    }

    public Map<Integer, String> getEnabledParams() {
        return enabledParams;
    }

    public boolean isEnabled(SimpleAlias plugin, CommandSender sender, String[] params) {
        for (Entry<Integer, String> entry : enabledParams.entrySet()) {
            int index = entry.getKey();
            if (index >= params.length || !params[index].equalsIgnoreCase(entry.getValue())) {
                return false;
            }
        }

        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        if (!enabledWorlds.isEmpty() && !enabledWorlds.contains(player.getWorld().getName())) {
            return false;
        }

        boolean hasPermissions = !enabledPermissionNodes.isEmpty();
        if (hasPermissions && enabledPermissionNodes.stream().anyMatch(player::hasPermission)) {
            return true;
        }

        if (enabledPermissionGroups.isEmpty()) {
            return !hasPermissions;
        }

        return plugin.getVaultHook().isInAnyGroup(player, enabledPermissionGroups);
    }

    public int getPriority() {
        return priority;
    }

    public boolean hasTranslateColorCodes() {
        return translateColorCodes;
    }

    public abstract ActionType getType();
}
