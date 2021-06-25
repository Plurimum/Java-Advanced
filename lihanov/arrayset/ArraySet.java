package info.kgeorgiy.ja.lihanov.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {

    private final List<T> elements;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        this(Collections.emptyList(), null);
    }

    public ArraySet(Collection<? extends T> elements) {
        this(elements, null);
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        Set<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(collection);
        this.elements = new ArrayList<>(treeSet);
        this.comparator = comparator;
    }

    public ArraySet(Comparator<? super T> comparator) {
        this(Collections.emptyList(), comparator);
    }

    private ArraySet(List<T> elements, Comparator<? super T> comparator) {
        this.elements = elements;
        this.comparator = comparator;
    }

    @Override
    public T lower(T t) {
        return getValue(t, true, true);
    }

    @Override
    public T floor(T t) {
        return getValue(t, true, false);
    }

    @Override
    public T ceiling(T t) {
        return getValue(t, false, false);
    }

    @Override
    public T higher(T t) {
        return getValue(t, false, true);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableCollection(elements).iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new FastReversibleList<>(elements), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        int fromIndex = indexOf(fromElement, false, !fromInclusive);
        int toIndex = indexOf(toElement, true, !toInclusive);
        if (compare(fromElement, toElement) == 0 && !(fromInclusive && toInclusive)) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(elements.subList(fromIndex, toIndex + 1), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        }
        return subSet(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        }
        return subSet(fromElement, inclusive, last(), true);
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("Left border cannot be greater than right border");
        }
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        assertIsNotEmpty();
        return elements.get(0);
    }

    @Override
    public T last() {
        assertIsNotEmpty();
        return elements.get(size() - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object object) {
        return Collections.binarySearch(elements, (T) object, comparator) >= 0;
    }

    private void assertIsNotEmpty() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    @SuppressWarnings("unchecked")
    private int compare(T o1, T o2) {
        if (comparator != null) {
            return comparator.compare(o1, o2);
        } else {
            return ((Comparable<T>) o1).compareTo(o2);
        }
    }

    private int indexOf(T element, boolean greatest, boolean strict) {
        int index = Collections.binarySearch(elements, element, comparator);
        if (index < 0) {
            final int foundIndex = -index - 1;
            return greatest ? (foundIndex - 1) : foundIndex;
        } else {
            if (strict) {
                return greatest ? (index - 1) : (index + 1);
            } else {
                return index;
            }
        }
    }

    private T getValue(T t, boolean greatest, boolean strict) {
        int index = indexOf(t, greatest, strict);
        if (index < 0 || index >= size()) {
            return null;
        } else {
            return elements.get(index);
        }
    }
}
