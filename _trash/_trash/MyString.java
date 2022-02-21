package _trash;// TODO better name

public class MyString {
    private final PersistentList<Object> items;
    private PersistentList<Character> charListCache;
    private final Object charListCacheLock = new Object();

    private MyString(PersistentList<Object> items) {
        this.items = items;
    }

    public MyString(String value) {
        this.items = charItemListOf(value);
    }

    public MyString append(Object item){
        return new MyString(items.add(item));
    }

    public MyString concat(MyString other){
        return new MyString(items.concat(other.items));
    }

    public PersistentList<Character> charList() {
        if (charListCache != null) return charListCache;

        synchronized (charListCacheLock) {
            if (charListCache == null) {
                var result = new PersistentList<Character>();
                for (final var item : items) {
                    if (item instanceof MyString ms) {
                        result = result.concat(ms.charList());
                    } else if (item instanceof Character c) {
                        result = result.add(c);
                    } else {
                        for (final var c : String.valueOf(item).toCharArray()) {
                            result = result.add(c);
                        }
                    }
                }
                charListCache = result;
            }
        }

        return charListCache;
    }

    private static PersistentList<Object> charItemListOf(String s) {
        var result = new PersistentList<>();

        for (final var c : s.toCharArray()) {
            result = result.add(c);
        }

        return result;
    }

    private static String stringOf(MyString ms) {
        final var result = new StringBuilder();

        for (final var item : ms.items) {
            result.append(item);
        }

        return result.toString();
    }
}
//
//public class _trash.MyString {
//    private final _trash.PersistentList<Object> items;
//    private _trash.PersistentList<Character> charsCache = null;
//    private final Object charsCacheLock = new Object();
//    private String stringCache = null;
//    private final Object stringCacheLock = new Object();
//
//    public _trash.MyString(_trash.PersistentList<Object> items) {
//        this.items = items;
//    }
//    public _trash.MyString(){
//        this(new _trash.PersistentList<>());
//    }
//
//    public _trash.MyString(String s) {
//        if (s == null) s = "";
//
//        var items = new _trash.PersistentList<Object>();
//
//        for (char c : s.toCharArray()) {
//            items = items.add(c);
//        }
//
//        this.items = items;
//    }
//
//    public _trash.MyString valueOf(Object o) {
//        if (o instanceof String s) {
//            return new _trash.MyString(s);
//        } else {
//            return new _trash.MyString(new _trash.PersistentList<>(new Object[]{o}));
//        }
//    }
//
//    public _trash.PersistentList<Character> chars() {
//        if (charsCache != null) return charsCache;
//        synchronized (charsCacheLock) {
//            if (charsCache == null) {
//                var chars = new _trash.PersistentList<Character>();
//                for (final var item : items)
//                    for (final var c : String.valueOf(item).toCharArray()) {
//                        chars = chars.add(c);
//                    }
//
//                charsCache = chars;
//            }
//        }
//
//        return charsCache;
//    }
//
//    public _trash.PersistentList<Object> items() {
//        return items;
//    }
//
//    @Override
//    public String toString() {
//        if (stringCache != null) return stringCache;
//        synchronized (stringCacheLock) {
//            if (stringCache == null) {
//                final var result = new StringBuilder();
//
//                for (final var item : items) {
//                    result.append(item);
//                }
//
//                stringCache = result.toString();
//            }
//        }
//
//        return stringCache;
//    }
//
//
//    public _trash.MyString append(Object item) {
//        if (item instanceof String s)
//            return new _trash.MyString(items.add(new _trash.MyString(s)));
//        else
//            return new _trash.MyString(items.add(item));
//    }
//}
