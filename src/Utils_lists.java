import java.util.Arrays;
import java.util.Objects;

class Utils_lists {
    public static int requireIndexInBounds(int index, int length) {
        if (0 <= index || index < length) {
            return index;
        } else {
            throw new IndexOutOfBoundsException(index);
        }
    }

    public static int requirePositiveLength(int length) {
        if (length >= 0) {
            return length;
        } else {
            throw new IllegalArgumentException("length must be 0 or greater.");
        }
    }

    public static class At_Start_Length {
        public final int at;
        public final int start;
        public final int length;

        public At_Start_Length(int at, int start, int length) {
            this.at = at;
            this.start = start;
            this.length = length;
        }
    }

    public static At_Start_Length bindAt_Start_Length(int dstLength, int srcLength, int at, int start, int length) {
        final var initStart = start;

        if (length < 0) {
            start += length;
            length = Math.abs(length);
        }

        if (at < 0) {
            start -= at;
            at = 0;
        }

        if (start < 0) {
            at -= start;
            start = 0;
        }

        if (at > dstLength) return null;
        if (start > srcLength) return null;

        length += initStart - start;

        if (length < 0) return null;

        if (start + length > srcLength) {
            length = srcLength - start;
        }

        return new At_Start_Length(at, start, length);
    }

    public static class Start_Length {
        final int start;
        final int length;

        public Start_Length(int start, int length) {
            this.start = start;
            this.length = length;
        }
    }

    public static Start_Length bindStart_Length(int srcLength, int start, int length) {
        if (length < 0) {
            start += length;
            length = Math.abs(length);
        }

        if (start > srcLength) return null;
        if (start + length < 0) return null;

        if (start < 0) {
            length += start;
            start = 0;
        }

        if (start + length > srcLength) {
            length = srcLength - start;
        }

        return new Start_Length(start, length);
    }

    // =============================================
    // Copy Modification -- array -- multi -- array
    // =============================================
    //remove
    public static Object[] without(Object[] original, int start, int length) {
        Objects.requireNonNull(original);

        final var bound = bindStart_Length(original.length, start, length);
        if (bound != null) {
            return _without(original, bound.start, bound.length);
        } else {
            return original;
        }
    }

    private static Object[] _without(Object[] original, int start, int length) {
        final var result = new Object[original.length - length];

        // copy preceding
        System.arraycopy(original, 0, result, 0, start);
        // copy following
        final var followingStart = start + length + 1;
        final var followingLength = result.length - followingStart;
        System.arraycopy(original, followingStart, result, start, followingLength);

        return result;
    }

    //get
    public static Object[] get(Object[] from) {
        return get(from, 0, from.length);
    }

    public static Object[] get(Object[] from, int start, int length) {
        Objects.requireNonNull(from);
        final var bound = bindStart_Length(from.length, start, length);
        if (bound != null) {
            return _get(from, bound.start, bound.length);
        } else {
            return new Object[0];
        }
    }

    private static Object[] _get(Object[] from, int start, int length) {
        final var result = new Object[length];
        System.arraycopy(from, start, result, 0, length);
        return result;
    }

    //insert
    public static Object[] withInsertion(Object[] original, int at, Object[] insertion) {
        return withInsertion(original, at, insertion, 0, insertion.length);
    }

    public static Object[] withInsertion(Object[] original, int at, Object[] insertion, int start) {
        return withInsertion(original, at, insertion, start, insertion.length - start);
    }

    public static Object[] withInsertion(Object[] original, int at, Object[] insertion, int start, int length) {
        Objects.requireNonNull(original);
        Objects.requireNonNull(insertion);

        final var bound = bindAt_Start_Length(original.length, insertion.length, at, start, length);
        if (bound != null) {
            return _withInsertion(original, bound.at, insertion, bound.start, bound.length);
        } else {
            return original;
        }
    }

    private static Object[] _withInsertion(Object[] original, int at, Object[] insertion, int start, int length) {
        final var result = new Object[original.length + length];

        // copy preceding
        System.arraycopy(original, 0, result, 0, at);

        // copy insertion
        System.arraycopy(insertion, start, result, at, length);

        // copy following
        final var followingStart = at + length;
        final var followingLength = result.length - followingStart;
        System.arraycopy(original, at, result, followingStart, followingLength);

        return result;
    }

    //set
    public static Object[] withReplacement(Object[] original, int at, Object[] replacement) {
        return withInsertion(original, at, replacement, 0, replacement.length);

    }

    public static Object[] withReplacement(Object[] original, int at, Object[] replacement, int start) {
        return withInsertion(original, at, replacement, start, replacement.length - start);
    }

