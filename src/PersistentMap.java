import java.util.Iterator;

public class PersistentMap<K, V> implements Iterable<PersistentMap<K, V>.Entry> {
    private static final int INITIAL_CAPACITY = 64;
    private static final PersistentList<_Entry> INITIAL_TABLE;
    private final PersistentList<_Entry> table;
    private final int size;

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

        while (!areEqual(key, current.key)) {
            if (++index >= table.size()) index = 0;
            if (index == startIndex) return null;

            current = table.get(index);
        }

        return current;
    }

    private static PersistentList<_Entry> insertEntry(PersistentList<_Entry> table, _Entry entry) {
        final var startIndex = getKeyIndex(table.size(), entry.key);
        int index = startIndex;

        // find a spot
        while (table.get(index) != null) {
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
        return Math.abs(hashCodeOf(key)) % capacity;
    }

    private static int hashCodeOf(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }

    private static boolean areEqual(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
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
            return new Entry((K) nextEntry.key, (V) nextEntry.value);
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

//import java.util.ArrayList;
//import java.util.Iterator;
//
//public class PersistentMap<K, V> {
//    private static final int INITIAL_SIZE = 100;
//    private static final PersistentList<_Entry> INITIAL_ROW = new PersistentList<>();
//    private static final PersistentList<PersistentList<_Entry>> INITIAL_TABLE;
//    private static final PersistentList<PersistentList<_Entry>> EMPTY_TABLE = new PersistentList<>();
//    private final PersistentList<PersistentList<_Entry>> table;
//
//    static {
//        final var initialTableRows = new ArrayList<PersistentList<_Entry>>(INITIAL_SIZE);
//
//        for (int i = 0; i < INITIAL_SIZE; ++i) {
//            initialTableRows.add(INITIAL_ROW);
//        }
//
//        INITIAL_TABLE = new PersistentList<>(initialTableRows);
//        System.out.println(INITIAL_TABLE);
//    }
//
//    private final int size;
//
//    private PersistentMap(PersistentList<PersistentList<_Entry>> table, int size) {
//        this.table = table;
//        this.size = size;
//    }
//
//    private PersistentMap(PersistentList<PersistentList<_Entry>> table) {
//        this(table, 0);
//    }
//
//    public PersistentMap() {
//        this(INITIAL_TABLE, 0);
//    }
//
//    public PersistentMap<K, V> put(K key, V value) {
//        if (getEntry(key) != null) {
//            return this;
//        } else {
//            return new PersistentMap<>(withInsertion(key, value), size + 1);
//        }
//    }
//
//    public PersistentMap<K, V> remove(K key) {
//        final var newTable = without(key);
//
//        return newTable == null ? this : new PersistentMap<>(newTable, size - 1);
//    }
//
//    public V get(K key) {
//        final var entry = getEntry(key);
//        return entry == null ? null : (V) entry.value;
//    }
//
//    public boolean containsKey(K key) {
//        return getEntry(key) != null;
//    }
//
//
//    private PersistentList<PersistentList<_Entry>> withInsertion(K key, V value) {
//        final var tableIndex = tableIndexOf(key);
//        final var result = table.set(tableIndex, table.get(tableIndex).add(new _Entry(key, value)));
//        System.out.println(result);
//        return result;
//    }
//
//    private _Entry getEntry(K key) {
//        final var tableIndex = tableIndexOf(key);
//
//        for (final var entry : table.get(tableIndex)) {
//            if (areEqual(key, entry.key)) {
//                return entry;
//            }
//        }
//
//        return null;
//    }
//
//    private PersistentList<PersistentList<_Entry>> without(K key) {
//        final var tableIndex = tableIndexOf(key);
//        final var row = table.get(tableIndex);
//
//        int rowIndex = 0;
//        for (final var entry : row) {
//            if (areEqual(key, entry.key)) {
//                return table.set(tableIndex, row.remove(rowIndex));
//            }
//            ++rowIndex;
//        }
//
//        return null;
//    }
//
//    private int tableIndexOf(K value) {
//        return Math.abs(hashCodeOf(value)) % table.size();
//    }
//
//    private static int hashCodeOf(Object obj) {
//        return obj == null ? 0 : obj.hashCode();
//    }
//
//    private static boolean areEqual(Object a, Object b) {
//        if (a == null) {
//            return b == null;
//        } else {
//            return a.equals(b);
//        }
//    }
//
//    private PersistentList<PersistentList<_Entry>> makeInitialTable(int size) {
//        if (size <= 0) return EMPTY_TABLE;
//        final var initialRows = new ArrayList<PersistentList<_Entry>>(INITIAL_SIZE);
//
//        for (int i = 0; i < INITIAL_SIZE; ++i) {
//            initialRows.add(INITIAL_ROW);
//        }
//
//        return new PersistentList<>(initialRows);
//        final var initialRows;
//    }
//
//    private PersistentList<PersistentList<_Entry>> resize(int newSize) {
//        var newTable = makeInitialTable(newSize);
//
//    }
//
//    private static class _Entry {
//        final Object key;
//        final Object value;
//
//        private _Entry(Object key, Object item) {
//            this.key = key;
//            this.value = item;
//        }
//    }
//
//    private class Entry {
//        final K key;
//        final V value;
//
//        private Entry(K key, V value) {
//            this.key = key;
//            this.value = value;
//        }
//    }
//
//
//    public class OwnIterator implements Iterator<Entry> {
//        private volatile int nextRowIndex;
//        private volatile int nextColumnIndex;
//
//        private volatile PersistentList<_Entry> nextRow;
//
//
//        public void reset() {
//            nextRowIndex = 0;
//            nextColumnIndex = 0;
//            nextRow = null;
//        }
//
//        private _Entry nextEntry() {
//            if (hasNext()) {
//                return nextRow.get(nextColumnIndex);
//            } else {
//                return null;
//            }
//        }
//
//        private void advance(){
//            if (nextRow == null) {
//                nextRow = PersistentMap.this.table.get(nextRowIndex);
//            }
//
//            if (nextColumnIndex < nextRow.size()){
//                ++nextColumnIndex;
//            } else {
//                nextColumnIndex = 0;
//                do{
//                    ++nextRowIndex;
//                    nextRow = PersistentMap.this.table.get(nextRowIndex);
//                } while(nextRow.size() == 0);
//            }
//        }
//
//        public boolean hasNext() {
//            if (nextRowIndex < PersistentMap.this.table.size()) {
//                if (nextRow == null) {
//                    nextRow = PersistentMap.this.table.get(nextRowIndex);
//                }
//
//                return nextColumnIndex < nextRow.size();
//            } else return false;
//        }
//
//        public Entry next() {
//            if (!hasNext()) return null;
//            final var result = nextEntry();
//            advance();
//            return new Entry((K) result.key, (V) result.value);
//        }
//    }
//}