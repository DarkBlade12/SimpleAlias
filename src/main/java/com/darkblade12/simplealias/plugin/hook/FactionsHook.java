package com.darkblade12.simplealias.plugin.hook;

import com.massivecraft.factions.entity.MPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FactionsHook extends Hook {
    public static final String DEFAULT_FACTION = "Default";

    public FactionsHook() {
        super("Factions");
    }

    @Override
    protected boolean initialize() {
        Plugin massiveCore = Bukkit.getServer().getPluginManager().getPlugin("MassiveCore");
        return massiveCore != null && massiveCore.isEnabled();
    }

    public String getFaction(Player player) {
        String faction = MPlayer.get(player).getFactionName();
        return faction == null ? DEFAULT_FACTION : faction;
    }
}
