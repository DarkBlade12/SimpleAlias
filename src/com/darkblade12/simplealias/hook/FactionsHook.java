package com.darkblade12.simplealias.hook;

import org.bukkit.entity.Player;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.UPlayer;

public class FactionsHook extends Hook<Factions> {
	public static String getFaction(Player p) {
		return !enabled ? "Default" : UPlayer.get(p).getFactionName();
	}

	@Override
	public String getPluginName() {
		return "Factions";
	}
}