import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PersistentArray<T> implements Iterable<T> {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private final Object[] items;
    private final boolean reversed;

    Object[] getItems() {
        return items;
    }

    boolean isReversed() {
        return reversed;
    }

    // =============
    // constructors
    // =============
    PersistentArray(Object[] items, boolean reversed) {
        Objects.requireNonNull(items);
        this.items = items;
        this.reversed = reversed;
    }

    public PersistentArray(Iterable<T> items) {
        this(
                StreamSupport.stream(
                        Objects.requireNonNull(Objects.requireNonNull(items)).spliterator(),
                        false).toArray(),
                false);

    }

    public PersistentArray(Stream<T> items) {
        this(Objects.requireNonNull(items).toArray(), false);
    }

    public PersistentArray(T[] items) {
        this(
                Arrays.copyOf(
                        Objects.requireNonNull(items),
                        items.length),
                false);
    }

    public PersistentArray() {
        this(EMPTY_ARRAY, false);
    }

    // ===========
    // properties
    // ===========
    public int size() {
        return items.length;
    }

    // ========
    // methods
    // ========
    @Override
    public Iterator<T> iterator() {
        return new SelfIterator();
    }

    // ===================================
    // single item manipulation
    // ===================================
    // remove
    public PersistentArray<T> remove(int index) {
        final var result = ArrayUtils.remove(items, index, reversed);
        return new PersistentArray<>(result, false);
    }

    // get
    public T get(int index) {
        ArrayUtils.requireIndexInBounds(index, size());
        return (T) ArrayUtils.get(items, index, reversed);
    }

    // insert
    public PersistentArray<T> add(int index, T item) {
        final var result = ArrayUtils.add(items, index, item, reversed);
        return new PersistentArray<>(result, false);
    }

    // swap
    public PersistentArray<T> set(int index, T with) {
        final var result = ArrayUtils.set(items, index, with, reversed);
        return new PersistentArray<>(result, false);
    }

    // put

    /**
     * adds item to end
     * "puts on the top"
     */
    public PersistentArray<T> put(T item) {
        return add(size(), item);
    }

    // pop

    /**
     * removes item from end
     * "pops off the top"
     */
    public PersistentArray<T> pop() {
        if (size() == 0) return this;
        return remove(size() - 1);
    }

    /**
     * adds item to start (index 0)
     * "pushes item into bottom"
     */
    // push
    public PersistentArray<T> push(T item) {
        return add(0, item);
    }

    // pull

    /**
     * removed item from start (index 0)
     * "pulls items out of bottom"
     */
    public PersistentArray<T> pull() {
        if (size() == 0) return this;
        return remove(0);
    }

    // get pop
    public T tail() {
        return get(size() - 1);
    }

    // get pull
    public T head() {
        return get(0);
    }


    // ==========================================
    // multi item manipulation -- PersistantArray
    // ==========================================
    //remove
    public PersistentArray<T> remove(int start, int length) {
        final var result = ArrayUtils.remove(items, start, length, reversed);
        return new PersistentArray<>(result, false);
    }

    //get
    public PersistentArray<T> get(int start, int length) {
        final var result = ArrayUtils.get(items, start, length, reversed);
        return new PersistentArray<>(items, false);
    }

    //insert
    public PersistentArray<T> insert(int index, PersistentArray<T> items) {
        Objects.requireNonNull(items);
        return insert(index, items, 0, items.size());
    }

    public PersistentArray<T> insert(int index, PersistentArray<T> items, int start) {
        Objects.requireNonNull(items);
        return insert(index, items, start, items.size());
    }

    public PersistentArray<T> insert(int index, PersistentArray<T> other, int start, int length) {
        Objects.requireNonNull(other);
        final var result = ArrayUtils.insert(items, index, other.items, start, length, reversed, other.reversed);
        return new PersistentArray<>(result, false);
    }

    //replace
    public PersistentArray<T> replace(int index, PersistentArray<T> items) {
        Objects.requireNonNull(items);
        return replace(index, items, 0, items.size());
    }

    public PersistentArray<T> replace(int index, PersistentArray<T> items, int start) {
        Objects.requireNonNull(items);
        return replace(index, items, start, items.size());
    }

    public PersistentArray<T> replace(int index, PersistentArray<T> other, int start, int length) {
        Objects.requireNonNull(other);
        final var result = ArrayUtils.replace(items, index, other.items, start, length, reversed, other.reversed);
        return new PersistentArray<>(result, false);
    }

    //misc
    // append
    public PersistentArray<T> append(PersistentArray<T> items) {
        Objects.requireNonNull(items);
        return append(items, 0, items.size());
    }

    public PersistentArray<T> append(PersistentArray<T> items, int start) {
        Objects.requireNonNull(items);
        return append(items, start, items.size());
    }

    public PersistentArray<T> append(PersistentArray<T> items, int start, int length) {
        return insert(size(), items, start, length);
    }


    // prepend
    public PersistentArray<T> prepend(PersistentArray<T> items) {
        Objects.requireNonNull(items);
        return prepend(items, 0, items.size());
    }

    public PersistentArray<T> prepend(PersistentArray<T> items, int start) {
        Objects.requireNonNull(items);
        return prepend(items, start, items.size());
    }

    public PersistentArray<T> prepend(PersistentArray<T> items, int start, int length) {
        return insert(0, items, start, length);
    }

    // pop
    public PersistentArray<T> pop(int length) {
        if (size() == 0) return this;
        return remove(size() - length, length);
    }

    // pull
    public PersistentArray<T> pull(int length) {
        if (size() == 0) return this;
        return remove(0, length);
    }

    // get pop
    public PersistentArray<T> tail(int length) {
        final var trimmedLength = Math.min(length, size());
        return get(size() - trimmedLength, trimmedLength);
    }

    // get pull
    public PersistentArray<T> head(int length) {
        final var trimmedLength = Math.min(length, size());
        return get(0, trimmedLength);
    }

    // reverse
    public PersistentArray<T> reverse() {
        if (size() <= 1) return this;
        return new PersistentArray<>(items, !reversed);
    }

    // ==================================
    // multi item manipulation -- Array
    // ==================================
    // insert
    public PersistentArray<T> insert(int at, T[] items) {
        Objects.requireNonNull(items);
        return insert(at, items, 0, items.length, false);
    }

    public PersistentArray<T> insert(int at, T[] items, int start) {
        Objects.requireNonNull(items);
        return insert(at, items, start, items.length, false);
    }

    public PersistentArray<T> insert(int at, T[] items, int start, int length) {
        return insert(at, items, start, length, false);
    }

    public PersistentArray<T> insert(int at, T[] other, int start, int length, boolean reverseItems) {
        final var result = ArrayUtils.insert(items, at, other, start, length, reversed, reverseItems);
        return new PersistentArray<>(items, false);
    }

    // replace
    public PersistentArray<T> replace(int at, T[] with) {
        Objects.requireNonNull(with);
        return replace(at, with, 0, with.length, false);
    }

    public PersistentArray<T> replace(int at, T[] with, int start) {
        Objects.requireNonNull(with);
        return replace(at, with, start, with.length);
    }

    public PersistentArray<T> replace(int at, T[] with, int start, int length) {
        return replace(at, with, start, length, false);
    }

    public PersistentArray<T> replace(int at, T[] with, int start, int length, boolean reverseWith) {
        final var result = ArrayUtils.replace(items, at, with, start, length, reversed, reverseWith);
        return new PersistentArray<>(result, false);
    }

    //misc
    // put
    public PersistentArray<T> append(T[] items) {
        Objects.requireNonNull(items);
        return append(items, 0, items.length, false);
    }

    public PersistentArray<T> append(T[] items, int start) {
        Objects.requireNonNull(items);
        return append(items, start, items.length, false);
    }

    public PersistentArray<T> append(T[] items, int start, int length) {
        return append(items, start, length, false);
    }

    public PersistentArray<T> append(T[] items, int start, int length, boolean reverseItems) {
        return insert(size(), items, start, length, reverseItems);
    }

    // push
    public PersistentArray<T> prepend(T[] items) {
        Objects.requireNonNull(items);
        return prepend(items, 0, items.length, false);
    }

    public PersistentArray<T> prepend(T[] items, int start) {
        Objects.requireNonNull(items);
        return prepend(items, start, items.length, false);
    }

    public PersistentArray<T> prepend(T[] items, int start, int length) {
        Objects.requireNonNull(items);
        return prepend(items, start, length, false);
    }

    public PersistentArray<T> prepend(T[] items, int start, int length, boolean reverseItems) {
        return insert(0, items, start, length, reverseItems);
    }

    // misc
    public PersistentArray<T> sort(Comparator<T> comparator) {
        Objects.requireNonNull(comparator);
        Comparator<Object> unTypeComparator = (a, b) -> comparator.compare((T) a, (T) b);

        // shortcut //
        if (ArrayUtils.isSorted(items, unTypeComparator, reversed)) return this;

        final var result = ArrayUtils.copyOf(items);

        Arrays.sort(result, unTypeComparator);

        return new PersistentArray<>(result, false);
    }

    public class SelfIterator implements Iterator<T> {
        private int i = 0;

        public boolean hasNext() {
            return i < size();
        }

        public T next() {
            return (T) ArrayUtils.get(items, i, reversed);
        }

        public void reset() {
            i = 0;
        }
    }
}
