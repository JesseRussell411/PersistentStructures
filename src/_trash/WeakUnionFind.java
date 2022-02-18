package _trash;

import java.util.Map;
import java.util.Objects;


public class WeakUnionFind<T>{
    private final Map<T, T> data = new DoubleWeakMap<>();

    public T find(T item){
        final var found = data.get(item);
        if (found == null) return item;
        if (found == item) return item;
        return find(item);
    }

    public void union(T a, T b){
        if (Objects.equals(a, b)) return;

        final var foundA = data.get(a);
        final var foundB = data.get(b);

        if (foundA != null && !Objects.equals(a, foundA)){
            union(b, foundA);
        } else if (foundB != null && !Objects.equals(b, foundB)){
            union(a, foundB);
        } else {
            data.put(a, b);
        }
    }
}
