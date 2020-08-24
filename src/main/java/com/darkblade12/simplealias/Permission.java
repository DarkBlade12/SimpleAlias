package com.darkblade12.simplealias;

import com.darkblade12.simplealias.plugin.command.PermissionInfo;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Permission implements PermissionInfo {
    NONE("none") {
        @Override
        public boolean test(CommandSender sender) {
            return true;
        }
    },
    ALL("simplealias.*"),
    COMMAND_ALL("simplealias.command.*", ALL),
    COMMAND_CREATE("simplealias.command.create", COMMAND_ALL),
    COMMAND_SINGLE("simplealias.command.single", COMMAND_ALL),
    COMMAND_MULTIPLE("simplealias.command.multiple", COMMAND_ALL),
    COMMAND_MESSAGE("simplealias.command.message", COMMAND_ALL),
    COMMAND_REMOVE("simplealias.command.remove", COMMAND_ALL),
    COMMAND_RENAME("simplealias.command.rename", COMMAND_ALL),
    COMMAND_MODIFY("simplealias.command.modify", COMMAND_ALL),
    COMMAND_CREATE_ACTION("simplealias.command.createaction", COMMAND_ALL),
    COMMAND_REMOVE_ACTION("simplealias.command.removeaction", COMMAND_ALL),
    COMMAND_RENAME_ACTION("simplealias.command.renameaction", COMMAND_ALL),
    COMMAND_MODIFY_ACTION("simplealias.command.modifyaction", COMMAND_ALL),
    COMMAND_LIST("simplealias.command.list", COMMAND_ALL),
    COMMAND_DETAILS("simplealias.command.details", COMMAND_ALL),
    COMMAND_RELOAD("simplealias.command.reload", COMMAND_ALL),
    BYPASS_ALL("simplealias.bypass.*", ALL),
    BYPASS_ENABLED_WORLDS("simplealias.bypass.enabledworlds", BYPASS_ALL),
    BYPASS_DELAY("simplealias.bypass.delay", BYPASS_ALL),
    BYPASS_COOLDOWN("simplealias.bypass.cooldown", BYPASS_ALL),
    BYPASS_COST("simplealias.bypass.cost", BYPASS_ALL),
    USE_ALL("simplealias.use.*", ALL);

    private final String name;
    private final Permission parent;

    Permission(String name, Permission parent) {
        this.name = name;
        this.parent = parent;
    }

    Permission(String name) {
        this(name, null);
    }

    @Override
    public boolean test(CommandSender sender) {
        return sender.hasPermission(name) || testParent(sender);
    }

    public boolean testParent(CommandSender sender) {
        return parent != null && parent.test(sender);
    }

    @Override
    public String getName() {
        return name;
    }

    public Permission getParent() {
        return parent;
    }

    public List<Permission> getChildren() {
        return Arrays.stream(values()).filter(p -> p.getParent() == this).collect(Collectors.toList());
    }
}
