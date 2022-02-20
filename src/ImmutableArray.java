import java.util.Objects;
import java.util.stream.StreamSupport;

public class ImmutableArray<T> {
    private Object[] items;

    public ImmutableArray(Iterable<T> items) {
        this(
                StreamSupport.stream(
                        Objects.requireNonNull(items).spliterator(),
                        false).toArray());

    }

    ImmutableArray(Object[] items) {
        this.items = items;
    }

    public static <T> ImmutableArray<T> from(T[] original) {
        return new ImmutableArray<>(Utils_lists.get(original));
    }

    public int size() {
        return items.length;
    }

    // ===================================
    // single item manipulation
    // ===================================
    public ImmutableArray<T> add(int index, T item) {
        Utils_lists.requireIndexInBounds(index, items.length);

        return new ImmutableArray<>(
                Utils_lists.withAddition(
                        items,
                        index,
                        item));
    }

    public T get(int index) {
        Utils_lists.requireIndexInBounds(index, items.length);

        return (T) items[index];
    }

    public ImmutableArray<T> remove(int index) {
        return new ImmutableArray<>(
                Utils_lists.without(items, index));
    }

    public ImmutableArray<T> swap(int index, T item) {
        return new ImmutableArray<>(
                Utils_lists.withSwap(items, index, item));
    }

    // ==================================
    // multi item manipulation -- ImmutableArray
    // ==================================
    public ImmutableArray<T> insert(int index, ImmutableArray<T> items) {
        return insert(index, items, 0, items.size());
    }

    public ImmutableArray<T> insert(int index, ImmutableArray<T> items, int start) {
        return insert(index, items, start, items.size() - start);
    }

    public ImmutableArray<T> insert(int index, ImmutableArray<T> items, int start, int length) {
        return new ImmutableArray<>(
                Utils_lists.withInsertion(
                        this.items,
                        index,
                        items.items,
                        start,
                        length));
    }

    public ImmutableArray<T> get(int start, int length) {
        return new ImmutableArray<>(
                Utils_lists.get(
                        items,
                        start,
                        length));
    }

    public ImmutableArray<T> remove(int index, int length) {
        return new ImmutableArray<>(
                Utils_lists.without(
                        items,
                        index,
                        length));
    }

    public ImmutableArray<T> replace(int index, ImmutableArray<T> replacement) {
        return replace(index, replacement, 0, replacement.size());

    }

    public ImmutableArray<T> replace(int index, ImmutableArray<T> replacement, int start) {
        return replace(index, replacement, start, replacement.size() - start);
    }

    public ImmutableArray<T> replace(int index, ImmutableArray<T> replacement, int start, int length) {
        return new ImmutableArray<>(
                Utils_lists.withReplacement(
                        this.items,
                        index,
                        replacement.items,
                        start,
                        length));
    }


    // ==================================
    // multi item manipulation -- Array
    // ==================================
    public ImmutableArray<T> insert(int index, T[] items) {
        return new ImmutableArray<>(
                Utils_lists.withInsertion(items, index, items));
    }

    public ImmutableArray<T> replace(T[])

//
// -multi-                  -single
//    insert(index, items)     add(index, item)
//    get(index, length)       get(index)
//    remove(index, length)    remove(index)
//    replace(index, items)    swap(index, item)
//
//    append(items)            put(item)
//    pop(length)              pop()
//    prepend(items)           push(item)
//    pull(length)             pull()

}
