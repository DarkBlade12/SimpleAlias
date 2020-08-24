package com.darkblade12.simplealias.cooldown;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.plugin.reader.JsonReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CooldownMap {
    private transient SimpleAlias plugin;
    private transient JsonReader<SimpleAlias, CooldownMap> reader;
    private final UUID playerId;
    private final Map<String, Cooldown> cooldownByAlias;

    public CooldownMap(SimpleAlias plugin, UUID playerId) {
        super();
        this.plugin = plugin;
        this.playerId = playerId;
        this.cooldownByAlias = new HashMap<>();

        CooldownManager manager = plugin.getCooldownManager();
        reader = new JsonReader<>(null, CooldownMap.class, new File(manager.getDataDirectory(), playerId + ".json"));
    }

    public static CooldownMap fromFile(SimpleAlias plugin, File file) {
        JsonReader<SimpleAlias, CooldownMap> reader = new JsonReader<>(null, CooldownMap.class, file);
        CooldownMap map = reader.read();
        if (map != null) {
            map.plugin = plugin;
            map.reader = reader;
        }

        return map;
    }

    public void put(String aliasName, Cooldown cooldown) {
        cooldownByAlias.put(aliasName, cooldown);
    }

    public Cooldown get(String aliasName) {
        return cooldownByAlias.get(aliasName);
    }

    public boolean remove(String aliasName) {
        return cooldownByAlias.remove(aliasName) != null;
    }

    public boolean removeExpired() {
        AliasManager manager = plugin.getAliasManager();
        return cooldownByAlias.entrySet().removeIf(e -> !manager.hasAlias(e.getKey()) || e.getValue().isExpired());
    }

    public void saveFile() {
        reader.save(this);
    }

    public boolean deleteFile() {
        return reader.deleteOutputFile();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public boolean isEmpty() {
        return cooldownByAlias.isEmpty();
    }
}
