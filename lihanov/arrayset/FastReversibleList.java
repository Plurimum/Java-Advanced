package info.kgeorgiy.ja.lihanov.arrayset;

import java.util.*;

public class FastReversibleList<T> extends AbstractList<T> {
    private final List<T> elements;

    public FastReversibleList(List<T> elements) {
        this.elements = elements;
    }

    @Override
    public T get(int index) {
        return elements.get(size() - 1 - index);
    }

    @Override
    public int size() {
        return elements.size();
    }
}
