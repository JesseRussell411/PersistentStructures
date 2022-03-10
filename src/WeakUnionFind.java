import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public class WeakUnionFind<T> {
    private Map<T, T> data = new WeakHashMap<>();

    public void union(T a, T b) {

    }

    public T find(T t) {
        if (data.containsKey(t)) {
            final var jumpStack = new LinkedList<T>();
            jumpStack.push(t);
            do {
                final var current = data.get(jumpStack.peek());
                if (Objects.equals(current, jumpStack.peek())) break;
                jumpStack.push(current);
            } while (data.containsKey(jumpStack.peek()));

            final var result = jumpStack.pop();
        } else {
            return t;
        }
    }
}
