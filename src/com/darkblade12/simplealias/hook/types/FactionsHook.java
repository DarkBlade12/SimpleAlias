package com.darkblade12.simplealias.hook.types;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.darkblade12.simplealias.hook.Hook;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.MPlayer;

public final class FactionsHook extends Hook<Factions> {
	@Override
	protected boolean onEnable() {
		return Bukkit.getServer().getPluginManager().getPlugin("MassiveCore") != null;
	}

	public String getFaction(Player p) {
		String faction = MPlayer.get(p).getFactionName();
		return faction == null ? "N/A" : faction;
	}

	@Override
	public String getPluginName() {
		return "Factions";
	}
}