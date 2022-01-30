public class Main {
    public static String stringFromIterable(Iterable<?> it) {
        final var result = new StringBuilder();
        boolean first = true;
        result.append("[ ");

        for (Object o : it) {
            if (!first) {

                result.append(", ");
            }
            result.append(o);

            first = false;
        }

        result.append(" ]");
        return result.toString();
    }

    public static void printIterable(Iterable<?> it) {
        System.out.println(stringFromIterable(it));
    }

    public static void main(String[] args) {
        final var stack = new PersistentStack<String>(new String[]{
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "pineapples",
                "apples",
                "peanuts"
        });

        printIterable(stack);



final var theRestOfTheAlphabet = new String[]
        {
                "h",
                "i",
                "j",
                "k",
                "l",
                "m",
                "n",
                "o",
                "p",
                "q",
                "r",
                "s",
                "t",
                "u",
                "v",
                "w",
                "x",
                "y",
                "z"
        };
        final var alphabetStack = stack.pop(3).putMany(theRestOfTheAlphabet);



        final var anotherStack = stack.pop(3);
        final var alsoAnotherStack = alphabetStack.pop(theRestOfTheAlphabet.length);

        printIterable(anotherStack);
        printIterable(alsoAnotherStack);

        System.out.println(anotherStack.hashCode() + " === " + alsoAnotherStack.hashCode());



        final var s1 = new PersistentStack<String>().put("-1").put("a").put("b");
        final var s2 = s1.pop(2).putMany(new String[]{"a", "b"});
        printIterable(s1);
        printIterable(s2);
        System.out.println(s1.hashCode() + " === " + s2.hashCode());

        System.out.println((88888*99999*99999*99999*99999)/99999/99999/99999/99999);

        System.out.println(20);
        System.out.println(Integer.valueOf(20).hashCode());
    }
}
