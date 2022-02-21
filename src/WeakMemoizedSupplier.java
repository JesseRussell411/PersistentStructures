import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Supplier;

class WeakMemoizedSupplier<T> {
    private volatile WeakReference<T> value = new WeakReference<>(null);
    private final Supplier<T> update;

    public WeakMemoizedSupplier(Supplier<T> update) {
        Objects.requireNonNull(update);
        this.update = update;
    }

    public T get() {
        var result = value.get();
        if (result != null) return result;

        synchronized (this) {
            result = value.get();
            if (result != null) return result;

            result = update.get();
            value = new WeakReference<>(result);
        }
        return result;
    }

    public T cachePeak() {
        return value.get();
    }
}