    public static Object[] withReplacement(Object[] original, int at, Object[] replacement, int start, int length) {
        Objects.requireNonNull(original);
        Objects.requireNonNull(replacement);

        final var bound = bindAt_Start_Length(original.length, replacement.length, at, start, length);
        if (bound != null) {
            return _withReplacement(
                    original,
                    bound.at,
                    replacement,
                    bound.start,
                    Math.min(
                            bound.length,
                            original.length - bound.at));
        } else {
            return original;
        }
    }

    private static Object[] _withReplacement(Object[] original, int at, Object[] replacement, int start, int length) {
        final var result = new Object[original.length];

        // copy preceding
        System.arraycopy(original, start, result, 0, at);
        // copy replacement
        System.arraycopy(replacement, start, result, at, length);
        // copy following
        final var followingStart = at + length;
        final var followingLength = result.length - followingStart;
        System.arraycopy(original, followingStart, result, followingStart, followingLength);

        return result;
    }

    // ======================================
    //  Copy Modification -- array -- single
    // ======================================
    //remove
    public static Object[] without(Object[] original, int index) {
        Objects.requireNonNull(original);
        if (0 <= index && index < original.length) {
            return _without(original, index);
        } else {
            return original;
        }
    }

    private static Object[] _without(Object[] original, int index) {
        final var result = new Object[original.length - 1];

        // copy preceding
        System.arraycopy(original, 0, result, 0, index);
        // copy following
        final var followingStart = index + 1;
        final var followingLength = original.length - followingStart;
        System.arraycopy(original, followingStart, result, index, followingLength);

        return result;
    }

    //get
    public static Object get(Object[] original, int index) {
        Objects.requireNonNull(original);
        if (0 <= index && index < original.length) {
            return _get(original, index);
        } else {
            return null;
        }
    }

    private static Object _get(Object[] original, int index) {
        return original[index];
    }

    //insert
    public static Object[] withAddition(Object[] original, int at, Object addition) {
        if (0 <= at && at <= original.length) {
            return _withAddition(original, at, addition);
        } else {
            return original;
        }
    }

    private static Object[] _withAddition(Object[] original, int at, Object addition) {
        final var result = new Object[original.length + 1];

        // copy preceding
        System.arraycopy(original, 0, result, 0, at);
        // copy addition
        result[at] = addition;
        // copy following
        System.arraycopy(original, at, result, at + 1, original.length - at);

        return result;
    }

    //set
    public static Object[] withSwap(Object[] original, int at, Object replacement) {
        Objects.requireNonNull(original);
        if (0 <= at && at < original.length) {
            return _withSwap(original, at, replacement);
        } else {
            return original;
        }
    }

