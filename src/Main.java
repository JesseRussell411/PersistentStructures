import java.util.Arrays;

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

        System.out.println("=============================================");


        final var range1to100 = new Integer[100];

        for(int i = 0; i < 100; ++i){
            range1to100[i] = i + 1;
        }

        final var pl1to100 = new PersistentList<>(range1to100);
        System.out.println(pl1to100.size());
        System.out.println("--");
        for(int num : Arrays.copyOfRange(new Integer[]{0,1,2,3},1,2)){
            System.out.println(num);
        }
        System.out.println("--");

        printIterable(pl1to100);

        final var modded1 = pl1to100.remove(9, 10).add(8, 111).insert(8, new Integer[]{-1,-2,-3,-4,-5,-6}).subList(1, 95).pull().pop().push(-100).put(200).remove(3, 5);
        final var modded2 = pl1to100.remove(9, 10).add(8, 111).insert(8, new Integer[]{-1,-2,-3,-4,-5,-6}).subList(1, 95).pull().pop().push(-100).put(200).remove(3, 5);
        printIterable(modded1);
        System.out.println(modded1.get(7));
        var modded = modded1.set(7, -69);
        System.out.println(modded.get(7));
        modded = modded.set(50, -89);
        System.out.println(modded.get(50));
        printIterable(modded);
        printIterable(pl1to100);
        System.out.println(modded1.equals(modded2.set(42, 42)));

    }
}
