package com.darkblade12.simplealias.nameable;

import java.util.Comparator;

public final class NameableComparator<T extends Nameable> implements Comparator<T> {
    @Override
    public int compare(T element1, T element2) {
        return element1.getName().compareTo(element2.getName());
    }
}
