import java.util.function.Supplier;

public class WeakCache<T> implements ICache<T> {
    private volatile NullableWeakReference<T> value;
    private volatile boolean clean = false;
    private final Supplier<T> update;

    public WeakCache(Supplier<T> update) {
        this.update = update;
    }

    public T get() {
        if (clean) {
            final var ref = this.value;
            final var value = ref.get();
            if (ref.isNull() || value != null) return value;
        }
        synchronized (this) {
            if (clean) {
                final var ref = this.value;
                final var value = ref.get();
                if (ref.isNull() || value != null) return value;
            }

            return getFresh();
        }
    }

    public synchronized T getFresh() {
        final var result = update.get();
        value = new NullableWeakReference<>(result);
        clean = true;
        return result;
    }

    public T peak() {
        return value.get();
    }

    public boolean isCached() {
        return clean && value.isCollected();
    }

    public void invalidate() {
        clean = false;
    }
}