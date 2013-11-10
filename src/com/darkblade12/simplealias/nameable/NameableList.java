package com.darkblade12.simplealias.nameable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NameableList<T extends Nameable> implements Iterable<T> {
	private List<T> list;

	public NameableList() {
		list = new ArrayList<T>();
	}

	public NameableList(List<T> list) {
		super();
		for (T element : list)
			this.list.add(element);
	}

	public void add(T element) {
		list.add(element);
	}

	public void remove(T element) {
		list.remove(element);
	}

	public void remove(String name) {
		for (int i = 0; i < list.size(); i++) {
			T element = list.get(i);
			if (element.getName().equals(name))
				list.remove(i);
		}
	}

	public T get(String name) {
		for (int i = 0; i < list.size(); i++) {
			T element = list.get(i);
			if (element.getName().equals(name))
				return element;
		}
		return null;
	}

	public boolean has(String name) {
		return get(name) != null;
	}

	public boolean contains(Object o) {
		return list.contains(o);
	}

	public void update(T element) {
		String name = element.getName();
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getName().equals(name)) {
				list.set(i, element);
				return;
			}
	}

	public int size() {
		return list.size();
	}

	public void clear() {
		list.clear();
	}

	public List<String> getNameList() {
		List<String> names = new ArrayList<String>();
		for (T element : this)
			names.add(element.getName());
		return names;
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}
}