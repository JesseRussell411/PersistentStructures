import java.util.*;

/**
 * Functions for manipulating lists.
 */
class Lists {
    // ===================================
    // immutable single item manipulation
    // ===================================
    // get
    public static Object get(Object[] src, int srcPos) {
        return get(src, srcPos, false);
    }

    public static Object get(Object[] src, int srcPos, boolean srcReversed) {
        Objects.requireNonNull(src);
        if (0 <= srcPos && srcPos < src.length) {
            return src[srcReversed ? reverseIndex(srcPos, src.length) : srcPos];
        } else {
            throw new IndexOutOfBoundsException(srcPos);
        }
    }

    // replace
    public static Object[] swap(Object[] src, int srcPos, Object item) {
        return swap(src, srcPos, item, false, false);
    }

    public static Object[] swap(Object[] src, int srcPos, Object item, boolean srcReversed) {
        return swap(src, srcPos, item, srcReversed, false);
    }

    public static Object[] swap(Object[] src, int srcPos, Object item, boolean srcReversed, boolean outReversed) {
        Objects.requireNonNull(src);
        if (0 <= srcPos && srcPos < src.length) {
            final var result = new Object[src.length];

            // copy preceding
            arraycopy(src, 0, result, 0, srcPos, srcReversed, outReversed);
            // copy replacement
            result[outReversed ? reverseIndex(srcPos, result.length) : srcPos] = item;
            // copy proceeding
            arraycopy(src, srcPos + 1, result, srcPos + 1, result.length - (srcPos + 1), srcReversed, outReversed);

            return result;
        } else return src;
    }

    // insert
    public static Object[] add(Object[] src, int srcPos, Object item) {
        return add(src, srcPos, item, false, false);
    }

    public static Object[] add(Object[] src, int srcPos, Object item, boolean srcReversed) {
        return add(src, srcPos, item, srcReversed, false);
    }

    public static Object[] add(Object[] src, int srcPos, Object item, boolean srcReversed, boolean outReversed) {
        Objects.requireNonNull(src);
        if (0 <= srcPos && srcPos <= src.length) {
            final var result = new Object[src.length + 1];

            // copy preceding
            arraycopy(src, 0, result, 0, srcPos, srcReversed, outReversed);
            // copy insertion
            result[outReversed ? reverseIndex(srcPos, result.length) : srcPos] = item;
            // copy proceeding
            arraycopy(src, srcPos, result, srcPos + 1, result.length - (srcPos + 1), srcReversed, outReversed);

            return result;
        } else return src;
    }

    // ==================================
    // immutable multi item manipulation
    // ==================================
    // remove
    public static Object[] remove(Object[] src, int srcPos) {
        return remove(src, srcPos, 1, false, false);
    }

    public static Object[] remove(Object[] src, int srcPos, int length) {
        return remove(src, srcPos, length, false, false);
    }

    public static Object[] remove(Object[] src, int srcPos, int length, boolean srcReversed) {
        return remove(src, srcPos, length, srcReversed, false);
    }

    public static Object[] remove(Object[] src, int srcPos, int length, boolean srcReversed, boolean outReversed) {
        Objects.requireNonNull(src);
        final var bound = bindPos(0, srcPos, src.length, length);
        if (bound == null) return new Object[0];

        if (bound.length > 0) {
            if (bound.pos + bound.length > src.length) {
                length = src.length - bound.pos;
            }

            final var result = new Object[src.length - length];

            // copy preceding
            arraycopy(src, 0, result, 0, bound.pos, srcReversed, outReversed);
            // copy proceeding
            arraycopy(src, bound.pos + length, result, bound.pos, result.length - (bound.pos), srcReversed, outReversed);

            return result;
        } else if (bound.length < 0) {
            if (bound.pos + bound.length < -1) {
                length = -1 - bound.pos;
            }

            final var result = new Object[src.length - (-length)];

            // copy preceding
            arraycopy(src, 0, result, 0, bound.pos + length + 1, srcReversed, outReversed);
            // copy proceeding
            final var removalEnd = bound.pos + length;
            arraycopy(src, bound.pos + 1, result, removalEnd + 1, result.length - (removalEnd + 1), srcReversed, outReversed);

            return result;
        } else return new Object[0];
    }

