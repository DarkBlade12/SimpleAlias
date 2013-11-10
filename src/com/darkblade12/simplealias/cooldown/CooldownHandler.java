package com.darkblade12.simplealias.cooldown;

import java.io.File;
import java.util.Iterator;

import org.bukkit.entity.Player;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.nameable.NameableList;

public class CooldownHandler implements Iterable<CooldownList> {
	private static File COOLDOWN_DIRECTORY = new File("plugins/SimpleAlias/cooldowns/");
	private static String COOLDOWN_DIRECTORY_FILE_NAME_PATTERN = ".*\\.cdwn";
	private SimpleAlias plugin;
	private NameableList<CooldownList> lists;

	public CooldownHandler(SimpleAlias plugin) {
		this.plugin = plugin;
		loadLists();
	}

	public void loadLists() {
		lists = new NameableList<CooldownList>();
		if (COOLDOWN_DIRECTORY.exists())
			for (File f : COOLDOWN_DIRECTORY.listFiles()) {
				String n = f.getName();
				if (n.matches(COOLDOWN_DIRECTORY_FILE_NAME_PATTERN)) {
					String name = n.replace(".cdwn", "");
					try {
						lists.add(CooldownList.fromFile(plugin, name));
					} catch (Exception e) {
						plugin.l.info("Failed to load cooldown list of player '" + name + "'. Reason: " + e.getMessage());
					}
				}
			}
		int amount = lists.size();
		plugin.l.info(amount + " cooldown list" + (amount == 1 ? "" : "s") + " loaded.");
	}

	public void saveLists() {
		for (CooldownList cl : this)
			cl.saveToFile(plugin);
		plugin.l.info("Cooldown lists saved.");
	}

	public void add(Player p, Cooldown c) {
		CooldownList list = getList(p);
		if (list == null)
			list = new CooldownList(p.getName());
		list.add(c);
		if (lists.contains(list))
			lists.update(list);
		else
			lists.add(list);
	}

	public CooldownList getList(Player p) {
		return lists.get(p.getName());
	}

	public Cooldown get(Player p, String name) {
		CooldownList list = getList(p);
		return list == null ? null : list.get(name);
	}

	public void remove(Player p, String name) {
		CooldownList list = getList(p);
		if (list != null) {
			list.remove(name);
			lists.update(list);
		}
	}

	@Override
	public Iterator<CooldownList> iterator() {
		return lists.iterator();
	}
}