import java.util.Objects;
import java.util.function.Supplier;

public class Cache<T> implements ICache<T> {
    private volatile T value = null;
    private volatile boolean clean = false;
    private final Supplier<T> update;

    public Cache(Supplier<T> update) {
        Objects.requireNonNull(update);
        this.update = update;
    }

    public void invalidate() {
        clean = false;
    }

    public boolean isCached() {
        return clean;
    }

    public T get() {
        if (clean) return value;

        synchronized (this) {
            if (clean) return value;

            return getFresh();
        }
    }

    public synchronized T getFresh() {
        value = update.get();
        clean = true;
        return value;
    }

    public T peak() {
        return value;
    }
}
