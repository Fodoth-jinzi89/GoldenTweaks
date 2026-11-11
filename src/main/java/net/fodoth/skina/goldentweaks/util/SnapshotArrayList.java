package net.fodoth.skina.goldentweaks.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class SnapshotArrayList<T> extends ArrayList<T> {

    @Override
    public Iterator<T> iterator() {
        return new ArrayList<>(this).iterator();
    }

    @Override
    public ListIterator<T> listIterator() {
        return new ArrayList<>(this).listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new ArrayList<>(this).listIterator(index);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        new ArrayList<>(this).forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return new ArrayList<>(this).spliterator();
    }
}