    // get
    public static Object[] get(Object[] src, int srcPos, int length) {
        return get(src, srcPos, length, false, false);
    }

    public static Object[] get(Object[] src, int srcPos, int length, boolean srcReversed) {
        return get(src, srcPos, length, srcReversed, false);
    }

    public static Object[] get(Object[] src, int srcPos, int length, boolean srcReversed, boolean outReversed) {
        Objects.requireNonNull(src);
        final var bound = bindPos(0, srcPos, src.length, length);
        if (bound == null) return new Object[0];

        if (bound.length > 0) {
            if (bound.pos + bound.length > src.length) {
                length = src.length - bound.pos;
            }

            final var result = new Object[length];

            arraycopy(src, bound.pos, result, 0, length, srcReversed, outReversed);

            return result;
        } else if (bound.length < 0) {
            if (bound.pos + bound.length < -1) {
                length = -1 - bound.pos;
            }

            final var result = new Object[-length];

            arraycopy(src, bound.pos, result, result.length - 1, length, srcReversed, outReversed);

            return result;
        } else return new Object[0];
    }

    // replace
    public static Object[] replace(
            Object[] dest,
            int destPos,
            Object[] src) {
        Objects.requireNonNull(src);
        return replace(dest, destPos, src, 0, src.length, false, false, false);
    }

    public static Object[] replace(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos) {
        Objects.requireNonNull(src);
        return replace(dest, destPos, src, srcPos, src.length, false, false, false);
    }

    public static Object[] replace(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length) {
        return replace(dest, destPos, src, srcPos, length, false, false, false);
    }

    public static Object[] replace(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length,
            boolean destReversed) {
        return replace(dest, destPos, src, srcPos, length, destReversed, false, false);
    }

    public static Object[] replace(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length,
            boolean destReversed,
            boolean srcReversed) {
        return replace(dest, destPos, src, srcPos, length, destReversed, srcReversed, false);
    }

    public static Object[] replace(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length,
            boolean destReversed,
            boolean srcReversed,
            boolean outReversed) {
        Objects.requireNonNull(dest);
        Objects.requireNonNull(src);
        final var bound = bindSrcPosAndDestPos(0, srcPos, src.length, 0, destPos, dest.length, length);
        if (bound == null) return dest;
        if (bound.length == 0) return dest;

        final var result = new Object[dest.length];

        if (bound.length > 0) {
            if (bound.destPos + bound.length > dest.length) {
                length = dest.length - bound.destPos;
            }

            if (bound.srcPos + bound.length > src.length) {
                length = src.length - bound.srcPos;
            }

            // copy preceding
            arraycopy(dest, 0, result, 0, bound.destPos, destReversed, outReversed);
            // copy replacement
            arraycopy(src, bound.srcPos, result, bound.destPos, length, srcReversed, outReversed);
            // copy proceeding
            final var destEnd = bound.destPos + length;
            arraycopy(dest, destEnd, result, destEnd, result.length - destEnd, destReversed, outReversed);
        } else {
            if (bound.destPos + bound.length < -1) {
                length = -1 - bound.destPos;
            }

            if (bound.srcPos + bound.length < -1) {
                length = -1 - bound.srcPos;
            }

            // copy preceding
            final var destEnd = bound.destPos + length;
            arraycopy(dest, 0, result, 0, destEnd + 1, destReversed, outReversed);
            // copy replacement
            arraycopy(src, bound.srcPos, result, bound.destPos, length, srcReversed, outReversed);
            // copy proceeding
            arraycopy(dest, bound.destPos + 1, result, bound.destPos + 1, result.length - (bound.destPos + 1), destReversed, outReversed);
        }

        return result;
    }

