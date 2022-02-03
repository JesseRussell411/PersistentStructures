import java.util.ArrayList;
import java.util.Iterator;

public class PersistentMap<K, V> {
    private static final int INITIAL_SIZE = 100;
    private static final PersistentList<Entry> INITIIAL_ROW = new PersistentList<>();
    private static final PersistentList<PersistentList<Entry>> INITIAL_TABLE;
    private PersistentList<PersistentList<Entry>> table;
    static {
        final var initialTableRows = new ArrayList<PersistentList<Entry>>(INITIAL_SIZE);

        for(int i = 0; i < INITIAL_SIZE; ++i){
            initialTableRows.add(INITIIAL_ROW);
        }

        INITIAL_TABLE = new PersistentList<>(initialTableRows);
        System.out.println(INITIAL_TABLE);
    }

    private final int size;

    private PersistentMap(PersistentList<PersistentList<Entry>> table, int size) {
        this.table = table;
        this.size = size;
    }

    private PersistentMap(PersistentList<PersistentList<Entry>> table){
        this(table, 0);
    }

    public PersistentMap(){
        this(INITIAL_TABLE, 0);
    }

    public PersistentMap<K, V> put(K key, V value) {
        if (getEntry(key) != null) {
            return this;
        } else {
            return new PersistentMap<>(withInsertion(key, value), size + 1);
        }
    }

    public PersistentMap<K, V> remove(K key) {
        final var newTable = without(key);

        return newTable == null ? this : new PersistentMap<>(newTable, size - 1);
    }

    public V get(K key) {
        final var entry = getEntry(key);
        return entry == null ? null : (V)entry.value;
    }

    public boolean containsKey(K key) {
        return getEntry(key) != null;
    }


    private PersistentList<PersistentList<Entry>> withInsertion(K key, V value) {
        final var tableIndex = tableIndexOf(key);
        final var result = table.set(tableIndex, table.get(tableIndex).add(new Entry(key, value)));
        System.out.println(result);
        return result;
    }

    private Entry getEntry(K key) {
        final var tableIndex = tableIndexOf(key);

        for (final var entry : table.get(tableIndex)) {
            if (areEqual(key, entry.key)) {
                return entry;
            }
        }

        return null;
    }

    private PersistentList<PersistentList<Entry>> without(K key){
        final var tableIndex = tableIndexOf(key);
        final var row = table.get(tableIndex);

        int rowIndex = 0;
        for(final var entry : row){
            if (areEqual(key, entry.key)){
                return table.set(tableIndex, row.remove(rowIndex));
            }
            ++rowIndex;
        }

        return null;
    }

    private int tableIndexOf(K value) {
        return Math.abs(hashCodeOf(value)) % table.size();
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

    public static class Entry {
        final Object key;
        final Object value;

        private Entry(Object key, Object item) {
            this.key = key;
            this.value = item;
        }
    }


//    public class OwnIterator implements Iterator<Entry<K, V>> {
//        private volatile int index;
//
//        public void reset() {
//            index = 0;
//        }
//
//        public boolean hasNext(){
//            return
//        }
//    }
}