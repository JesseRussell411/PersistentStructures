import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

class AllTheSame {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof AllTheSame;
    }

    @Override
    public int hashCode() {
        return 0;
    }

}

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

        System.out.println((88888 * 99999 * 99999 * 99999 * 99999) / 99999 / 99999 / 99999 / 99999);

        System.out.println(20);
        System.out.println(Integer.valueOf(20).hashCode());

        System.out.println("=============================================");


        final var range1to100 = new Integer[100];

        for (int i = 0; i < 100; ++i) {
            range1to100[i] = i + 1;
        }


        final var pl1to100 = new PersistentList<>(range1to100);
        System.out.println(pl1to100.size());
        System.out.println("--");
        for (int num : Arrays.copyOfRange(new Integer[]{0, 1, 2, 3}, 1, 2)) {
            System.out.println(num);
        }
        System.out.println("--");

        printIterable(pl1to100);

        final var modded1 = pl1to100.remove(9, 10).add(8, 111).insert(8, new Integer[]{-1, -2, -3, -4, -5, -6}).subList(1, 95).pull().pop().push(-100).put(200).remove(3, 5);
        final var modded2 = pl1to100.remove(9, 10).add(8, 111).insert(8, new Integer[]{-1, -2, -3, -4, -5, -6}).subList(1, 95).pull().pop().push(-100).put(200).remove(3, 5);
        printIterable(modded1);
        System.out.println(modded1.get(7));
        var modded = modded1.set(7, -69);
        System.out.println(modded.get(7));
        modded = modded.set(50, -89);
        System.out.println(modded.get(50));
        printIterable(modded);
        printIterable(pl1to100);
        System.out.println("true:  " + modded1.equals(modded2));
        System.out.println("true:  " + modded1.push(9).pull().equals(modded2));
        System.out.println("false: " + modded1.equals(modded2.set(42, 42)));


        final var shortList = new PersistentList<>(new Integer[]{0, 1, 2, 3, 4, 5});
        printIterable(shortList);
        System.out.println(shortList.size());
        System.out.println("===");

        final var range1to1000 = new Integer[1000];
        for (int i = 0; i < range1to1000.length; ++i) range1to1000[i] = i + 1;
        final var range1to1000000 = new Integer[1000000];
        for (int i = 0; i < range1to1000000.length; ++i) range1to1000000[i] = i + 1;
        final var pl1to1000 = new PersistentList<>(range1to1000);
        final var pl1to1000000 = new PersistentList<>(range1to1000000);

        printIterable(pl1to1000);
        printIterable(pl1to1000000.subList(1000, 1000));

        printIterable(pl1to100.subList(20, 50));

        System.out.println();


        final var range0to9 = new Integer[10];
        for (int i = 0; i < range0to9.length; ++i) range0to9[i] = i;
        final var pl0to9 = new PersistentList<>(range0to9);
        System.out.println("------");
        printIterable(pl0to9);
        System.out.println("--");

        printIterable(pl0to9.subList(0, 10));

        printIterable(pl0to9.subList(0, 8));
        printIterable(pl0to9.subList(1, 8));
        printIterable(pl0to9.subList(2, 8));

        printIterable(pl0to9.subList(0, 6));
        printIterable(pl0to9.subList(1, 6));
        printIterable(pl0to9.subList(2, 6));
        printIterable(pl0to9.subList(3, 6));
        printIterable(pl0to9.subList(4, 6));

        printIterable(pl0to9.subList(0, 4));
        printIterable(pl0to9.subList(1, 4));
        printIterable(pl0to9.subList(2, 4));
        printIterable(pl0to9.subList(3, 4));
        printIterable(pl0to9.subList(4, 4));
        printIterable(pl0to9.subList(5, 4));
        printIterable(pl0to9.subList(6, 4));

        printIterable(pl0to9.subList(0, 2));
        printIterable(pl0to9.subList(1, 2));
        printIterable(pl0to9.subList(2, 2));
        printIterable(pl0to9.subList(3, 2));
        printIterable(pl0to9.subList(4, 2));
        printIterable(pl0to9.subList(5, 2));
        printIterable(pl0to9.subList(6, 2));
        printIterable(pl0to9.subList(7, 2));
        printIterable(pl0to9.subList(8, 2));

        printIterable(pl1to1000000.subList(999, 1000));
        printIterable(pl1to100);
        printIterable(pl1to1000000.subList(0, 100));


        final var copypl1to100 = pl1to1000.subList(0, 100);
        System.out.println("true: " + pl1to100.equals(copypl1to100));
        System.out.println("true: " + pl1to100.equals(copypl1to100));

        final var pl1to102 = pl1to100.add(101).add(102);
        pl1to102.hashCode();

        System.out.println(pl1to1000.hashCode());
        System.out.println(pl1to1000000.subList(0, 1000).hashCode());
        System.out.println(pl1to100.hashCode());
        System.out.println(pl1to102.hashCode());
        System.out.println(pl1to1000000.subList(0, 120 - 1).add(-10000000).hashCode());
        System.out.println();

        System.out.println(1108378657 * -10000000 * 31 + 1108378657 + -10000000);


        final var ms = new MyString().append("phone number: ").append(999999999).append('\n').append("age: ").append(45).append('\n');
        System.out.println(ms);
        System.out.println();
        System.out.println(ms.items());

        for(final var item : ms.items()){
            System.out.println("" + item + ": " + item.getClass());
        }
    }
}
