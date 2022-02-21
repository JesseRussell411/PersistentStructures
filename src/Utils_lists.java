import java.util.Arrays;
import java.util.Objects;

class Utils_lists {
    private static int reverseIndex(int index, int length) {
        return length - 1 - index;
    }

    private static int sign(int num) {
        if (num < 0) {
            return -1;
        } else if (num > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public static void arrayCopy(Object[] src, int srcPos, Object[] dest, int destPos, int length) {
        arrayCopy(src, srcPos, dest, destPos, length, false, false);
    }

    public static void arrayCopy(Object[] src, int srcPos, Object[] dest, int destPos, int length, boolean srcReversed, boolean destReversed) {
        Objects.requireNonNull(src);
        Objects.requireNonNull(dest);

        final var bound = bindDestPosAndSrcPos(dest.length, src.length, destPos, srcPos, length);
        if (bound == null) return;

        if (length >= 0) {
            final var destLengthOverflow = (bound.destPos + bound.length) - dest.length;
            final var srcLengthOverflow = (bound.srcPos + bound.length) - src.length;
            if (destLengthOverflow > 0 || srcLengthOverflow > 0) {
                if (destLengthOverflow > srcLengthOverflow) {
                    _arrayCopy(src, bound.srcPos, dest, bound.destPos, dest.length - bound.destPos, srcReversed, destReversed);
                } else {
                    _arrayCopy(src, bound.srcPos, dest, bound.destPos, src.length - bound.srcPos, srcReversed, destReversed);
                }
            } else {
                _arrayCopy(src, bound.srcPos, dest, bound.destPos, bound.length, srcReversed, destReversed);
            }
        } else {
            final var destLengthUnderflow = bound.destPos + length + 1;
            final var srcLengthUnderflow = bound.srcPos + length + 1;
            if (destLengthUnderflow < 0 || srcLengthUnderflow < 0) {
                if (destLengthUnderflow < srcLengthUnderflow) {
                    _arrayCopy(src, bound.srcPos, dest, bound.destPos, -(destPos + 1), srcReversed, destReversed);
                } else {
                    _arrayCopy(src, bound.srcPos, dest, bound.destPos, -(srcPos + 1), srcReversed, destReversed);
                }
            } else {
                _arrayCopy(src, bound.srcPos, dest, bound.destPos, bound.length, srcReversed, destReversed);
            }
        }


    }

    public static void _arrayCopy(Object[] src, int srcPos, Object[] dest, int destPos, int length, boolean srcReversed, boolean destReversed) {

        int s = srcReversed ? reverseIndex(srcPos, src.length) : srcPos;
        int d = destReversed ? reverseIndex(destPos, dest.length) : destPos;
        int direction = sign(length);

        for (int l = 0; l < Math.abs(length); l++) {
            dest[d] = src[s];
            d += destReversed ? -direction : direction;
            s += srcReversed ? -direction : direction;
        }
    }


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

    public static class DestPos_SrcPos_Length {
        public final int destPos;
        public final int srcPos;
        public final int length;

        public DestPos_SrcPos_Length(int destPos, int srcPos, int length) {
            this.destPos = destPos;
            this.srcPos = srcPos;
            this.length = length;
        }
    }

    public static DestPos_SrcPos_Length bindDestPosAndSrcPos(int destLength, int srcLength, int destPos, int srcPos, int length) {
        if (length >= 0) {
            if (destPos < 0) {
                srcPos -= destPos;
                length += destPos;
                destPos = 0;
            }

            if (srcPos < 0) {
                destPos -= srcPos;
                length += srcPos;
                srcPos = 0;
            }

            if (length < 0) return null;
            if (srcPos >= srcLength) return null;
            if (destPos >= destLength) return null;
        } else {
            if (destPos >= destLength) {
                srcPos -= destPos - (destLength - 1);
                length += destPos - (destLength - 1);
                destPos = destLength - 1;
            }

            if (srcPos >= srcLength) {
                destPos -= srcPos - (srcLength - 1);
                length += srcPos - (srcLength - 1);
                srcPos = srcLength - 1;
            }

            if (length > 0) return null;
            if (srcPos < 0) return null;
            if (destPos < 0) return null;
        }
        return new DestPos_SrcPos_Length(destPos, srcPos, length);
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
    public static Object[] without(Object[] original, int start, int length, boolean reversed) {
        Objects.requireNonNull(original);

        final var bound = bindStart_Length(original.length, start, length);
        if (bound != null) {
            return _without(original, bound.start, bound.length, reversed);
        } else {
            return original;
        }
    }

    private static Object[] _without(Object[] original, int start, int length, boolean reversed) {
        final var result = new Object[original.length - length];

        // copy preceding
        arrayCopy(original, 0, result, 0, start, reversed, false);
        // copy following
        final var followingStart = start + length + 1;
        final var followingLength = result.length - followingStart;
        arrayCopy(original, followingStart, result, start, followingLength, reversed, false);

        return result;
    }

    //get
    public static Object[] get(Object[] from) {
        return get(from, 0, from.length, false);
    }

    public static Object[] get(Object[] from, int start, int length, boolean reversed) {
        Objects.requireNonNull(from);
        final var bound = bindStart_Length(from.length, start, length);
        if (bound != null) {
            return _get(from, bound.start, bound.length, reversed);
        } else {
            return new Object[0];
        }
    }

    private static Object[] _get(Object[] from, int start, int length, boolean reversed) {
        final var result = new Object[length];
        arrayCopy(from, start, result, 0, length, reversed, false);
        return result;
    }

    //insert
    public static Object[] withInsertion(Object[] dest, int destPost, Object[] src) {
        return withInsertion(dest, destPost, src, 0, src.length, false, false);
    }

    public static Object[] withInsertion(Object[] dest, int destPost, Object[] src, int srcPos) {
        return withInsertion(dest, destPost, src, srcPos, src.length - srcPos, false, false);
    }

    public static Object[] withInsertion(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length,
            boolean srcReversed,
            boolean destReversed) {
        Objects.requireNonNull(dest);
        Objects.requireNonNull(src);

        final var bound = bindDestPosAndSrcPos(dest.length, src.length, destPos, srcPos, length);

        if (bound != null) {
            if (bound.length >= 0) {
                return _withInsertion(dest,
                        bound.destPos,
                        src,
                        bound.srcPos,
                        Math.min(
                                bound.length,
                                src.length - bound.srcPos),
                        srcReversed,
                        destReversed);

            } else {
                return _withInsertion(dest,
                        bound.destPos,
                        src,
                        bound.srcPos,
                        Math.max(
                                bound.length,
                                -bound.srcPos - 1),
                        srcReversed,
                        destReversed);
            }
        } else {
            return dest;
        }
    }

    private static Object[] _withInsertion(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length,
            boolean srcReversed,
            boolean destReversed) {
        final var result = new Object[dest.length + length];

        // copy preceding
        arrayCopy(dest, 0, result, 0, destPos, destReversed, false);

        // copy insertion
        if (length >= 0) {
            arrayCopy(src, srcPos, result, destPos, length, srcReversed, false);
        } else {
            arrayCopy(src, srcPos, result, destPos - length - 1, length, srcReversed, false);
        }

        // copy following
        final var followingStart = destPos + Math.abs(length);
        final var followingLength = result.length - followingStart;
        arrayCopy(dest, destPos, result, followingStart, followingLength, destReversed, false);

        return result;
    }

    //set
    public static Object[] withReplacement(Object[] dest, int destPost, Object[] src) {
        return withInsertion(dest, destPost, src, 0, src.length, false, false);

    }

    public static Object[] withReplacement(Object[] dest, int destPos, Object[] src, int srcPos) {
        return withInsertion(dest, destPos, src, srcPos, src.length - srcPos, false, false);
    }

    public static Object[] withReplacement(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length,
            boolean srcReversed,
            boolean destReversed) {
        Objects.requireNonNull(dest);
        Objects.requireNonNull(src);
        return _withReplacement(dest, destPos, src, srcPos, length, srcReversed, destReversed);
    }

    private static Object[] _withReplacement(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length,
            boolean srcReversed,
            boolean destReversed) {
        Objects.requireNonNull(dest);
        Objects.requireNonNull(src);

        final var result = new Object[dest.length];

        // copy preceding
        if (length >= 0) {
            arrayCopy(dest, 0, result, 0, destPos, destReversed, false);
        } else {
            arrayCopy(dest, 0, result, 0, destPos + length + 1);
        }

        // copy replacement
        arrayCopy(src, srcPos, result, destPos, length, srcReversed, false);

        // copy following
        if (length >= 0) {
            final var followingStart = destPos + length;
            final var followingLength = result.length - followingStart;
            arrayCopy(dest, followingStart, result, followingStart, followingLength, destReversed, false);
        } else {
            final var followingStart = destPos + 1;
            final var followingLength = result.length - followingStart;
            arrayCopy(dest, followingStart, result, followingStart, followingLength, destReversed, false);
        }

        return result;
    }

    // ======================================
    //  Copy Modification -- array -- single
    // ======================================
    //remove
    public static Object[] without(Object[] original, int index) {
        return without(original, index, false);
    }

    public static Object[] without(Object[] original, int index, boolean reversed) {
        Objects.requireNonNull(original);
        if (0 <= index && index < original.length) {
            return _without(original, index, reversed);
        } else {
            return original;
        }
    }

    private static Object[] _without(Object[] original, int index, boolean reversed) {
        final var result = new Object[original.length - 1];

        // copy preceding
        arrayCopy(original, 0, result, 0, index, reversed, false);
        // copy following
        final var followingStart = index + 1;
        final var followingLength = original.length - followingStart;
        arrayCopy(original, followingStart, result, index, followingLength, reversed, false);

        return result;
    }

    //get
    public static Object get(Object[] original, int index) {
        return get(original, index, false);
    }

    public static Object get(Object[] from, int index, boolean reversed) {
        Objects.requireNonNull(from);
        if (0 <= index && index < from.length) {
            return _get(from, index, reversed);
        } else {
            return null;
        }
    }

    private static Object _get(Object[] original, int index, boolean reversed) {
        return original[reversed ? reverseIndex(index, original.length) : index];
    }

    //insert
    public static Object[] withAddition(Object[] original, int at, Object addition) {
        return withAddition(original, at, addition, false);
    }

    public static Object[] withAddition(Object[] original, int at, Object addition, boolean reversed) {
        if (0 <= at && at <= original.length) {
            return _withAddition(original, at, addition, reversed);
        } else {
            return original;
        }
    }

    private static Object[] _withAddition(Object[] original, int at, Object addition, boolean reversed) {
        final var result = new Object[original.length + 1];

        // copy preceding
        arrayCopy(original, 0, result, 0, at, reversed, false);
        // copy addition
        result[at] = addition;
        // copy following
        arrayCopy(original, at, result, at + 1, original.length - at, reversed, false);

        return result;
    }

    //set
    public static Object[] withSwap(Object[] original, int at, Object replacement) {
        return withSwap(original, at, replacement, false);
    }

    public static Object[] withSwap(Object[] original, int at, Object replacement, boolean reversed) {
        Objects.requireNonNull(original);
        if (0 <= at && at < original.length) {
            return _withSwap(original, at, replacement, reversed);
        } else {
            return original;
        }
    }

    private static Object[] _withSwap(Object[] original, int at, Object replacement, boolean reversed) {
        final var result = new Object[original.length];

        // copy preceding
        arrayCopy(original, 0, result, 0, at, reversed, false);
        // copy replacement
        result[at] = replacement;
        // copy following
        arrayCopy(original, at + 1, result, at + 1, original.length - at - 1, reversed, false);

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
