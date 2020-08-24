package com.darkblade12.simplealias.cooldown;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.plugin.Manager;
import com.darkblade12.simplealias.plugin.reader.JsonReader;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CooldownManager extends Manager<SimpleAlias> {
    private final List<CooldownMap> cooldownMaps;

    public CooldownManager(SimpleAlias plugin) {
        super(plugin, new File("plugins/SimpleAlias/cooldowns/"));
        cooldownMaps = new ArrayList<>();
    }

    @Override
    protected void onEnable() {
        loadMaps();

        int count = cooldownMaps.size();
        plugin.logInfo("{0} cooldown map{1} loaded.", count, count == 1 ? "" : "s");
    }

    @Override
    protected void onDisable() {
        unloadMaps();
        cooldownMaps.clear();
    }

    private void loadMaps() {
        if (!dataDirectory.isDirectory()) {
            return;
        }

        File[] cooldownFiles = dataDirectory.listFiles((dir, name) -> JsonReader.isJson(name));
        if (cooldownFiles == null) {
            return;
        }

        for (File file : cooldownFiles) {
            CooldownMap map = CooldownMap.fromFile(plugin, file);
            if (map == null) {
                plugin.logWarning("Failed to load the cooldown map {0}.", file.getName());
                continue;
            }

            cooldownMaps.add(map);
        }
    }

    private void unloadMaps() {
        for (int i = 0; i < cooldownMaps.size(); i++) {
            CooldownMap map = cooldownMaps.get(i);
            boolean changed = map.removeExpired();
            if (map.isEmpty()) {
                map.deleteFile();
            } else if (changed) {
                map.saveFile();
            }
        }
    }

    public void register(Player player, String aliasName, Cooldown cooldown) {
        CooldownMap map = getMap(player);
        map.put(aliasName, cooldown);
        map.removeExpired();
        map.saveFile();
    }

    public void unregister(Player player, String aliasName) {
        CooldownMap map = getMap(player);
        boolean changed = false;
        if (!map.isEmpty()) {
            changed = map.remove(aliasName) | map.removeExpired();
        }

        if (map.isEmpty()) {
            map.deleteFile();
        } else if (changed) {
            map.saveFile();
        }
    }

    public CooldownMap getMap(Player player) {
        UUID playerId = player.getUniqueId();
        CooldownMap map = cooldownMaps.stream().filter(c -> c.getPlayerId() == playerId).findFirst().orElse(null);
        if (map == null) {
            map = new CooldownMap(plugin, playerId);
            cooldownMaps.add(map);
        }

        return map;
    }

    public Cooldown getCooldown(Player player, String aliasName) {
        return getMap(player).get(aliasName);
    }
}
