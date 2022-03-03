import java.util.Objects;

public class ArrayUtils {

    public Object[] remove(Object[] original, int start, int length) {
        return remove(original, start, length, false, false);
    }

    public Object[] remove(Object[] original, int start, int length, boolean reversed) {
        return remove(original, start, length, reversed, false);
    }

    public Object[] remove(Object[] original, int start, int length, boolean reversed, boolean reverseResult) {
        Objects.requireNonNull(original);
        requireRangeInBounds(start, length, original.length);
        if (length == 0) return original;

        final var result = new Object[original.length - length];
        final var end = start + length;

        // copy preceding
        arraycopy(original, 0, result, 0, start, reversed, reverseResult);

        // copy proceeding
        arraycopy(original, end, result, start, original.length - end, reversed, reverseResult);

        return result;
    }

    public Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source) {
        Objects.requireNonNull(source);
        return replace(destination, destinationStart, source, 0, source.length, false, false, false);
    }

    public Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart) {
        Objects.requireNonNull(source);
        return replace(destination, destinationStart, source, sourceStart, source.length - sourceStart, false, false, false);
    }

    public Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length) {
        return replace(destination, destinationStart, source, sourceStart, length, false, false, false);
    }

    public Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed) {
        return replace(destination, destinationStart, source, sourceStart, length, destinationReversed, false, false);
    }

    public Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed,
            boolean sourceReversed) {
        return replace(destination, destinationStart, source, sourceStart, length,
                destinationReversed, sourceReversed, false);
    }

    public Object[] replace(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed,
            boolean sourceReversed,
            boolean reverseResult) {
        Objects.requireNonNull(destination);
        Objects.requireNonNull(source);
        requireRangeInBounds(destinationStart, length, destination.length);
        requireRangeInBounds(sourceStart, length, source.length);
        if (length == 0) return destination;
        if (length == destination.length && length == source.length) return source;

        final var result = new Object[destination.length];
        final var destinationEnd = destinationStart + length;

        // copy preceding
        arraycopy(destination, 0, result, 0, destinationStart,
                destinationReversed, reverseResult);

        // copy replacement
        arraycopy(source, sourceStart, result, destinationStart, length,
                sourceReversed, reverseResult);

        // copy proceeding
        arraycopy(destination, destinationEnd, result, destinationEnd, destination.length - destinationEnd,
                destinationReversed, reverseResult);

        return result;
    }

    public Object[] get(Object[] original, int start, int length) {
        return get(original, start, length, false, false, );
    }

    public Object[] get(Object[] original, int start, int length, boolean reversed) {
        return get(original, start, length, reversed, false);
    }

    public Object[] get(Object[] original, int start, int length, boolean reversed, boolean reverseResult) {
        Objects.requireNonNull(original);
        requireRangeInBounds(start, length, original.length);
        if (length == 0) return new Object[0];
        if (length == original.length) return original;

        final var result = new Object[length];

        // copy range
        arraycopy(original, start, result, 0, length, reversed, reverseResult);

        return result;
    }

    public Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source) {
        Objects.requireNonNull(source);
        return insert(destination, destinationStart, source, 0, source.length, false, false, false);
    }

    public Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart) {
        Objects.requireNonNull(source);
        return insert(destination, destinationStart, source, sourceStart, source.length - sourceStart, false, false, false);
    }

    public Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length) {
        return insert(destination, destinationStart, source, sourceStart, length, false, false, false);
    }

    public Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed) {
        return insert(destination, destinationStart, source, sourceStart, length, destinationReversed, false, false);
    }

    public Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed,
            boolean sourceReversed) {
        return insert(destination, destinationStart, source, sourceStart, length, destinationReversed, sourceReversed, false);
    }

    public Object[] insert(
            Object[] destination,
            int destinationStart,
            Object[] source,
            int sourceStart,
            int length,
            boolean destinationReversed,
            boolean sourceReversed,
            boolean reverseResult) {
        Objects.requireNonNull(destination);
        Objects.requireNonNull(source);
        requireIndexInBounds(destinationStart, destinationStart + 1);
        requireRangeInBounds(sourceStart, length, source.length);
        if (length == 0) return destination;

        final var result = new Object[destination.length + length];
        final var rangeEnd = destinationStart + length;

        // copy preceding
        arraycopy(destination, 0, result, 0, destinationStart,
                destinationReversed, reverseResult);

        // copy insertion
        arraycopy(source, sourceStart, result, destinationStart, length,
                sourceReversed, reverseResult);

        // copy proceeding
        arraycopy(destination, destinationStart, result, rangeEnd, result.length - rangeEnd,
                destinationReversed, reverseResult);

        return result;
    }

    // single

    public Object[] remove(Object[] original, int index) {
        return remove(original, index, false, false);
    }

    public Object[] remove(Object[] original, int index, boolean reversed) {
        return remove(original, index, reversed, false);
    }

    public Object[] remove(Object[] original, int index, boolean reversed, boolean reverseResult) {
        return remove(original, index, 1, reversed, reverseResult);
    }

    public Object[] set(Object[] destination, int index, Object item) {
        return set(destination, index, item, false, false);
    }

    public Object[] set(Object[] destination, int index, Object item, boolean reversed) {
        return set(destination, index, item, reversed, false);
    }

    public Object[] set(Object[] destination, int index, Object item, boolean reversed, boolean reverseResult) {
        Objects.requireNonNull(destination);
        requireIndexInBounds(index, destination.length);

        final var result = new Object[destination.length];
        final var end = index + 1;

        // copy preceding
        arraycopy(destination, 0, result, 0, index, reversed, reverseResult);

        // copy insertion
        result[reverseIndexIf(reverseResult, index, result.length)]
                = item;

        // copy proceeding
        arraycopy(destination, end, result, end, destination.length - end, reversed, reverseResult);

        return result;
    }

    public Object get(Object[] from, int index) {
        return get(from, index, false);
    }

    public Object get(Object[] from, int index, boolean reversed) {
        Objects.requireNonNull(from);
        requireIndexInBounds(index, from.length);
        return from[reverseIndexIf(reversed, index, from.length)];
    }

    public Object[] add(Object[] destination, int index, Object item) {
        return add(destination, index, item, false, false);
    }

    public Object[] add(Object[] destination, int index, Object item, boolean reversed) {
        return add(destination, index, item, reversed, false);
    }

    public Object[] add(Object[] destination, int index, Object item, boolean reversed, boolean reverseResult) {
        Objects.requireNonNull(destination);
        requireIndexInBounds(index, destination.length + 1);

        final var result = new Object[destination.length + 1];
        final var rangeEnd = index + 1;

        // copy preceding
        arraycopy(destination, 0, result, 0, index, reversed, reverseResult);

        // copy insertion
        result[reverseIndexIf(reverseResult, index, result.length)]
                = item;

        // copy proceeding
        arraycopy(destination, index, result, rangeEnd, result.length - rangeEnd, reversed, reverseResult);

        return result;
    }

    public void arraycopy(
            Object[] source,
            int sourceStart,
            Object[] destination,
            int destinationStart,
            int length,
            boolean sourceReversed,
            boolean destinationReversed) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(destination);
        requireRangeInBounds(sourceStart, length, source.length);
        requireRangeInBounds(destinationStart, length, destination.length);

        int s = reverseIndexIf(sourceReversed, sourceStart, source.length);
        int d = reverseIndexIf(destinationReversed, destinationStart, destination.length);
        final var sd = sourceReversed ? -1 : 1;
        final var dd = destinationReversed ? -1 : 1;

        for (int i = 0; i < length; i++) {
            destination[d] = source[s];
            s += sd;
            d += dd;
        }
    }

    public int requireIndexInBounds(int index, int upper) {
        return requireIndexInBounds(0, index, upper);
    }

    public int requireIndexInBounds(int lower, int index, int upper) {
        if (lower <= index && index < upper) {
            return index;
        } else {
            throw new IndexOutOfBoundsException(index);
        }
    }

    public int requireRangeInBounds(int start, int length, int upper) {
        return requireRangeInBounds(0, start, length, upper);
    }

    public int requireRangeInBounds(int lower, int start, int length, int upper) {
        if (lower > start) throw new IndexOutOfBoundsException(start);

        requireNonNegative(length);

        if (upper <= start + length) throw new IndexOutOfBoundsException(start + length);

        return start;
    }

    public int requireNonNegative(int number) {
        if (number >= 0) {
            return number;
        } else {
            throw new IllegalArgumentException("number must not be negative.");
        }
    }

    public int reverseIndexIf(boolean condition, int index, int length) {
        if (condition) {
            return reverseIndex(index, length);
        } else {
            return index;
        }
    }

    public int reverseIndex(int index, int length) {
        return length - index - 1;
    }
}