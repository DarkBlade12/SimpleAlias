package com.darkblade12.simplealias.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class CommandList extends ArrayList<ICommand> {
	private static final long serialVersionUID = -3228460490008738846L;
	private Map<String, ICommand> map;

	public CommandList() {
		super();
		map = new HashMap<String, ICommand>();
	}

	public CommandList(Collection<ICommand> c) {
		this();
		for (ICommand i : c)
			add(i);
	}

	public boolean add(ICommand e) {
		CommandDetails c = CommandHandler.getDetails(e);
		if (c != null) {
			map.put(c.name(), e);
			return super.add(e);
		}
		return false;
	}

	public ICommand get(String name) {
		return map.get(name.toLowerCase());
	}
}