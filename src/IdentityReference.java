/**
 * The equality and hashCode of this class is based on the instance of the Object it stores,
 * regardless of the Object's own definition of equality.
 * @param <T>
 */
public class IdentityReference<T> {

    public final T value;

    public IdentityReference(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IdentityReference<?> ir && value == ir.value;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(value);
    }
}
