import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Supplier;

class MemoizedSupplier<T> {
    private volatile T value = null;
    private volatile Supplier<T> update;

    public MemoizedSupplier(Supplier<T> update) {
        Objects.requireNonNull(update);
        this.update = update;
    }

    public T get() {
        if (update == null) return value;

        synchronized (this) {
            if (update == null) return value;
            value = update.get();
            update = null;
            return value;
        }
    }

    public T cachePeak() {
        return value;
    }

    public boolean cached() {
        return update == null;
    }
}
