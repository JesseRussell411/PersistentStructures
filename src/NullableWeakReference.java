
import java.lang.ref.WeakReference;

/** a lot like WeakReference<T> except it keeps track of whether
 * the thing it references was originally null. */
public class NullableWeakReference<T> {
    private final WeakReference<T> ref;

    public NullableWeakReference(T value) {
        if (value == null) {
            ref = null;
        } else {
            ref = new WeakReference<>(value);
        }
    }

    public boolean isCollected() {
        if (ref == null) {
            // null will never be collected because it doesn't exist.
            return false;
        } else {
            return ref.get() == null;
        }
    }

    public boolean isNull() {
        return ref == null;
    }

    public T get() {
        if (ref == null) {
            return null;
        } else {
            return ref.get();
        }
    }
}
