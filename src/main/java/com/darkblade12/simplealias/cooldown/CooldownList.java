package com.darkblade12.simplealias.cooldown;

import com.darkblade12.simplealias.SimpleAlias;
import com.darkblade12.simplealias.alias.AliasManager;
import com.darkblade12.simplealias.nameable.Nameable;
import com.darkblade12.simplealias.nameable.NameableList;
import com.darkblade12.simplealias.reader.types.SerializableReader;

public final class CooldownList extends NameableList<Cooldown> implements Nameable {
	private static final long serialVersionUID = -4205499130090416937L;
	private String name;
	private transient SerializableReader<CooldownList> serializableReader;

	public CooldownList(String name) {
		super();
		this.name = name;
		serializableReader = new SerializableReader<CooldownList>(name + ".cooldownlist", "plugins/SimpleAlias/cooldown lists/");
	}

	public static CooldownList fromFile(String name) throws Exception {
		SerializableReader<CooldownList> s = new SerializableReader<CooldownList>(name + ".cooldownlist", "plugins/SimpleAlias/cooldown lists/");
		CooldownList list = s.readFromFile();
		list.serializableReader = s;
		return list;
	}

	public void cleanUp() {
		AliasManager a = SimpleAlias.getAliasManager();
		for (int i = 0; i < size(); i++) {
			Cooldown c = get(i);
			if (!a.hasAlias(c.getName()) || c.isExpired()) {
				remove(i);
				i--;
			}
		}
	}

	public void saveToFile() {
		cleanUp();
		serializableReader.saveToFile(this);
	}

	public void deleteFile() {
		serializableReader.deleteFile();
	}

	@Override
	public String getName() {
		return name;
	}
}