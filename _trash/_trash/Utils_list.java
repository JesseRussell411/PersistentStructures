package _trash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

class Utils_list {
    private static Object[] reversed(Object[] array) {
        final var result = new Object[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[result.length - 1 - i] = array[i];
        }
        return result;
    }



    private static <T> boolean isSorted(Iterable<T> items, Comparator<T> comparator) {
        final var iter = items.iterator();

        if (!iter.hasNext()) return true;

        var prev = iter.next();

        while (iter.hasNext()) {
            if (comparator.compare(prev, prev = iter.next()) > 0) return false;
        }

        return true;
    }



    private static boolean isSorted(Object[] array, Comparator<Object> comparator) {
        for (int i = 1; i < array.length; ++i) {
            if (comparator.compare(array[i - 1], array[i]) > 0) return false;
        }
        return true;
    }


    private static Object[] merged(Iterable<Object> a, Iterable<Object> b, Comparator<Object> comparator) {
        final var enuA = new Enumerator<>(a.iterator());
        final var enuB = new Enumerator<>(b.iterator());
        final var result = new ArrayList<>();

        enuA.moveNext();
        enuB.moveNext();

        while (!enuA.complete() && !enuB.complete()) {
            if (comparator.compare(enuA.current(), enuB.current()) < 0) {
                result.add(enuA.current());
                enuA.moveNext();
            } else {
                result.add(enuB.current());
                enuB.moveNext();
            }
        }

        if (!enuA.complete()) {
            do {
                result.add(enuA.current());
            } while (enuA.moveNext());
        } else if (!enuB.complete()) {
            do {
                result.add(enuB.current());
            } while (enuB.moveNext());
        }

        return result.toArray();
    }


    private static Object[] withInsertion(Object[] original, int index, Object[] values) {
        if (values == null || values.length == 0) return original;
        indexCheck(index, original.length + 1);
        nullCheck(original);

        final var result = new Object[original.length + values.length];

        for (int i = 0; i < index; ++i) {
            result[i] = original[i];
        }

        for (int i = index; i < index + values.length; ++i) {
            result[i] = values[i - index];
        }

        for (int i = index + values.length; i < original.length + values.length; ++i) {
            result[i] = original[i - values.length];
        }

        return result;
    }

    private static Object[] withReplacement(Object[] original, int start, int end, Object[] src, int srcStart) {
        final var length = end - start;
        final var result = new Object[original.length];

        for (int i = 0; i < start; ++i) {
            result[i] = original[i];
        }

        boolean containedChange = false;
        for (int i = 0; i < length; ++i) {
            final var indexO = i + start;
            final var indexS = i + srcStart;
            final var newItem = src[indexS];

            if (!Objects.equals(original[indexO], newItem)) {
                containedChange = true;
            }

            result[indexO] = newItem;
        }

        for (int i = end; i < original.length; ++i) {
            result[i] = original[i];
        }

        if (containedChange) {
            return result;
        } else {
            return original;
        }
    }

    private static Object[] withoutRange(Object[] original, int start, int length) {
        if (length <= 0) return original;
        if (start == 0 && length == original.length) return new Object[0];
        indexCheck(start, original.length);
        if (start + length > original.length) throw new IndexOutOfBoundsException(start + length);

        final var result = new Object[original.length - length];

        for (int i = 0; i < start; ++i) {
            result[i] = original[i];
        }

        for (int i = start + length; i < original.length; ++i) {
            result[i - length] = original[i];
        }

        return result;
    }

    private static Object[][] partition(Object[] array, int size) {
        nullCheck(array);
        if (size < 1) throw new IllegalArgumentException("Size must be greater than 0.");

        if (size >= array.length) {
            return new Object[][]{array};
        }

        final var remainder = array.length % size;
        final var wholeCount = array.length / size;

        final var result = new Object[wholeCount + (remainder > 0 ? 1 : 0)][];

        for (int i = 0; i < wholeCount; ++i) {
            final var index = i * size;
            result[i] = Arrays.copyOfRange(array, index, index + size);
        }

        if (remainder > 0) {
            final var index = (wholeCount) * size;
            result[wholeCount] = Arrays.copyOfRange(array, index, index + remainder);
        }

        return result;
    }

    public static void indexCheck(int index, int length) {
        if (index < 0 || length <= index) throw new IndexOutOfBoundsException(index);
    }
}
