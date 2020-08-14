package com.darkblade12.simplealias.cooldown;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import com.darkblade12.simplealias.Settings;
import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.manager.Manager;
import com.darkblade12.simplealias.nameable.NameableList;

public final class CooldownManager extends Manager {
	private static final File DIRECTORY = new File("plugins/SimpleAlias/cooldown lists/");
	private NameableList<CooldownList> lists;

	@Override
	public boolean onEnable() {
		loadLists();
		return true;
	}

	@Override
	public void onDisable() {}

	private void loadLists() {
		lists = new NameableList<CooldownList>();
		Logger l = SimpleAlias.logger();
		if (DIRECTORY.exists() && DIRECTORY.isDirectory())
			for (File f : DIRECTORY.listFiles()) {
				String name = f.getName();
				int index = name.indexOf(".cooldownlist");
				if (index != -1)
					try {
						lists.add(CooldownList.fromFile(name.substring(0, index)));
					} catch (Exception e) {
						l.info("Failed to load the cooldown list of player '" + name + "'. Cause: " + e.getMessage());
						if(Settings.isDebugEnabled()) {
							e.printStackTrace();
						}
					}
			}
		int amount = lists.size();
		l.info(amount + " cooldown list" + (amount == 1 ? "" : "s") + " loaded.");
	}

	public void register(Player p, Cooldown c) {
		CooldownList list = getList(p);
		list.add(c);
		list.saveToFile();
	}

	public void unregister(Player p, Cooldown c) {
		CooldownList list = getList(p);
		if (!list.isEmpty()) {
			list.remove(c.getName());
			if (list.isEmpty())
				list.deleteFile();
			else
				list.saveToFile();
		}
	}

	public List<CooldownList> getLists() {
		return Collections.unmodifiableList(lists);
	}

	public int getListAmount() {
		return lists.size();
	}

	public CooldownList getList(Player p) {
		String name = p.getName();
		CooldownList list = lists.get(name);
		if (list == null) {
			list = new CooldownList(name);
			lists.add(list);
		}
		return list;
	}

	public Cooldown getCooldown(Player p, String name) {
		return getList(p).get(name);
	}
}