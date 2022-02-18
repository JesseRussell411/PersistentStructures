package _trash;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Supplier;

public class WeakSingleCache<T> {
    private volatile WeakReference<T> value = null;
    private Supplier<T> makeValue;

    public WeakSingleCache(Supplier<T> makeValue) {
        this.makeValue = Objects.requireNonNull(makeValue);
    }

    public boolean cached() {
        return value != null;
    }

    public T get() {
        if (value != null && value)
        return value == null ? null : value.get();
    }

}
