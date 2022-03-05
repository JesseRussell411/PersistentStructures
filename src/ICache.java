public interface ICache<T> {
    T getFresh();
    T get();
    T peak();
    boolean isCached();
    void invalidate();
}
