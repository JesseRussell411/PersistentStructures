import java.util.Iterator;

/**
 * Mimics C#'s IEnumerator which is preferable in some cases (though not all).
 */
public class Enumerator<T> {
    public Enumerator(Iterator<T> iter) {
        this.iter = iter;
    }

    private Iterator<T> iter;
    private Boolean hadNext = null;
    private T current = null;

    public boolean moveNext() {
        advance();
        return hadNext;
    }

    public boolean unStarted() {
        return hadNext == null;
    }

    public boolean complete() {
        return hadNext != null && !hadNext;
    }

    public T current() {
        return current;
    }

    private void advance() {
        if (iter.hasNext()) {
            hadNext = true;
            current = iter.next();
        } else {
            hadNext = false;
            current = null;
        }
    }
}
