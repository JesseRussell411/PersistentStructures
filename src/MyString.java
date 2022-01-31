// TODO better name

public class MyString {
    private final PersistentList<Object> items;
    private PersistentList<Character> charsCache = null;
    private final Object charsCacheLock = new Object();
    private String stringCache = null;
    private final Object stringCacheLock = new Object();

    public MyString(PersistentList<Object> items) {
        this.items = items;
    }
    public MyString(){
        this(new PersistentList<>());
    }

    public MyString(String s) {
        if (s == null) s = "";

        var items = new PersistentList<Object>();

        for (char c : s.toCharArray()) {
            items = items.add(c);
        }

        this.items = items;
    }

    public MyString valueOf(Object o) {
        if (o instanceof String s) {
            return new MyString(s);
        } else {
            return new MyString(new PersistentList<>(new Object[]{o}));
        }
    }

    public PersistentList<Character> chars() {
        if (charsCache != null) return charsCache;
        synchronized (charsCacheLock) {
            if (charsCache == null) {
                var chars = new PersistentList<Character>();
                for (final var item : items)
                    for (final var c : String.valueOf(item).toCharArray()) {
                        chars = chars.add(c);
                    }

                charsCache = chars;
            }
        }

        return charsCache;
    }

    public PersistentList<Object> items() {
        return items;
    }

    @Override
    public String toString() {
        if (stringCache != null) return stringCache;
        synchronized (stringCacheLock) {
            if (stringCache == null) {
                final var result = new StringBuilder();

                for (final var item : items) {
                    result.append(item);
                }

                stringCache = result.toString();
            }
        }

        return stringCache;
    }


    public MyString append(Object item) {
        if (item instanceof String s)
            return new MyString(items.add(new MyString(s)));
        else
            return new MyString(items.add(item));
    }
}
