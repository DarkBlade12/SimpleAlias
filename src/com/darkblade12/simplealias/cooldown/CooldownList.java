package com.darkblade12.simplealias.cooldown;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.loader.TextFileLoader;
import com.darkblade12.simplealias.nameable.Nameable;
import com.darkblade12.simplealias.nameable.NameableList;

public class CooldownList implements Nameable, Iterable<Cooldown> {
	private String name;
	private NameableList<Cooldown> list;

	public CooldownList(String name) {
		this.name = name;
		list = new NameableList<Cooldown>();
	}

	public CooldownList(String name, NameableList<Cooldown> list) {
		this.name = name;
		this.list = list;
	}

	public static CooldownList fromFile(SimpleAlias plugin, String name) throws Exception {
		NameableList<Cooldown> list = new NameableList<Cooldown>();
		List<String> lines;
		try {
			lines = new TextFileLoader(plugin, "cooldowns", name + ".cdwn").readLines();
		} catch (Exception e) {
			throw new Exception("Failed to load the cooldown file");
		}
		for (String line : lines) {
			String[] s = line.split("#");
			String aliasName = s[0];
			if (plugin.aliasHandler.exists(aliasName))
				try {
					Cooldown c = new Cooldown(aliasName, Long.parseLong(s[1]));
					if (!c.isExpired())
						list.add(c);
				} catch (Exception e) {
					continue;
				}
		}
		return new CooldownList(name, list);
	}

	public void saveToFile(SimpleAlias plugin) {
		List<String> lines = new ArrayList<String>();
		for (Cooldown c : this)
			if (plugin.aliasHandler.exists(c.getName()) && !c.isExpired())
				lines.add(c.toString());
		TextFileLoader loader = new TextFileLoader(plugin, "cooldowns", name + ".cdwn");
		if (lines.isEmpty())
			loader.deleteFile();
		else
			loader.saveFile(lines);
	}

	public void add(Cooldown c) {
		list.add(c);
	}

	public void remove(String name) {
		list.remove(name);
	}

	public Cooldown get(String name) {
		return list.get(name);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Iterator<Cooldown> iterator() {
		return list.iterator();
	}
}