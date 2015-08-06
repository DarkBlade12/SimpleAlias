package com.darkblade12.simplealias.alias;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.manager.Manager;
import com.darkblade12.simplealias.nameable.NameableComparator;
import com.darkblade12.simplealias.nameable.NameableList;

public final class AliasManager extends Manager {
	private static final File DIRECTORY = new File("plugins/SimpleAlias/aliases/");
	private static final NameableComparator<Alias> COMPARATOR = new NameableComparator<Alias>();
	private NameableList<Alias> aliases;

	@Override
	public boolean onEnable() {
		loadAliases();
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
}