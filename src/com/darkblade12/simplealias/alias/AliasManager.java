package com.darkblade12.simplealias.alias;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.manager.Manager;
import com.darkblade12.simplealias.nameable.NameableComparator;
import com.darkblade12.simplealias.nameable.NameableList;

public final class AliasManager extends Manager {
	private static final File DIRECTORY = new File("plugins/SimpleAlias/aliases/");
	private static final NameableComparator<Alias> COMPARATOR = new NameableComparator<Alias>();
	private NameableList<Alias> aliases;
	private Set<String> uncheckedPlayers;

	@Override
	public boolean onEnable() {
		loadAliases();
		uncheckedPlayers = new HashSet<String>();
		registerEvents();
		return true;
	}

	@Override
	public void onDisable() {
		Logger l = SimpleAlias.logger();
		for (int i = 0; i < aliases.size(); i++) {
			Alias a = aliases.get(i);
			if (!a.getCommand().unregister())
				l.warning("Failed to unregister alias '" + a.getName() + "' from the commands!");
		}
		unregisterAll();
	}

	private void loadAliases() {
		aliases = new NameableList<Alias>();
		Logger l = SimpleAlias.logger();
		if (DIRECTORY.exists() && DIRECTORY.isDirectory())
			for (File f : DIRECTORY.listFiles()) {
				String name = f.getName();
				int index = name.indexOf(".yml");
				if (index != -1)
					try {
						aliases.add(new Alias(name.substring(0, index)));
					} catch (Exception e) {
						l.info("Failed to load alias '" + name + "'. Cause: " + e.getMessage());
						if (Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
					}
			}
		sort();
		int amount = aliases.size();
		l.info(amount + " alias" + (amount == 1 ? "" : "es") + " loaded.");
	}

	private void sort() {
		Collections.sort(aliases, COMPARATOR);
	}

	public Alias createAlias(String name) throws Exception {
		Alias alias = new Alias(name);
		register(alias);
		return alias;
	}

	public void register(Alias a) {
		aliases.add(a);
		sort();
	}

	public void unregister(Alias a) {
		String name = a.getName();
		aliases.remove(name);
		sort();
		a.deleteConfiguration();
		if (!a.getCommand().unregister())
			SimpleAlias.logger().warning("Failed to unregister alias '" + name + "' from the commands!");
	}

	public List<Alias> getAliases() {
		return Collections.unmodifiableList(aliases);
	}

	public int getAliasAmount() {
		return aliases.size();
	}

	public Alias getAlias(String name) {
		return aliases.get(name);
	}

	public boolean hasAlias(String name) {
		return aliases.contains(name);
	}

	public Alias getMatchingAlias(String message) {
		String name = message.split(" ")[0];
		for (int i = 0; i < aliases.size(); i++) {
			Alias a = aliases.get(i);
			if (a.getName().equalsIgnoreCase(name))
				return a;
		}
		return null;
	}

	public Set<String> getUncheckedPlayers() {
		return uncheckedPlayers;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		String[] commandArray = StringUtils.removeStart(event.getMessage(), "/").split(" ");
		String command = commandArray[0].toLowerCase();
		Player p = event.getPlayer();
		String name = p.getName();
		if (uncheckedPlayers.contains(name)) {
			uncheckedPlayers.remove(name);
		} else if (!p.isOp() && Settings.isCommandDisabled(command)) {
			String message = Settings.getDisabledMessage(command);
			if (message.length() > 0)
				p.sendMessage(message);
			event.setCancelled(true);
		}
	}
}