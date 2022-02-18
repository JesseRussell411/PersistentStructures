package _trash;

import _utils.ObjectUtils;

import java.util.*;

public class PersistentMap<K, V> implements Iterable<PersistentMap<K, V>.Entry> {
    private static final int INITIAL_CAPACITY = 64;
    private static final PersistentList<_Entry> INITIAL_TABLE;
    private final PersistentList<_Entry> table;
    private final int size;
    private final Map<Object, Boolean> equalityCache = Collections.synchronizedMap(new WeakHashMap<>());
    private volatile Integer hashCache = null;
    private final Object hashCacheLock = new Object();

    Object identifier() {
        return table.identifier();
    }

    private PersistentMap(PersistentList<_Entry> table, int size) {
        this.table = table;
        this.size = size;
    }

    public PersistentMap() {
        this(INITIAL_TABLE, 0);
    }

    public Iterator<Entry> iterator() {
        return new OwnIterator();
    }

    static {
        INITIAL_TABLE = makeInitialTable(INITIAL_CAPACITY);
    }

    public int size() {
        return size;
    }

    public V get(K key) {
        final var entry = getEntry(key);
        return entry == null ? null : (V) entry.value;
    }

    public PersistentMap<K, V> put(K key, V value) {
        return new PersistentMap<>(
                insertEntry(
                        size >= table.size() ?
                                realloc() :
                                table,
                        new _Entry(key, value)),
                size + 1);
    }

    public boolean containsKey(K key) {
        return getEntry(key) != null;
    }

    private _Entry getEntry(Object key) {
        final var startIndex = getKeyIndex(table.size(), key);
        int index = startIndex;
        _Entry current = table.get(index);

        while (!Utils_object.equals(key, current.key)) {
            if (++index >= table.size()) index = 0;
            if (index == startIndex) return null;

            current = table.get(index);
        }

        return current;
    }

    private static PersistentList<_Entry> insertEntry(PersistentList<_Entry> table, _Entry entry) {
        final var startIndex = getKeyIndex(table.size(), entry.key);
        int index = startIndex;
        _Entry current;

        // find a spot
        while ((current = table.get(index)) != null && !Utils_object.equals(entry.key, current.key)) {
            if (++index >= table.size()) index = 0;
            if (index == startIndex) return null;
        }

        // insert entry into that spot
        return table.set(index, entry);
    }

    private PersistentList<_Entry> realloc() {
        var newTable = makeInitialTable(table.size() * 2);

        for (final var entry : table) {
            newTable = insertEntry(newTable, entry);
        }

        return newTable;
    }

    private static int getKeyIndex(int capacity, Object key) {
        final var h = Math.abs(hashCodeOf(key));
        return (h ^ (h >> 16)) % capacity;
    }

    private static int hashCodeOf(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }

    private static PersistentList<_Entry> makeInitialTable(int capacity) {
        final var initialEntries = new _Entry[capacity];
        return new PersistentList<>(initialEntries);
    }

    private static class _Entry {
        final Object key;
        final Object value;

        _Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }
    }

    public class Entry {
        public final K key;
        public final V value;

        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(key) + ": " + String.valueOf(value);
        }
    }

    public class OwnIterator implements Iterator<Entry> {
        private Integer nextIndex;
        private _Entry nextEntry;

        private OwnIterator() {
            nextIndex = 0;
            while ((nextEntry = table.get(nextIndex)) == null) {
                if (++nextIndex >= table.size()) {
                    nextEntry = null;
                }
            }
        }

        public boolean hasNext() {
            return nextEntry != null;
        }

        public Entry next() {
            if (nextEntry == null) return null;
            final var result = new Entry((K) nextEntry.key, (V) nextEntry.value);
            advance();
            return result;
        }

        private void advance() {
            do {
                if (++nextIndex >= table.size()) {
                    nextEntry = null;
                    return;
                }
            } while ((nextEntry = table.get(nextIndex)) == null);
        }

    }
}