    // insert
    public static Object[] insert(
            Object[] dest,
            int destPos,
            Object[] src) {
        Objects.requireNonNull(src);
        return insert(dest, destPos, src, 0, src.length, false, false, false);
    }

    public static Object[] insert(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos) {
        Objects.requireNonNull(src);
        return insert(dest, destPos, src, srcPos, src.length, false, false, false);
    }

    public static Object[] insert(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length) {
        return insert(dest, destPos, src, srcPos, length, false, false, false);
    }

    public static Object[] insert(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length,
            boolean destReversed) {
        return insert(dest, destPos, src, srcPos, length, destReversed, false, false);
    }

    public static Object[] insert(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length,
            boolean destReversed,
            boolean srcReversed) {
        return insert(dest, destPos, src, srcPos, length, destReversed, srcReversed, false);
    }

    public static Object[] insert(
            Object[] dest,
            int destPos,
            Object[] src,
            int srcPos,
            int length,
            boolean destReversed,
            boolean srcReversed,
            boolean outReversed) {
        Objects.requireNonNull(dest);
        Objects.requireNonNull(src);
        final var bound = bindSrcPosAndDestPos(0, srcPos, src.length, 0, destPos, dest.length + 1, length);
        if (bound == null) return dest;

        if (bound.length > 0) {
            if (bound.srcPos + bound.length > src.length) {
                length = src.length - bound.srcPos;
            }

            final var result = new Object[dest.length + length];
            // copy preceding
            arraycopy(dest, 0, result, 0, bound.destPos, destReversed, outReversed);
            // copy insertion
            arraycopy(src, bound.srcPos, result, bound.destPos, length, srcReversed, outReversed);
            // copy proceeding
            final var outEnd = bound.destPos + length;
            arraycopy(dest, bound.destPos, result, outEnd, result.length - outEnd, destReversed, outReversed);

            return result;
        } else if (bound.length < 0) {
            if (bound.srcPos + bound.length < -1) {
                length = -1 - bound.srcPos;
            }

            final var result = new Object[dest.length + length];

            // copy preceding
            arraycopy(dest, 0, result, 0, bound.destPos, destReversed, outReversed);
            // copy insertion
            arraycopy(src, bound.srcPos, result, bound.destPos + (-length) - 1, length, srcReversed, outReversed);
            // copy proceeding
            final var outEnd = bound.destPos + (-length);
            arraycopy(dest, bound.destPos, result, outEnd, result.length - outEnd, destReversed, outReversed);

            return result;
        } else return dest;
    }


    // ===========================
    // mutable multi manipulation
    // ===========================
    public static void arraycopy(Object[] src, int srcPos, Object[] dest, int destPos, int length,
                                 boolean srcReversed, boolean destReversed) {
        Objects.requireNonNull(src);
        Objects.requireNonNull(dest);
        final var bound = bindSrcPosAndDestPos(0, srcPos, src.length, 0, destPos, dest.length, length);
        if (bound == null) return;

        if (bound.length > 0) {
            final var destLengthOverflow = Math.max(0, (bound.destPos + bound.length) - dest.length);
            final var srcLengthOverflow = Math.max(0, (bound.srcPos + bound.length) - src.length);
            _arraycopy(
                    src,
                    bound.srcPos,
                    dest,
                    bound.destPos,
                    bound.length - Math.max(
                            destLengthOverflow,
                            srcLengthOverflow),
                    srcReversed,
                    destReversed);
        } else if (bound.length < 0) {
            final var destLengthUnderflow = Math.min(0, bound.destPos + length + 1);
            final var srcLengthUnderflow = Math.min(0, bound.srcPos + length + 1);
            _arraycopy(
                    src,
                    bound.srcPos,
                    dest,
                    bound.destPos,
                    bound.length - Math.min(
                            destLengthUnderflow,
                            srcLengthUnderflow),
                    srcReversed,
                    destReversed);
        }
    }

