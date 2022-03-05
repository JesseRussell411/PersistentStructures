import java.util.Objects;
import java.util.function.Supplier;

class Lazy<T> implements Supplier<T> {
    private volatile T value;
    private volatile RuntimeException error;
    private volatile Supplier<T> supplier;

    public boolean isResolved(){
        return error == null && supplier == null;
    }

    public boolean isRejected(){
        return error != null;
    }

    public Lazy(Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        this.supplier = supplier;
    }

    public T get() {
        if (error != null) throw error;
        if (supplier == null) return value;

        synchronized (this) {
            if (error != null) throw error;
            if (supplier == null) return value;

            try {
                value = supplier.get();
                supplier = null;
                return value;
            } catch (RuntimeException e) {
                error = e;
                supplier = null;
                throw e;
            }
        }
    }
}
