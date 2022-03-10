import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DoubleWeakHashMap<K, V> implements Map<K, V> {
    private final WeakHashMap<K, NullableWeakReference<V>> data = new WeakHashMap<>();


    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        final var found = new Ref<>(false);

        if (value == null) {
            forEach(e -> {
                if (e.getValue().isNull()) {
                    found.current = true;
                }
            });
        } else {
            forEach(e -> {
                if (Objects.equals(e.getValue(), value)) {
                    found.current = true;
                }
            });
        }

        return found.current;
    }

    @Override
    public V get(Object key) {
        final var result = data.get(key);
        if (result.isCollected()) {
            data.remove(key);
        }
        return result.get();
    }

    @Override
    public V put(K key, V value) {
        final var result = data.put(key, new NullableWeakReference<>(value));
        if (result == null) {
            return null;
        } else {
            return result.get();
        }
    }

    @Override
    public V remove(Object key) {
        return data.remove(key).get();
    }

    public void removeMany(Iterable<K> keys) {
        for (final var key : keys) {
            remove(key);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public Set<K> keySet() {
        return data.keySet();
    }

    @Override
    public Collection<V> values() {
        final var result = new ArrayList<V>(size());
        forEach(e -> result.add(e.getValue().get()));
        result.trimToSize();
        return result;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        final var result = new HashSet<Entry<K, V>>();
        forEach(e -> result.add(Map.entry(e.getKey(), e.getValue().get())));
        return result;
    }

    public void forEach(Consumer<Map.Entry<K, NullableWeakReference<V>>> action) {
        final var toPrune = new LinkedList<K>();

        for (final var entry : data.entrySet()) {
            action.accept(entry);
        }

        removeMany(toPrune);
    }
}