    private static void _arraycopy(Object[] src, int srcPos, Object[] dest, int destPos, int length,
                                   boolean srcReversed, boolean destReversed) {
        int s = srcReversed ? reverseIndex(srcPos, src.length) : srcPos;
        int d = destReversed ? reverseIndex(destPos, dest.length) : destPos;
        int direction = sign(length);
        for (int l = 0; l < Math.abs(length); l++) {
            dest[d] = src[s];
            d += destReversed ? -direction : direction;
            s += srcReversed ? -direction : direction;
        }
    }

    // ====== misc ======
    public static Object[][] partition(Object[] src, int size, boolean srcReversed, boolean reversePartitions, boolean outReversed) {
        Objects.requireNonNull(src);
        if (size <= 0) throw new IllegalArgumentException("size must be at least 1.");
        if (size >= src.length) return new Object[][]{src};

        final var remainder = src.length % size;
        final var count = (src.length / size) + (remainder > 0 ? 1 : 0);
        final var result = new Object[count][];

        // copy whole partitions
        for (int i = 0; i < count - 1; i++) {
            int index = outReversed ? reverseIndex(i, result.length) : i;
            result[index] = get(src, i * size, size, srcReversed, reversePartitions);
        }

        // copy remainder
        if (remainder > 0) {
            result[outReversed ? 0 : count - 1] = get(src, (count - 1) * size, remainder, srcReversed, reversePartitions);
        }

        return result;
    }

    public static Object[] copy(Object[] src, boolean srcReversed, boolean outReversed) {
        Objects.requireNonNull(src);
        return get(src, 0, src.length, srcReversed, outReversed);
    }

    public static Object[] sorted(Object[] src, Comparator<Object> comparator) {
        return sorted(src, comparator, false);
    }

    public static Object[] sorted(Object[] src, Comparator<Object> comparator, boolean outReversed) {
        Objects.requireNonNull(src);

        return Arrays.stream(src).sorted(
                outReversed ? (a, b) -> comparator.compare(b, a) : comparator
        ).toArray();
    }

    public static class MergedIterator<T> implements Iterator<T> {
        public MergedIterator(Iterator<T> a, Iterator<T> b, Comparator<T> comparator) {
            Objects.requireNonNull(a);
            Objects.requireNonNull(b);
            Objects.requireNonNull(comparator);

            this.a = new Enumerator<>(a);
            this.b = new Enumerator<>(b);
            this.comparator = comparator;
        }

        private final Enumerator<T> a, b;
        private final Comparator<T> comparator;

        public boolean hasNext() {
            if (a.unStarted()) a.moveNext();
            if (b.unStarted()) b.moveNext();
            return !(a.complete() && b.complete());
        }

        public T next() {
            if (a.unStarted()) a.moveNext();
            if (b.unStarted()) b.moveNext();

            if (a.complete() && b.complete()) {
                return null;
            } else if (a.complete()) {
                final var result = b.current();
                b.moveNext();
                return result;
            } else if (b.complete()) {
                final var result = a.current();
                a.moveNext();
                return result;
            } else if (comparator.compare(a.current(), b.current()) < 0) {
                final var result = a.current();
                a.moveNext();
                return result;
            } else {
                final var result = b.current();
                b.moveNext();
                return result;
            }
        }
    }

    public static class MergedIterable<T> implements Iterable<T> {
        private final Iterable<T> a, b;
        private final Comparator<T> comparator;

        public MergedIterable(Iterable<T> a, Iterable<T> b, Comparator<T> comparator) {
            Objects.requireNonNull(a);
            Objects.requireNonNull(b);
            Objects.requireNonNull(comparator);

            this.a = a;
            this.b = b;
            this.comparator = comparator;
        }

        public Iterator<T> iterator() {
            return new MergedIterator<>(a.iterator(), b.iterator(), comparator);
        }
    }

    public static <T> Iterable<T> merge(Iterable<T> a, Iterable<T> b, Comparator<T> comparator) {
        return new MergedIterable<>(a, b, comparator);
    }


    // ===================
    // index manipulation
    // ===================
    public record Pos_Length(int pos, int length) {
    }

