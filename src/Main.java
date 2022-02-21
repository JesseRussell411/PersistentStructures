public class Main {
    public static void main(String[] args) {
        var arr = new ImmutableArray<Integer>();
        arr = arr.put(0);
        arr = arr.put(1);
        arr = arr.put(2);
        arr = arr.put(3);
        arr = arr.put(4);
        arr = arr.put(5);
        arr = arr.append(new Integer[]{6, 7, 8, 9});
        System.out.println("test");
        System.out.println(arr);
        arr = arr.put(14);
        System.out.println(arr);
        arr = arr.insert(arr.size() - 1, new Integer[]{13, 12, 11, 10, 34}, 1, 4, true);
        System.out.println(arr);
        arr = arr.insert(4, new Integer[]{-90, -88});
        System.out.println(arr);
        arr = arr.remove(4, 2);
        arr = arr.insert(5, new Integer[]{-9, -8, 24324}, 1, 20, true);
        System.out.println(arr);
        arr = arr.remove(7, -2);
        System.out.println(arr);
        System.out.println("==");
        arr = arr.insert(5, new Integer[]{34534, -9, -8}, 1, -20, true);
        System.out.println(arr);
        arr = arr.remove(7, -2);
        System.out.println(arr);

        arr = arr.add(4, -9);
        System.out.println(arr);
        arr = arr.remove(4);
        System.out.println(arr);
    }
}
