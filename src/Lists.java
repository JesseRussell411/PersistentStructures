import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

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

    public static Object[] sorted(Object[] src, Comparator<Object> comparator, boolean outReversed) {
        Objects.requireNonNull(src);

        return Arrays.stream(src).sorted(
                outReversed ? (a, b) -> comparator.compare(b, a) : comparator
        ).toArray();
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

//
//
//    private static int reverseIndex(int index, int length) {
//        return length - 1 - index;
//    }
//
//    private static int sign(int num) {
//        if (num < 0) {
//            return -1;
//        } else if (num > 0) {
//            return 1;
//        } else {
//            return 0;
//        }
//    }
//
//    public static void arrayCopy(Object[] src, int srcPos, Object[] dest, int destPos, int length) {
//        arrayCopy(src, srcPos, dest, destPos, length, false, false);
//    }
//
//    public static void arrayCopy(Object[] src, int srcPos, Object[] dest, int destPos, int length, boolean srcReversed, boolean destReversed) {
//        Objects.requireNonNull(src);
//        Objects.requireNonNull(dest);
//
//        final var bound = bindDestPosAndSrcPos(dest.length, src.length, destPos, srcPos, length);
//        if (bound == null) return;
//
//        if (length >= 0) {
//            final var destLengthOverflow = (bound.destPos + bound.length) - dest.length;
//            final var srcLengthOverflow = (bound.srcPos + bound.length) - src.length;
//            if (destLengthOverflow > 0 || srcLengthOverflow > 0) {
//                if (destLengthOverflow > srcLengthOverflow) {
//                    _arrayCopy(src, bound.srcPos, dest, bound.destPos, dest.length - bound.destPos, srcReversed, destReversed);
//                } else {
//                    _arrayCopy(src, bound.srcPos, dest, bound.destPos, src.length - bound.srcPos, srcReversed, destReversed);
//                }
//            } else {
//                _arrayCopy(src, bound.srcPos, dest, bound.destPos, bound.length, srcReversed, destReversed);
//            }
//        } else {
//            final var destLengthUnderflow = bound.destPos + length + 1;
//            final var srcLengthUnderflow = bound.srcPos + length + 1;
//            if (destLengthUnderflow < 0 || srcLengthUnderflow < 0) {
//                if (destLengthUnderflow < srcLengthUnderflow) {
//                    _arrayCopy(src, bound.srcPos, dest, bound.destPos, -(destPos + 1), srcReversed, destReversed);
//                } else {
//                    _arrayCopy(src, bound.srcPos, dest, bound.destPos, -(srcPos + 1), srcReversed, destReversed);
//                }
//            } else {
//                _arrayCopy(src, bound.srcPos, dest, bound.destPos, bound.length, srcReversed, destReversed);
//            }
//        }
//
//
//    }
//
//    public static void _arrayCopy(Object[] src, int srcPos, Object[] dest, int destPos, int length, boolean srcReversed, boolean destReversed) {
//
//        int s = srcReversed ? reverseIndex(srcPos, src.length) : srcPos;
//        int d = destReversed ? reverseIndex(destPos, dest.length) : destPos;
//        int direction = sign(length);
//
//        for (int l = 0; l < Math.abs(length); l++) {
//            dest[d] = src[s];
//            d += destReversed ? -direction : direction;
//            s += srcReversed ? -direction : direction;
//        }
//    }
//
//
//    public static int requireIndexInBounds(int index, int length) {
//        if (0 <= index || index < length) {
//            return index;
//        } else {
//            throw new IndexOutOfBoundsException(index);
//        }
//    }
//
//    public static int requirePositiveLength(int length) {
//        if (length >= 0) {
//            return length;
//        } else {
//            throw new IllegalArgumentException("length must be 0 or greater.");
//        }
//    }
//
//    public static class DestPos_SrcPos_Length {
//        public final int destPos;
//        public final int srcPos;
//        public final int length;
//
//        public DestPos_SrcPos_Length(int destPos, int srcPos, int length) {
//            this.destPos = destPos;
//            this.srcPos = srcPos;
//            this.length = length;
//        }
//    }
//
//    public static DestPos_SrcPos_Length bindDestPosAndSrcPos(int destLength, int srcLength, int destPos, int srcPos, int length) {
//        if (length >= 0) {
//            if (destPos < 0) {
//                srcPos -= destPos;
//                length += destPos;
//                destPos = 0;
//            }
//
//            if (srcPos < 0) {
//                destPos -= srcPos;
//                length += srcPos;
//                srcPos = 0;
//            }
//
//            if (length < 0) return null;
//            if (srcPos >= srcLength) return null;
//            if (destPos >= destLength) return null;
//        } else {
//            if (destPos >= destLength) {
//                srcPos -= destPos - (destLength - 1);
//                length += destPos - (destLength - 1);
//                destPos = destLength - 1;
//            }
//
//            if (srcPos >= srcLength) {
//                destPos -= srcPos - (srcLength - 1);
//                length += srcPos - (srcLength - 1);
//                srcPos = srcLength - 1;
//            }
//
//            if (length > 0) return null;
//            if (srcPos < 0) return null;
//            if (destPos < 0) return null;
//        }
//        return new DestPos_SrcPos_Length(destPos, srcPos, length);
//    }
//
//    public static class Start_Length {
//        final int start;
//        final int length;
//
//        public Start_Length(int start, int length) {
//            this.start = start;
//            this.length = length;
//        }
//    }
//
//    public static Start_Length bindStart_Length(int srcLength, int start, int length) {
//        if (length < 0) {
//            start += length;
//            length = Math.abs(length);
//        }
//
//        if (start > srcLength) return null;
//        if (start + length < 0) return null;
//
//        if (start < 0) {
//            length += start;
//            start = 0;
//        }
//
//        if (start + length > srcLength) {
//            length = srcLength - start;
//        }
//
//        return new Start_Length(start, length);
//    }
//
//    // =============================================
//    // Copy Modification -- array -- multi -- array
//    // =============================================
//    //remove
//    public static Object[] without(Object[] original, int start, int length, boolean reversed) {
//        Objects.requireNonNull(original);
//
//        final var bound = bindStart_Length(original.length, start, length);
//        if (bound != null) {
//            return _without(original, bound.start, bound.length, reversed);
//        } else {
//            return original;
//        }
//    }
//
//    private static Object[] _without(Object[] original, int start, int length, boolean reversed) {
//        final var result = new Object[original.length - length];
//
//
//        if (length > 0) {
//            // copy preceding
//            arrayCopy(original, 0, result, 0, start, reversed, false);
//            // copy following
//            final var followingStart = start + length;
//            final var followingLength = original.length - followingStart;
//            arrayCopy(original, followingStart, result, start, followingLength, reversed, false);
//
//        } else if (length < 0) {
//            // copy preceding
//            arrayCopy(original, 0, result, 0, start + length + 1, reversed, false);
//            // copy following
//            final var followingStart = start + 1;
//            final var followingLength = original.length - followingStart;
//            arrayCopy(original, followingStart, result, start, followingLength, reversed, false);
//
//        } else {
//            arrayCopy(original, 0, result, 0, original.length, reversed, false);
//        }
//
//        return result;
//    }
//
//    //get
//    public static Object[] get(Object[] from) {
//        return get(from, 0, from.length, false);
//    }
//
//    public static Object[] get(Object[] from, int start, int length, boolean reversed) {
//        Objects.requireNonNull(from);
//        final var bound = bindStart_Length(from.length, start, length);
//        if (bound != null) {
//            return _get(from, bound.start, bound.length, reversed);
//        } else {
//            return new Object[0];
//        }
//    }
//
//    private static Object[] _get(Object[] from, int start, int length, boolean reversed) {
//        final var result = new Object[length];
//        arrayCopy(from, start, result, 0, length, reversed, false);
//        return result;
//    }
//
//    //insert
//    public static Object[] withInsertion(Object[] dest, int destPost, Object[] src) {
//        return withInsertion(dest, destPost, src, 0, src.length, false, false);
//    }
//
//    public static Object[] withInsertion(Object[] dest, int destPost, Object[] src, int srcPos) {
//        return withInsertion(dest, destPost, src, srcPos, src.length - srcPos, false, false);
//    }
//
//    public static Object[] withInsertion(
//            Object[] dest,
//            int destPos,
//            Object[] src,
//            int srcPos,
//            int length,
//            boolean srcReversed,
//            boolean destReversed) {
//        Objects.requireNonNull(dest);
//        Objects.requireNonNull(src);
//
//        final var bound = bindDestPosAndSrcPos(dest.length + 1, src.length, destPos, srcPos, length);
//
//        if (bound != null) {
//            if (bound.length >= 0) {
//                return _withInsertion(dest,
//                        bound.destPos,
//                        src,
//                        bound.srcPos,
//                        Math.min(
//                                bound.length,
//                                src.length - bound.srcPos),
//                        srcReversed,
//                        destReversed);
//
//            } else {
//                return _withInsertion(dest,
//                        bound.destPos,
//                        src,
//                        bound.srcPos,
//                        Math.max(
//                                bound.length,
//                                -bound.srcPos - 1),
//                        srcReversed,
//                        destReversed);
//            }
//        } else {
//            return dest;
//        }
//    }
//
//    private static Object[] _withInsertion(
//            Object[] dest,
//            int destPos,
//            Object[] src,
//            int srcPos,
//            int length,
//            boolean srcReversed,
//            boolean destReversed) {
//        final var result = new Object[dest.length + Math.abs(length)];
//
//        // copy preceding
//        arrayCopy(dest, 0, result, 0, destPos, destReversed, false);
//
//        // copy insertion
//        if (length >= 0) {
//            arrayCopy(src, srcPos, result, destPos, length, srcReversed, false);
//        } else {
//            arrayCopy(src, srcPos, result, destPos - length - 1, length, srcReversed, false);
//        }
//
//        // copy following
//        final var followingStart = destPos + Math.abs(length);
//        final var followingLength = result.length - followingStart;
//        arrayCopy(dest, destPos, result, followingStart, followingLength, destReversed, false);
//
//        return result;
//    }
//
//    //set
//    public static Object[] withReplacement(Object[] dest, int destPost, Object[] src) {
//        return withInsertion(dest, destPost, src, 0, src.length, false, false);
//
//    }
//
//    public static Object[] withReplacement(Object[] dest, int destPos, Object[] src, int srcPos) {
//        return withInsertion(dest, destPos, src, srcPos, src.length - srcPos, false, false);
//    }
//
//    public static Object[] withReplacement(
//            Object[] dest,
//            int destPos,
//            Object[] src,
//            int srcPos,
//            int length,
//            boolean srcReversed,
//            boolean destReversed) {
//        Objects.requireNonNull(dest);
//        Objects.requireNonNull(src);
//        return _withReplacement(dest, destPos, src, srcPos, length, srcReversed, destReversed);
//    }
//
//    private static Object[] _withReplacement(
//            Object[] dest,
//            int destPos,
//            Object[] src,
//            int srcPos,
//            int length,
//            boolean srcReversed,
//            boolean destReversed) {
//        Objects.requireNonNull(dest);
//        Objects.requireNonNull(src);
//
//        final var result = new Object[dest.length];
//
//        // copy preceding
//        if (length >= 0) {
//            arrayCopy(dest, 0, result, 0, destPos, destReversed, false);
//        } else {
//            arrayCopy(dest, 0, result, 0, destPos + length + 1);
//        }
//
//        // copy replacement
//        arrayCopy(src, srcPos, result, destPos, length, srcReversed, false);
//
//        // copy following
//        if (length >= 0) {
//            final var followingStart = destPos + length;
//            final var followingLength = result.length - followingStart;
//            arrayCopy(dest, followingStart, result, followingStart, followingLength, destReversed, false);
//        } else {
//            final var followingStart = destPos + 1;
//            final var followingLength = result.length - followingStart;
//            arrayCopy(dest, followingStart, result, followingStart, followingLength, destReversed, false);
//        }
//
//        return result;
//    }
//
//    // ======================================
//    //  Copy Modification -- array -- single
//    // ======================================
//    //remove
//    public static Object[] without(Object[] original, int index) {
//        return without(original, index, false);
//    }
//
//    public static Object[] without(Object[] original, int index, boolean reversed) {
//        Objects.requireNonNull(original);
//        if (0 <= index && index < original.length) {
//            return _without(original, index, reversed);
//        } else {
//            return original;
//        }
//    }
//
//    private static Object[] _without(Object[] original, int index, boolean reversed) {
//        final var result = new Object[original.length - 1];
//
//        // copy preceding
//        arrayCopy(original, 0, result, 0, index, reversed, false);
//        // copy following
//        final var followingStart = index + 1;
//        final var followingLength = original.length - followingStart;
//        arrayCopy(original, followingStart, result, index, followingLength, reversed, false);
//
//        return result;
//    }
//
//    //get
//    public static Object get(Object[] original, int index) {
//        return get(original, index, false);
//    }
//
//    public static Object get(Object[] from, int index, boolean reversed) {
//        Objects.requireNonNull(from);
//        if (0 <= index && index < from.length) {
//            return _get(from, index, reversed);
//        } else {
//            return null;
//        }
//    }
//
//    private static Object _get(Object[] original, int index, boolean reversed) {
//        return original[reversed ? reverseIndex(index, original.length) : index];
//    }
//
//    //insert
//    public static Object[] withAddition(Object[] original, int at, Object addition) {
//        return withAddition(original, at, addition, false);
//    }
//
//    public static Object[] withAddition(Object[] original, int at, Object addition, boolean reversed) {
//        if (0 <= at && at <= original.length) {
//            return _withAddition(original, at, addition, reversed);
//        } else {
//            return original;
//        }
//    }
//
//    private static Object[] _withAddition(Object[] original, int at, Object addition, boolean reversed) {
//        final var result = new Object[original.length + 1];
//
//        // copy preceding
//        arrayCopy(original, 0, result, 0, at, reversed, false);
//        // copy addition
//        result[at] = addition;
//        // copy following
//        arrayCopy(original, at, result, at + 1, original.length - at, reversed, false);
//
//        return result;
//    }
//
//    //set
//    public static Object[] withSwap(Object[] original, int at, Object replacement) {
//        return withSwap(original, at, replacement, false);
//    }
//
//    public static Object[] withSwap(Object[] original, int at, Object replacement, boolean reversed) {
//        Objects.requireNonNull(original);
//        if (0 <= at && at < original.length) {
//            return _withSwap(original, at, replacement, reversed);
//        } else {
//            return original;
//        }
//    }
//
//    private static Object[] _withSwap(Object[] original, int at, Object replacement, boolean reversed) {
//        final var result = new Object[original.length];
//
//        // copy preceding
//        arrayCopy(original, 0, result, 0, at, reversed, false);
//        // copy replacement
//        result[at] = replacement;
//        // copy following
//        arrayCopy(original, at + 1, result, at + 1, original.length - at - 1, reversed, false);
//
//        return result;
//    }
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