    public static Pos_Length bindPos(int lower, int pos, int upper, int length) {
        if (upper <= lower) return null;

        if (length > 0) {
            if (pos + length - 1 < lower) return null;
            if (pos > upper - 1) return null;
        } else if (length < 0) {
            if (pos + length + 1 > upper - 1) return null;
            if (pos < lower) return null;
        } else {
            if (pos > upper - 1) return null;
            if (pos < lower) return null;
        }

        final var result = bindPos(lower, pos, upper);

        if (result == null) return null;

        return new Pos_Length(
                result,
                length + (result - pos));

    }

    public static Integer bindPos(int lower, int pos, int upper) {
        if (upper <= lower) return null;

        return Math.max(
                lower,
                Math.min(
                        pos,
                        upper - 1));
    }

    public record SrcPos_DestPos(int srcPos, int destPos) {
    }

    public static SrcPos_DestPos bindSrcPosAndDestPos(int srcLower, int srcPos, int srcUpper, int destLower,
                                                      int destPos, int destUpper) {
        final var srcResult = bindPos(srcLower, srcPos, srcUpper);
        if (srcResult == null) return null;
        final var destResult = bindPos(destLower, destPos, destUpper);
        if (destResult == null) return null;

        return new SrcPos_DestPos(srcResult, destResult);
    }

    public record SrcPos_DestPos_Length(int srcPos, int destPos, int length) {
    }

    public static SrcPos_DestPos_Length bindSrcPosAndDestPos(int srcLower, int srcPos, int srcUpper,
                                                             int destLower, int destPos, int destUpper, int length) {
        if (length > 0) {
            if (destPos + length - 1 < destLower) return null;
            if (srcPos + length - 1 < srcLower) return null;
            if (destPos > destUpper - 1) return null;
            if (srcPos > srcUpper - 1) return null;

            if (destPos < destLower) {
                srcPos += destLower - destPos;
                length -= destLower - destPos;
                destPos = destLower;
            }

            if (srcPos > srcUpper - 1) return null;

            if (srcPos < srcLower) {
                destPos += srcLower - srcPos;
                length -= srcLower - srcPos;
                srcPos = srcLower;
            }

            if (destPos > destUpper - 1) return null;

        } else if (length < 0) {
            if (destPos + length + 1 > destUpper - 1) return null;
            if (srcPos + length + 1 > srcUpper - 1) return null;
            if (destPos < destLower) return null;
            if (srcPos < srcLower) return null;

            if (destPos > destUpper - 1) {
                srcPos += (destUpper - 1) - destPos;
                length -= (destUpper - 1) - destPos;
                destPos = destUpper - 1;
            }

            if (srcPos < srcLower) return null;

            if (srcPos > srcUpper - 1) {
                destPos += (srcUpper - 1) - srcPos;
                length -= (srcUpper - 1) - srcPos;
                srcPos = srcUpper - 1;
            }

            if (destPos < destLower) return null;

        } else {
            if (destPos > destUpper - 1) return null;
            if (destPos < destLower) return null;
            if (srcPos > srcUpper - 1) return null;
            if (srcPos < srcLower) return null;
        }

        return new SrcPos_DestPos_Length(srcPos, destPos, length);
    }

    public static int reverseIndex(int index, int length) {
        return length - 1 - index;
    }

    public static int sign(int num) {
        if (num < 0) {
            return -1;
        } else if (num > 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public static boolean boundsCheck(int index, int length) {
        return boundsCheck(0, index, length);
    }

    public static boolean boundsCheck(int lower, int index, int upper) {
        return lower <= index && index < upper;
    }

    public static int requireIndexInBounds(int index, int length) {
        return requireIndexInBounds(0, index, length);
    }

    public static int requireIndexInBounds(int lower, int index, int upper) {
        if (!boundsCheck(lower, index, upper)) {
            throw new IndexOutOfBoundsException(index);
        } else {
            return index;
        }
    }
}