    private static Object[] _withSwap(Object[] original, int at, Object replacement) {
        final var result = new Object[original.length];

        // copy preceding
        System.arraycopy(original, 0, result, 0, at);
        // copy replacement
        result[at] = replacement;
        // copy following
        System.arraycopy(original, at + 1, result, at + 1, original.length - at - 1);

        return result;
    }
    //======================================================================================================

//
//    public static Object[] copy(Object[] original) {
//        final var result = new Object[original.length];
//        System.arraycopy(original, 0, result, 0, original.length);
//        return result;
//    }
//
//    public static Object[][] partition(Object[] array, int size) {
//        Objects.requireNonNull(array);
//        if (size < 1) throw new IllegalArgumentException("size must be greater than 1.");
//        if (size >= array.length) return new Object[][]{array};
//
//        final var count = array.length / size;
//        final var remainder = array.length % size;
//        final var totalCount = count + (remainder > 0 ? 1 : 0);
//        final var result = new Object[totalCount][];
//
//        // create whole partitions
//        for (int i = 0; i < count; i++) {
//            final var index = i * size;
//            result[i] = Arrays.copyOfRange(array, index, index + size);
//        }
//
//        // create remainder partition if needed
//        if (remainder > 0) {
//            final var index = count * size;
//            result[count] = Arrays.copyOfRange(array, index, index + remainder);
//        }
//
//        return result;
//    }
//
//    public static Object[] withAddition(Object[] original, int index, Object item) {
//        Objects.requireNonNull(original);
//        requireIndexInBounds(index, original.length + 1);
//
//        final var result = new Object[original.length + 1];
//
//        // copy preceding section
//        for (int i = 0; i < index; i++) {
//            result[i] = original[i];
//        }
//
//        // copy insertion
//        result[index] = item;
//
//        // copy following section
//        for (int i = index + 1; i < result.length; i++) {
//            result[i] = original[i - 1];
//        }
//
//        return result;
//    }
//
//    public static Object[] get(Object[] items, int start, int length) {
//        Objects.requireNonNull(items);
//        requireIndexInBounds(start, items.length);
//        requirePositiveLength(length);
//        if (length == 0) return new Object[0];
//
//        if (start + length < items.length) {
//            return _get(items, start, length);
//        } else {
//            return _get(items, start, items.length - start);
//        }
//
//    }
//
//    private static Object[] _get(Object[] items, int start, int length) {
//        final var result = new Object[length];
//
//        System.arraycopy(items, start, result, 0, length);
//
//        return result;
//    }

//    public static Object[] without(Object[] original, int index) {
//        Objects.requireNonNull(original);
//        requireIndexInBounds(index, original.length);
//        if (original.length <= 1) return new Object[0];
//
//        final var result = new Object[original.length - 1];
//
//        // copy preceding section
//        for (int i = 0; i < index; i++) {
//            result[i] = original[i];
//        }
//
//        // copy following section
//        for (int i = index + 1; i < index; i++) {
//            result[i - 1] = original[i];
//        }
//
//        return result;
//    }
//
//    public static Object[] withSwap(Object[] original, int index, Object replacement) {
//        Objects.requireNonNull(original);
//        requireIndexInBounds(index, original.length);
//
//        final var result = new Object[original.length];
//
//        // copy preceding section
//        for (int i = 0; i < index; i++) {
//            result[i] = original[i];
//        }
//
//        // copy replacement
//        result[index] = replacement;
//
//        // copy following section
//        for (int i = index + 1; i < result.length; i++) {
//            result[i] = original[i];
//        }
//    }
//
//
//    public static Object[] without(Object[] original, int index, int length) {
//        Objects.requireNonNull(original);
//        requireIndexInBounds(index, original.length);
//
//        if (index + length < original.length) {
//            return _without(original, index, length);
//        } else {
//            return _without(original, index, original.length - index);
//        }
//    }
//
//    private static Object[] _without(Object[] original, int index, int length) {
//        if (length <= 0) return original;
//
//
//        final var result = new Object[original.length - length];
//
//        // copy preceding section
//        for (int i = 0; i < index; i++) {
//            result[i] = original[i];
//        }
//
//        // copy following section
//        for (int i = index + length + 1; i < result.length; i++) {
//            result[i] = original[i + length];
//        }
//
//        return result;
//    }
//
//    public static Object[] withInsertion(Object[] original, int index, Object[] items) {
//        Objects.requireNonNull(original);
//        Objects.requireNonNull(index);
//        requireIndexInBounds(index, original.length);
//
//        final var result = new Object[original.length + items.length];
//
//        // copy preceding section
//        for (int i = 0; i < index; i++) {
//            result[i] = original[i];
//        }
//
//        // copy insertion
//        for (int i = index; i < index + items.length; i++) {
//            result[i] = items[i - index];
//        }
//
//        // copy following section
//        for (int i = index + items.length + 1; i < result.length; i++) {
//            result[i] = original[i - items.length];
//        }
//
//        return result;
//    }

//    public static Object[] withInsertion(Object[] original, int index, Object[] items, int start, int length) {
//        Objects.requireNonNull(original);
//        Objects.requireNonNull(items);
//        requireIndexInBounds(index, original.length);
//        requireIndexInBounds(start, items.length);
//
//        if (start + length < items.length) {
//            return _withInsertion(original, index, items, start, length);
//        } else {
//            return _withInsertion(original, index, items, start, items.length - start);
//        }
//    }
//
//    private static Object[] _withInsertion(Object[] original, int index, Object[] items, int start, int length) {
//        final var result = new Object[original.length + length];
//
//        // copy preceding section
//        for (int i = 0; i < index; i++) {
//            result[i] = original[i];
//        }
//
//        // copy insertion
//        for (int i = index; i < length; i++) {
//            result[i] = items[start + (i - index)];
//        }
//
//        // copy following section
//        for (int i = index + length + 1; i < result.length; i++) {
//            result[i] = original[i - length];
//        }
//
//        return result;
//    }
//
//    public Object[] withReplacement(Object[] original, int index, Object[] items) {
//        return withReplacement(original, index, items, 0, items.length);
//    }
//
//    public Object[] withReplacement(Object[] original, int index, Object[] items, int start, int length) {
//        return _withReplacement(original, index, items, start, Math.min(length, items.length - start));
//    }
//
//    private Object[] _withReplacement(Object[] original, int index, Object[] items, int start, int length) {
//        final var result = new Object[original.length];
//
//        // copy preceding
//        System.arraycopy(original, 0, result, 0, index);
//        // copy replacement
//        System.arraycopy(items, index, result, start, length);
//        // copy following
//        final var startOfFollowing = index + length + 1;
//        System.arraycopy(original, startOfFollowing, result, startOfFollowing, original.length - startOfFollowing);
//
//        return result;
//    }


//    public static Object[] reversed(Object[] array) {
//        final var result = new Object[array.length];
//        for (int i = 0; i < array.length; ++i) {
//            result[result.length - 1 - i] = array[i];
//        }
//        return result;
//    }
//
//
//
//    private static <T> boolean isSorted(Iterable<T> items, Comparator<T> comparator) {
//        final var iter = items.iterator();
//
//        if (!iter.hasNext()) return true;
//
//        var prev = iter.next();
//
//        while (iter.hasNext()) {
//            if (comparator.compare(prev, prev = iter.next()) > 0) return false;
//        }
//
//        return true;
//    }
//
//
//
//    private static boolean isSorted(Object[] array, Comparator<Object> comparator) {
//        for (int i = 1; i < array.length; ++i) {
//            if (comparator.compare(array[i - 1], array[i]) > 0) return false;
//        }
//        return true;
//    }
//
//
//    private static Object[] merged(Iterable<Object> a, Iterable<Object> b, Comparator<Object> comparator) {
//        final var enuA = new Enumerator<>(a.iterator());
//        final var enuB = new Enumerator<>(b.iterator());
//        final var result = new ArrayList<>();
//
//        enuA.moveNext();
//        enuB.moveNext();
//
//        while (!enuA.complete() && !enuB.complete()) {
//            if (comparator.compare(enuA.current(), enuB.current()) < 0) {
//                result.add(enuA.current());
//                enuA.moveNext();
//            } else {
//                result.add(enuB.current());
//                enuB.moveNext();
//            }
//        }
//
//        if (!enuA.complete()) {
//            do {
//                result.add(enuA.current());
//            } while (enuA.moveNext());
//        } else if (!enuB.complete()) {
//            do {
//                result.add(enuB.current());
//            } while (enuB.moveNext());
//        }
//
//        return result.toArray();
//    }
//
//
//    private static Object[] withInsertion(Object[] original, int index, Object[] values) {
//        if (values == null || values.length == 0) return original;
//        indexCheck(index, original.length + 1);
//        nullCheck(original);
//
//        final var result = new Object[original.length + values.length];
//
//        for (int i = 0; i < index; ++i) {
//            result[i] = original[i];
//        }
//
//        for (int i = index; i < index + values.length; ++i) {
//            result[i] = values[i - index];
//        }
//
//        for (int i = index + values.length; i < original.length + values.length; ++i) {
//            result[i] = original[i - values.length];
//        }
//
//        return result;
//    }
//
//    private static Object[] withReplacement(Object[] original, int start, int end, Object[] src, int srcStart) {
//        final var length = end - start;
//        final var result = new Object[original.length];
//
//        for (int i = 0; i < start; ++i) {
//            result[i] = original[i];
//        }
//
//        boolean containedChange = false;
//        for (int i = 0; i < length; ++i) {
//            final var indexO = i + start;
//            final var indexS = i + srcStart;
//            final var newItem = src[indexS];
//
//            if (!Objects.equals(original[indexO], newItem)) {
//                containedChange = true;
//            }
//
//            result[indexO] = newItem;
//        }
//
//        for (int i = end; i < original.length; ++i) {
//            result[i] = original[i];
//        }
//
//        if (containedChange) {
//            return result;
//        } else {
//            return original;
//        }
//    }
//
//    private static Object[] withoutRange(Object[] original, int start, int length) {
//        if (length <= 0) return original;
//        if (start == 0 && length == original.length) return EMPTY_ARRAY;
//        indexCheck(start, original.length);
//        if (start + length > original.length) throw new IndexOutOfBoundsException(start + length);
//
//        final var result = new Object[original.length - length];
//
//        for (int i = 0; i < start; ++i) {
//            result[i] = original[i];
//        }
//
//        for (int i = start + length; i < original.length; ++i) {
//            result[i - length] = original[i];
//        }
//
//        return result;
//    }
//
//    private static Object[][] partition(Object[] array, int size) {
//        nullCheck(array);
//        if (size < 1) throw new IllegalArgumentException("Size must be greater than 0.");
//
//        if (size >= array.length) {
//            return new Object[][]{array};
//        }
//
//        final var remainder = array.length % size;
//        final var wholeCount = array.length / size;
//
//        final var result = new Object[wholeCount + (remainder > 0 ? 1 : 0)][];
//
//        for (int i = 0; i < wholeCount; ++i) {
//            final var index = i * size;
//            result[i] = Arrays.copyOfRange(array, index, index + size);
//        }
//
//        if (remainder > 0) {
//            final var index = (wholeCount) * size;
//            result[wholeCount] = Arrays.copyOfRange(array, index, index + remainder);
//        }
//
//        return result;
//    }
//
//    public static void indexCheck(int index, int length) {
//        if (index < 0 || length <= index) throw new IndexOutOfBoundsException(index);
//    }
}
