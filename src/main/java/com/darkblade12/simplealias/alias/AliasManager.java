package com.darkblade12.simplealias.alias;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.nameable.NameableComparator;
import com.darkblade12.simplealias.nameable.NameableList;
import com.darkblade12.simplealias.plugin.Manager;
import com.darkblade12.simplealias.plugin.reader.ConfigurationReader;
import com.darkblade12.simplealias.plugin.settings.InvalidValueException;
import com.darkblade12.simplealias.settings.Settings;
import com.darkblade12.simplealias.util.MessageUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class AliasManager extends Manager<SimpleAlias> {
    private static final NameableComparator<Alias> COMPARATOR = new NameableComparator<>();
    private final NameableList<Alias> aliases;
    private final Set<UUID> uncheckedPlayers;

    public AliasManager(SimpleAlias plugin) {
        super(plugin, new File("plugins/SimpleAlias/aliases/"));
        aliases = new NameableList<>();
        uncheckedPlayers = new HashSet<>();
    }

    @Override
    protected void onEnable() {
        loadAliases();

        int count = aliases.size();
        plugin.logInfo(count + " alias" + (count == 1 ? "" : "es") + " loaded.");
    }

    @Override
    protected void onDisable() {
        unloadAliases();
        aliases.clear();
        uncheckedPlayers.clear();
    }

    private void loadAliases() {
        if (!dataDirectory.isDirectory()) {
            return;
        }

        File[] aliasFiles = dataDirectory.listFiles((dir, name) -> ConfigurationReader.isConfiguration(name));
        if (aliasFiles == null) {
            return;
        }

        for (File file : aliasFiles) {
            String name = ConfigurationReader.stripExtension(file);

            try {
                aliases.add(new Alias(plugin, name, false));
            } catch (AliasException | InvalidValueException e) {
                plugin.logException("Failed to load alias {1}: {0}", e, name);
            }
        }
    }

    private void unloadAliases() {
        for (int i = 0; i < aliases.size(); i++) {
            Alias alias = aliases.get(i);
            try {
                alias.unregisterCommand();
            } catch (AliasException e) {
                plugin.logException("{0}", e);
            }
        }
    }

    public Alias createAlias(String name) throws AliasException, InvalidValueException {
        if (aliases.contains(name)) {
            throw new IllegalArgumentException("Alias name is not unique.");
        }

        Alias alias = new Alias(plugin, name, true);
        if (plugin.getSettings().hasCommandSync()) {
            AliasCommand.syncCommands();
        }

        aliases.add(alias);
        return alias;
    }

    public void removeAlias(Alias alias) throws AliasException {
        alias.unregisterCommand();
        alias.deleteSettings();

        if (plugin.getSettings().hasCommandSync()) {
            AliasCommand.syncCommands();
        }
        aliases.remove(alias.getName());
    }

    public void addUncheckedPlayer(Player player) {
        uncheckedPlayers.add(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String[] commandParts = StringUtils.removeStart(event.getMessage(), "/").split(" ");
        String command = commandParts[0].toLowerCase();
        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        Settings settings = plugin.getSettings();

        if (uncheckedPlayers.contains(id)) {
            uncheckedPlayers.remove(id);
            return;
        } else if (player.isOp() || !settings.isCommandDisabled(command)) {
            return;
        }

        String message = settings.getDisabledMessage(command);
        if (!MessageUtils.isBlank(message)) {
            player.sendMessage(message);
        }

        event.setCancelled(true);
    }

    public NameableList<Alias> getAliases() {
        NameableList<Alias> clone = new NameableList<>(aliases);
        clone.sort(COMPARATOR);
        return clone;
    }

    public int getAliasCount() {
        return aliases.size();
    }

    public List<String> getAliasNames() {
        List<String> names = aliases.getNames();
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public Alias getAlias(String name) {
        return aliases.get(name);
    }

    public boolean hasAlias(String name) {
        return aliases.contains(name);
    }
}