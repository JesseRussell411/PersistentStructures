import java.util.Objects;

class Utils_objects {
    public static void nullCheck(Object nullable) {
        if (nullable == null) throw new NullPointerException();
    }

    public static int hashIterable(Iterable<?> objects) {
        int result = 0;

        for (final var obj : objects) {
            result = Objects.hash(result, obj);
        }

        return result;
    }

    /**
     * Generates a hash that isn't well distributed but at least doesn't care about order.
     */
    public static int nonOrderedHash(Object a, Object b) {
//        return hashCodeOf(a) + hashCodeOf(b);
        return hashCodeOf(a) ^ hashCodeOf(b);
//        return 31 * hashCodeOf(a) + 31 * hashCodeOf(b);
//        final var hashA = hashCodeOf(a);
//        final var hashB = hashCodeOf(b);
//        return hashA * hashB * 31 + hashA + hashB; // breaks with lots of values
    }

    /**
     * Generates a hash that isn't well distributes but at least doesn't care about order.
     */
    public static int nonOrderedHash(Object...objects) {
        int result = 0;
        for (final var obj : objects) {
            result = nonOrderedHash(result, obj);
        }

        return result;
    }
}
