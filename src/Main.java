import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

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
        System.out.println(pl0to9.insert(3, pl1to1000000.subList(0, 3)));

        final var alTimes = new ArrayList<Double>();
        final var plTimes = new ArrayList<Double>();
        final var iterations = 10;
        final var count = 100_000;

        for (int j = 0; j < iterations; ++j) {
            System.out.println("iteration " + j + " / " + iterations + "...");
            final var alAddTest = new ArrayList<Integer>();
            var plAddTest = new PersistentList<Object>();

            final var alStartTime = System.currentTimeMillis();
            for (int i = 0; i < count; ++i) {
                alAddTest.add(i);
            }
            final var alEndTime = System.currentTimeMillis();
            alTimes.add((double) (alEndTime - alStartTime));

            final var plStartTime = System.currentTimeMillis();
            plAddTest = plAddTest.insert(0, alAddTest.toArray());
//            for (int i = 0; i < count; ++i) {
//                plAddTest = plAddTest.add(i);
//            }
            final var plEndTime = System.currentTimeMillis();
            plTimes.add((double) (plEndTime - plStartTime));

//            if (j == iterations - 1)
//                System.out.println(plAddTest);
        }


        System.out.println("p-list time:" + average(plTimes));
        System.out.println("a-list time:" + average(alTimes));

        final var randomList = new PersistentList<>(getRandomArray(20, 50));
        final var sortedList = randomList.sort((a, b) -> a - b);
        System.out.println(randomList);
        System.out.println(sortedList);

        for (int q = 0; q < 3; ++q) {
            final var big = 10;
            final var bigRandomArray = getRandomArray(big, 10);
            final var bigSortedArray = getSortedArray(big);
            final var bigAlmostSortedArray = getSortedArray(big);
            for (int i = 0; i < big / 10_000; ++i)
                bigAlmostSortedArray[rand.nextInt(big)] = i;

            bigAlmostSortedArray[rand.nextInt(big)] = 9;
            bigAlmostSortedArray[rand.nextInt(big)] = 9;
            bigAlmostSortedArray[rand.nextInt(big)] = 9;
            bigAlmostSortedArray[rand.nextInt(big)] = 9;
            bigAlmostSortedArray[rand.nextInt(big)] = 9;
            bigAlmostSortedArray[rand.nextInt(big)] = 9;
            bigAlmostSortedArray[rand.nextInt(big)] = 9;
            bigAlmostSortedArray[rand.nextInt(big)] = 9;

            final var bigRandomList = new PersistentList<>(bigRandomArray);
            final var bigSortedList = new PersistentList<>(bigSortedArray);
            final var bigAlmostSortedList = new PersistentList<>(bigAlmostSortedArray);

            var startTime = System.currentTimeMillis();
            bigRandomList.sort(Comparator.comparingInt(a -> a));
            var endTime = System.currentTimeMillis();

            System.out.println("PL Sorting Time: " + (endTime - startTime));

            final var randArr = Arrays.copyOf(bigRandomArray, bigRandomArray.length);
            startTime = System.currentTimeMillis();
            Arrays.sort(randArr);
            endTime = System.currentTimeMillis();
            System.out.println("AR Sorting Time: " + (endTime - startTime));


            startTime = System.currentTimeMillis();
            bigSortedList.sort(Comparator.comparingInt(a -> a));
            endTime = System.currentTimeMillis();
            System.out.println("PL already sorted time: " + (endTime - startTime));

            final var sortedArr = Arrays.copyOf(bigSortedArray, bigSortedArray.length);
            startTime = System.currentTimeMillis();
            Arrays.sort(sortedArr);
            endTime = System.currentTimeMillis();
            System.out.println("AR already sorted time: " + (endTime - startTime));

            startTime = System.currentTimeMillis();
            bigAlmostSortedList.sort(Comparator.comparingInt(a -> a));
            endTime = System.currentTimeMillis();
            System.out.println("PL almost already sorted time: " + (endTime - startTime));

            final var almostSortedArr = Arrays.copyOf(bigAlmostSortedArray, bigAlmostSortedArray.length);
            startTime = System.currentTimeMillis();
            Arrays.sort(almostSortedArr);
            endTime = System.currentTimeMillis();
            System.out.println("AR almost already sorted time: " + (endTime - startTime));
        }



        var testMap = new PersistentMap<String, Integer>();

        for(int i = 0; i < 100; ++i){
            testMap = testMap.put(String.valueOf(i), i);
        }

        for(int i = 0; i < 100; ++i){
            System.out.println(testMap.get(String.valueOf(i)));
        }





        System.out.println();

//        final var ms = new MyString().append("phone number: ").append(999999999).append('\n').append("age: ").append(45).append('\n');
//        System.out.println(ms);
//        System.out.println();
//        System.out.println(ms.items());
//
//        for(final var item : ms.items()){
//            System.out.println("" + item + ": " + item.getClass());
//        }
    }

    private static double average(Iterable<Double> numbers) {
        double result = 0.0;
        int count = 0;
        for (final var num : numbers) {
            ++count;
            result += num;
        }
        result /= count;
        return result;
    }

    private static Random rand = new Random();

    private static Integer[] getRandomArray(int size, Integer numSize) {
        final var result = new Integer[size];
        for (int i = 0; i < size; ++i)
            result[i] = numSize == null ? rand.nextInt() : rand.nextInt(numSize);
        return result;
    }

    private static Integer[] getSortedArray(int size) {
        final var result = new Integer[size];

        for (int i = 0; i < size; ++i) {
            result[i] = i;
        }

        return result;
    }
}
