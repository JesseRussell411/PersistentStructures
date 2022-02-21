package _trash;

import _utils.ObjectUtils;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Collectors;

/**
 * !Not thread safe!
 * <p>
 * Like a WeakHashMap but the values are weak as well as the keys.
 *
 * @param <K>
 * @param <V>
 */
public class DoubleWeakMap<K, V> implements Map<K, V> {
    private final WeakHashMap<K, WeakReference<V>> data = new WeakHashMap<>();

    public V get(Object key) {
        final var weakRef = data.get(key);

        if (weakRef == null || weakRef.get() == null) {
            // prune empty entry
            data.remove(key);

            return null;
        } else {
            return weakRef.get();
        }
    }

    public V put(K key, V value) {
        return data.put(key, new WeakReference<>(value)).get();
    }

    public V remove(Object key) {
        final var result = data.remove(key);
        return result == null ? null : result.get();
    }

    public boolean remove(Object key, Object value) {
        final var weakRef = data.get(key);
        if (weakRef == null || weakRef.get() == null) {
            // prune empty entry
            data.remove(key);

            return false;
        } else if (Utils_object.equals(weakRef.get(), value)) {
            return data.remove(key, weakRef);
        } else {
            return false;
        }
    }

    public int size() {
        return data.size();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    public boolean containsValue(Object value) {
        for (final var entry : data.entrySet()) {
            final var weakRef = entry.getValue();

            if (weakRef == null || weakRef.get() == null) {
                // prune empty entry
                data.remove(entry.getKey());
            } else if (Utils_object.equals(weakRef.get(), value)) {
                return true;
            }
        }

        return false;
    }

    public void putAll(Map<? extends K, ? extends V> entries) {
        for (final var entry : entries.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void clear() {
        data.clear();
    }

    public Set<K> keySet() {
        return data.keySet();
    }

    public Collection<V> values() {
        final var toRemove = new LinkedList<K>();
        final var result = data.entrySet()
                .stream()
                .map(
                        entry -> {
                            final var weakRef = entry.getValue();
                            if (weakRef == null || weakRef.get() == null) {
                                //prune empty entry
                                toRemove.add(entry.getKey());

                                return null;
                            } else {
                                return entry.getValue();
                            }

                        })
                .filter(weakRef -> weakRef != null)
                .map(weakRef -> weakRef.get())
                .toList();

        // prune empty entries
        for (final var key : toRemove) {
            data.remove(key);
        }

        return result;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        final var toRemove = new LinkedList<K>();
        final var result = data
                .entrySet()
                .stream()
                .map(
                        entry -> {
                            final var weakRef = entry.getValue();
                            if (weakRef == null || weakRef.get() == null) {
                                //prune empty entry
                                toRemove.add(entry.getKey());

                                return null;
                            } else {
                                return Map.entry(entry.getKey(), weakRef.get());
                            }
                        })
                .filter(entry -> entry != null)
                .collect(Collectors.toSet());

        // prune empty entries
        for (final var key : toRemove) {
            data.remove(key);
        }

        return result;
    }
}
