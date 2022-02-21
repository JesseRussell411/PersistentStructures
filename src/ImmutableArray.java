import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ImmutableArray<T> implements Iterable<T> {
    // this may seem stupid, but there's a very good reason I'm doing it.
    static class Data {
        final Object[] items;
        final boolean reversed;

        public Data(Object[] items) {
            this(items, false);
        }

        public Data(Object[] items, boolean reversed) {
            this.items = items;
            this.reversed = reversed;
        }
    }

    private static final Data EMPTY_DATA = new Data(new Object[0], false);

    // the fields items and reversed may be over-written in the event that an equal ImmutableArray is found.
    // This is to save memory since there's no point in keeping two separate but equal array instances in memory.
    // However, in order to make ImmutableArray thread safe, I would need to make those fields volatile which would slow
    // everything down. Well the whole point of volatile is to keep the values from being cached by the cpu in case they change.
    // But items and reversed will never be replaced with something that's considered un-equal. If they're replaced, it'll be because
    // whatever they're being replaced with IS equal. So caching them is completely fine; however, they do need to be cached together
    // so that's why they're being put in one class. Not a record though, I want the default equality behavior.

    private Data data;
    private volatile MemoizedSupplier<Integer> hashSupplier = new MemoizedSupplier<>(() -> {
        // get hash code
        return Arrays.hashCode(data.items);
    });

    private volatile MemoizedSupplier<String> stringSupplier = new MemoizedSupplier<>(() -> {
        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < size(); i++) {
            if (i > 0) result.append(", ");
            result.append(get(i));
        }

        return result.toString();
    });


    private void consolidateEqual(ImmutableArray<?> other, boolean equalTypes) {
        if (equalTypes) {
            data = other.data;
        }

        if (hashSupplier.cached()) {
            other.hashSupplier = hashSupplier;
        } else {
            hashSupplier = other.hashSupplier;
        }

        if (stringSupplier.cached()) {
            other.stringSupplier = stringSupplier;
        } else {
            stringSupplier = other.stringSupplier;
        }
    }

    // =============
    // constructors
    // =============
    ImmutableArray(Data fields) {
        this.data = fields;
    }

    public ImmutableArray(Iterable<T> items) {
        this(new Data(
                StreamSupport.stream(
                        Objects.requireNonNull(items).spliterator(),
                        false).toArray(),
                false));
    }

    public ImmutableArray(Stream<T> items) {
        this(new Data(items.toArray(), false));
    }

    public ImmutableArray(T[] items) {
        this(Arrays.stream(items));
    }

    public ImmutableArray() {
        this(EMPTY_DATA);
    }

    // ===========
    // properties
    // ===========
    public int size() {
        return data.items.length;
    }

    // ========
    // methods
    // ========
    @Override
    public String toString() {
        return stringSupplier.get();
    }

    @Override
    public Iterator<T> iterator() {
        return new SelfIterator();
    }

    @Override
    public int hashCode() {
        return hashSupplier.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ImmutableArray<?> other) {
            final var quickEqual = quickEquals(other);
            if (quickEqual != null) return quickEqual;

            final var iterA = iterator();
            final var iterB = other.iterator();
            boolean equalTypes = false;

            while (iterA.hasNext() && iterB.hasNext()) {
                final var nextA = iterA.next();
                final var nextB = iterB.next();

                // The things we do when java has type erasure.  ):
                if (!equalTypes && nextA != null && nextB != null) {
                    if (nextA.getClass() == nextB.getClass()) {
                        equalTypes = true;
                    }
                }

                if (!Objects.equals(nextA, nextB)) return false;
            }

            if (iterA.hasNext() == iterB.hasNext()) {
                consolidateEqual(other, equalTypes);
                return true;
            } else return false;
        } else return false;
    }

    /**
     * @return Whether definitely equal. If null: inconclusive.
     */
    public Boolean quickEquals(ImmutableArray<?> other) {
        if (other == null) return false;
        if (other.size() != size()) return false;

        if (other == this) return true;
        if (other.data == data) return true;
        if (other.size() == 0 && size() == 0) return true;

        if (other.hashCode() != hashCode()) return false;

        return null;
    }

    // ===================================
    // single item manipulation
    // ===================================
    // remove
    public ImmutableArray<T> remove(int index) {
        final var result = Utils_lists.without(data.items, index, data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    // get
    public T get(int index) {
        Utils_lists.requireIndexInBounds(index, size());

        return (T) Utils_lists.get(data.items, index, data.reversed);
    }

    // insert
    public ImmutableArray<T> add(int index, T item) {
        Utils_lists.requireIndexInBounds(index, size() + 1);
        final var result = Utils_lists.withAddition(data.items, index, item, data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    // swap
    public ImmutableArray<T> swap(int index, T with) {
        Utils_lists.requireIndexInBounds(index, size());
        final var result = Utils_lists.withSwap(data.items, index, with, data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    // put
    public ImmutableArray<T> put(T item) {
        return add(size(), item);
    }

    // pop
    public ImmutableArray<T> pop() {
        if (size() == 0) return this;
        return remove(size() - 1);
    }

    // push
    public ImmutableArray<T> push(T item) {
        return add(0, item);
    }

    // pull
    public ImmutableArray<T> pull() {
        if (size() == 0) return this;
        return remove(0);
    }

    // get pop
    public T getFinal() {
        return get(size() - 1);
    }

    // get pull
    public T getFirst() {
        return get(0);
    }


    // ==========================================
    // multi item manipulation -- ImmutableArray
    // ==========================================
    //remove
    public ImmutableArray<T> remove(int start, int length) {
        final var result = Utils_lists.without(data.items, start, length, data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    //get
    public ImmutableArray<T> get(int start, int length) {
        final var result = Utils_lists.get(data.items, start, length, data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    //insert
    public ImmutableArray<T> insert(int index, ImmutableArray<T> items) {
        Objects.requireNonNull(items);
        return insert(index, items, 0, items.size());
    }

    public ImmutableArray<T> insert(int index, ImmutableArray<T> items, int start) {
        Objects.requireNonNull(items);
        return insert(index, items, start, items.size());
    }

    public ImmutableArray<T> insert(int index, ImmutableArray<T> items, int start, int length) {
        Objects.requireNonNull(items);
        final var result = Utils_lists.withInsertion(
                data.items,
                index,
                items.data.items,
                start,
                length,
                items.data.reversed,
                data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    //replace
    public ImmutableArray<T> replace(int index, ImmutableArray<T> items) {
        Objects.requireNonNull(items);
        return replace(index, items, 0, items.size());
    }

    public ImmutableArray<T> replace(int index, ImmutableArray<T> items, int start) {
        Objects.requireNonNull(items);
        return replace(index, items, start, items.size());
    }

    public ImmutableArray<T> replace(int index, ImmutableArray<T> items, int start, int length) {
        Objects.requireNonNull(items);
        final var result = Utils_lists.withReplacement(data.items, index, items.data.items, start, length, items.data.reversed, data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    //misc
    // append
    public ImmutableArray<T> append(ImmutableArray<T> items) {
        Objects.requireNonNull(items);
        return append(items, 0, items.size());

    }

    public ImmutableArray<T> append(ImmutableArray<T> items, int start) {
        Objects.requireNonNull(items);
        return append(items, start, items.size());
    }

    public ImmutableArray<T> append(ImmutableArray<T> items, int start, int length) {
        return insert(size(), items, start, length);
    }


    // prepend
    public ImmutableArray<T> prepend(ImmutableArray<T> items) {
        Objects.requireNonNull(items);
        return prepend(items, 0, items.size());
    }

    public ImmutableArray<T> prepend(ImmutableArray<T> items, int start) {
        Objects.requireNonNull(items);
        return prepend(items, start, items.size());
    }

    public ImmutableArray<T> prepend(ImmutableArray<T> items, int start, int length) {
        return insert(0, items, start, length);
    }

    // pop
    public ImmutableArray<T> pop(int length) {
        if (size() == 0) return this;
        return remove(size() - length, length);
    }

    // pull
    public ImmutableArray<T> pull(int length) {
        if (size() == 0) return this;
        return remove(0, length);
    }

    // get pop
    public ImmutableArray<T> getFinal(int length) {
        return get(size() - length, length);
    }

    // get pull
    public ImmutableArray<T> getFirst(int length) {
        return get(0, length);
    }

    // reverse
    public ImmutableArray<T> reverse() {
        if (size() <= 1) return this;
        return new ImmutableArray<>(new Data(this.data.items, !this.data.reversed));
    }

    // ==================================
    // multi item manipulation -- Array
    // ==================================
    // insert
    public ImmutableArray<T> insert(int at, T[] items) {
        Objects.requireNonNull(items);
        return insert(at, items, 0, items.length, false);
    }

    public ImmutableArray<T> insert(int at, T[] items, int start) {
        Objects.requireNonNull(items);
        return insert(at, items, start, items.length, false);
    }

    public ImmutableArray<T> insert(int at, T[] items, int start, int length) {
        return insert(at, items, start, length, false);
    }

    public ImmutableArray<T> insert(int at, T[] items, int start, int length, boolean reverseItems) {
        final var result = Utils_lists.withInsertion(data.items, at, items, start, length, reverseItems, data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    // replace
    public ImmutableArray<T> replace(int at, T[] with) {
        Objects.requireNonNull(with);
        return replace(at, with, 0, with.length, false);
    }

    public ImmutableArray<T> replace(int at, T[] with, int start) {
        Objects.requireNonNull(with);
        return replace(at, with, start, with.length);
    }

    public ImmutableArray<T> replace(int at, T[] with, int start, int length) {
        return replace(at, with, start, length, false);
    }

    public ImmutableArray<T> replace(int at, T[] with, int start, int length, boolean reverseWith) {
        final var result = Utils_lists.withReplacement(data.items, at, with, start, length, reverseWith, data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    //misc
    // put
    public ImmutableArray<T> append(T[] items) {
        Objects.requireNonNull(items);
        return append(items, 0, items.length, false);
    }

    public ImmutableArray<T> append(T[] items, int start) {
        Objects.requireNonNull(items);
        return append(items, start, items.length, false);
    }

    public ImmutableArray<T> append(T[] items, int start, int length) {
        return append(items, start, length, false);
    }

    public ImmutableArray<T> append(T[] items, int start, int length, boolean reverseItems) {
        final var result = Utils_lists.withInsertion(data.items, size(), items, start, length, reverseItems, data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    // push
    public ImmutableArray<T> prepend(T[] items) {
        Objects.requireNonNull(items);
        return prepend(items, 0, items.length, false);
    }

    public ImmutableArray<T> prepend(T[] items, int start) {
        Objects.requireNonNull(items);
        return prepend(items, start, items.length, false);
    }

    public ImmutableArray<T> prepend(T[] items, int start, int length) {
        Objects.requireNonNull(items);
        return prepend(items, start, length, false);
    }

    public ImmutableArray<T> prepend(T[] items, int start, int length, boolean reverseItems) {
        final var result = Utils_lists.withInsertion(data.items, 0, items, start, length, reverseItems, data.reversed);

        return new ImmutableArray<>(new Data(result, false));
    }

    public class SelfIterator implements Iterator<T> {
        private int i = 0;

        public boolean hasNext() {
            return i < size();
        }

        public T next() {
            return (T) Utils_lists.get(data.items, i, data.reversed);
        }

        public void reset() {
            i = 0;
        }
    }


}
