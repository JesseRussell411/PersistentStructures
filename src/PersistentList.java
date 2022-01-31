import java.util.*;

// TODO add node iterator
// TODO improve balancing algorithm (currently retro-fitting the balancing algorithm from an avl tree. It kinda works a little I think. For all I know it makes things worse).

public class PersistentList<T> implements Iterable<T> {
    private static final int LEAF_ITEM_LIMIT = 32;
    private static final Leaf EMPTY_LEAF = new Leaf(new Object[0]);
    private static final Object[] EMPTY_ARRAY = new Object[0];

    private final Node root;
    private final Map<PersistentList<?>, Boolean> equalityCache = Collections.synchronizedMap(new WeakHashMap<>());
//    private Integer hashCache = null;
//    private final Object hashCacheLock = new Object();

    private PersistentList(Node root) {
        nullCheck(root);
        this.root = root;
    }

    public PersistentList(T[] initialValue) {
        this(withInsertion(EMPTY_LEAF, 0, initialValue));
    }

    public PersistentList() {
        this(EMPTY_LEAF);
    }

    public String toString() {
        StringBuilder result = new StringBuilder("[ ");
        boolean first = true;

        for (final var item : this) {
            if (!first) {
                result.append(", ");
            }

            if (item != null) {
                result.append(item);
            }
            first = false;
        }
        result.append(" ]");

        return result.toString();
    }

    @Override
    public int hashCode() {
        return root.totalQuickHash();
//        if (hashCache != null) return hashCache;
//
//        synchronized (hashCacheLock) {
//            if (hashCache == null) {
//                hashCache = hashIterable(this);
//            }
//        }
//
//        return hashCache;
    }

    @Override
    public boolean equals(Object obj) {
        // If it's the same instance, it's obviously equal.
        if (this == obj) return true;
        if (obj instanceof PersistentList<?> other) {

            // try quick checks from fastest -> slowest
            if (root == other.root) return true;
            if (size() != other.size()) return false;
            if (size() == 0 && other.size() == 0) return true;
            // check the cache
            final var fromCache = checkEqualityFromCache(other);
            if (fromCache != null) return fromCache;
            // Check hashCodes
            // * HashCodes are cached, so these will be slow the first time but fast subsequent times.
//            if (root.totalQuickHash() != other.root.totalQuickHash()) return false;
            if (hashCode() != other.hashCode()) return false;

            // Quick checks failed, full check needed. Result will be cached for later.
            if (fullCheckValueEquality(root, other.root)) {
                // add to cache
                storeEqualityInCache(other, true);
                return true;
            } else {
                // add to cache
                storeEqualityInCache(other, false);
                return false;
            }
        } else {
            return false;
        }
    }

    private Boolean checkEqualityFromCache(PersistentList<?> other) {
        return equalityCache.get(other);
    }

    private void storeEqualityInCache(PersistentList<?> other, boolean equality) {
        equalityCache.put(other, equality);
    }

    public Iterator<T> iterator() {
        return new SelfIterator();
    }

    public int size() {
        return root.itemCount();
    }

    public T get(int index) {
        indexCheck(index, size());

        return (T) valueFrom(root, index);
    }

    public PersistentList<T> set(int index, T value) {
        indexCheck(index, size());

        return new PersistentList<>(withValueAtIndex(root, index, value));
    }

    public PersistentList<T> insert(int index, T[] values) {
        indexCheck(index, size());

        return new PersistentList<>(withInsertion(root, index, values));
    }

    public PersistentList<T> add(int index, T value) {
        return new PersistentList<>(withInsertion(root, index, new Object[]{value}));
    }

    public PersistentList<T> add(T value) {
        return add(size(), value);
    }

    public PersistentList<T> remove(int index, int length) {
        if (length <= 0) return this;
        indexCheck(index, size());
        final var end = index + length;
        if (end > size()) throw new IndexOutOfBoundsException(end);

        final var toPrune = new boolRef(false);
        final var result = withoutRange(root, index, end, toPrune);

        return new PersistentList<>(toPrune.value ? balanced(pruned(result)) : result);
    }

    public PersistentList<T> remove(int index) {
        return remove(index, 1);
    }

    public PersistentList<T> subList(int start, int length) {
        if (start == 0 && length == size()) return this;
        if (length <= 0) return new PersistentList<>(EMPTY_LEAF);
        indexCheck(start, size());
        final var end = start + length;
        if (end > size()) throw new IndexOutOfBoundsException(end);
        final var toPrune = new boolRef(false);
        final var result = subListFrom(root, start, end, toPrune);


        return new PersistentList<>(toPrune.value ? balanced(pruned(result)) : result);
    }

    public PersistentList<T> pull() {
        return remove(0);
    }

    public PersistentList<T> pop() {
        return remove(size() - 1);
    }

    public PersistentList<T> push(T value) {
        return add(0, value);
    }

    public PersistentList<T> put(T value) {
        return add(size(), value);
    }

    // ==================== Internal Modification ========================
    private static Node withInsertion(Node n, int index, Object[] values) {
        indexCheck(index, n.itemCount() + 1);
        if (values == null || values.length == 0) return n;

        final var toBalance = new boolRef(false);
        final var result = withInsertion(n, index, values, toBalance);
        return toBalance.value ? balanced(result) : result;
    }

    private static Object valueFrom(Node n, int index) {
        indexCheck(index, n.itemCount());

        if (n instanceof Structure s) {
            if (index < s.left.itemCount()) {
                return valueFrom(s.left, index);
            } else {
                return valueFrom(s.right, index - s.left.itemCount());
            }
        } else if (n instanceof Leaf l) {
            return l.items[index];
        } else {
            return null;
        }
    }

    private static Node withValueAtIndex(Node n, int index, Object value) {
        indexCheck(index, n.itemCount());

        if (n instanceof Structure s) {
            if (index < s.left.itemCount()) {
                return new Structure(withValueAtIndex(s.left, index, value), s.right);
            } else {
                return new Structure(s.left, withValueAtIndex(s.right, index - s.left.itemCount(), value));
            }
        } else if (n instanceof Leaf l) {
            final var items = Arrays.copyOf(l.items, l.items.length);
            items[index] = value;
            return new Leaf(items);
        } else {
            throw new NullPointerException();
        }
    }

    private static Node withInsertion(Node n, int index, Object[] values, boolRef toBalance) {
        if (n instanceof Structure s) {
            if (index < s.left.itemCount()) {
                return new Structure(withInsertion(s.left, index, values, toBalance), s.right);
            } else {
                return new Structure(s.left, withInsertion(s.right, index - s.left.itemCount(), values, toBalance));
            }
        } else if (n instanceof Leaf l) {
            final var items = withInsertion(l.items, index, values);
            if (items.length < LEAF_ITEM_LIMIT) {
                return new Leaf(items);
            } else {
                toBalance.value = true;
                final var partitions = partition(items, LEAF_ITEM_LIMIT);
                return fromPartitions(partitions, 0, partitions.length);
            }
        } else {
            throw new NullPointerException();
        }
    }

    private static Node withoutRange(Node n, int index, int end, boolRef toPrune) {
        if (n instanceof Structure s) {
            if (index < s.left.itemCount()) {
                if (end > s.left.itemCount()) {
                    return new Structure(
                            withoutRange(s.left, index, s.left.itemCount(), toPrune),
                            withoutRange(s.right, 0, end - s.left.itemCount(), toPrune));
                } else {
                    return new Structure(
                            withoutRange(s.left, index, end, toPrune),
                            s.right);
                }
            } else {
                return new Structure(
                        s.left,
                        withoutRange(s.right, index - s.left.itemCount(), end - s.left.itemCount(), toPrune));
            }
        } else if (n instanceof Leaf l) {
            final var items = withoutRange(l.items, index, end - index);

            if (items.length == 0) {
                toPrune.value = true;
                return EMPTY_LEAF;
            }

            return new Leaf(items);
        } else {
            throw new NullPointerException();
        }
    }

    private static Node subListFrom(Node n, int start, int end, boolRef toPrune) {
        if (n instanceof Structure s) {
            if (start < s.left.itemCount()) {
                if (end > s.left.itemCount()) {
                    return new Structure(
                            subListFrom(s.left, start, s.left.itemCount(), toPrune),
                            subListFrom(s.right, 0, end - s.left.itemCount(), toPrune));
                } else {
                    toPrune.value = true;
                    return new Structure(
                            subListFrom(s.left, start, end, toPrune),
                            EMPTY_LEAF);
                }
            } else {
                toPrune.value = true;
                return new Structure(
                        EMPTY_LEAF,
                        subListFrom(s.right, start - s.left.itemCount(), end - s.left.itemCount(), toPrune));
            }
        } else if (n instanceof Leaf l) {
            final var items = Arrays.copyOfRange(l.items, start, end);
            if (items.length == 0) {
                toPrune.value = true;
            }
            return new Leaf(items);
        } else {
            throw new NullPointerException();
        }
    }

    private static Node balanced(Node n) {
        // So hopefully this is better than nothing, but it is just the balance logic taken from an AVL tree where
        // you can only insert one item at a time. I don't know how well it works here.
        if (n instanceof Structure s) {
            final var leftBalanced = balanced(s.left);
            final var rightBalanced = balanced(s.right);
            final var forceCopy = leftBalanced != s.left || rightBalanced != s.right;
            final var result = forceCopy ? new Structure(leftBalanced, rightBalanced) : s;

            if (result.balanceFactor() < -1) {
                return rotatedRight(result);
            } else if (result.balanceFactor() > 1) {
                return rotatedLeft(result);
            } else {
                return result;
            }
        } else {
            return n;
        }
    }

    private static Node pruned(Node n) {
        if (n.itemCount() == 0) {
            return n;
        } else if (n instanceof Structure s) {
            if (s.left.itemCount() == 0) {
                return pruned(s.right);
            } else if (s.right.itemCount() == 0) {
                return pruned(s.left);
            } else {
                return s;
            }
        } else {
            return n;
        }
    }

    private static Node rotatedLeft(Node root) {
        if (root instanceof Structure s) {
            if (s.right instanceof Structure right_s) {
                return new Structure(new Structure(s.left, right_s.left), right_s.right);
            } else {
                return s;
            }
        } else {
            return root;
        }
    }

    private static Node rotatedRight(Node root) {
        if (root instanceof Structure s) {
            if (s.left instanceof Structure left_s) {
                return new Structure(left_s.left, new Structure(left_s.right, s.right));
            } else {
                return s;
            }
        } else {
            return root;
        }
    }

    private static boolean fullCheckValueEquality(Node a, Node b) {
        final var leafIteratorA = new LeafIterator(a);
        final var leafIteratorB = new LeafIterator(b);

        Leaf currentLeafA = null;
        Leaf currentLeafB = null;
        int offsetA = 0;
        int offsetB = 0;

        while (leafIteratorA.hasNext() && leafIteratorB.hasNext()) {
            currentLeafA = leafIteratorA.next();
            currentLeafB = leafIteratorB.next();
            if (currentLeafA != currentLeafB) {
                break;
            } else {
                offsetA += currentLeafA.items.length;
                offsetB += currentLeafB.items.length;
            }
        }

        final var itemIteratorA = new ItemIterator<>(a, leafIteratorA, currentLeafA, offsetA, offsetA);
        final var itemIteratorB = new ItemIterator<>(b, leafIteratorB, currentLeafB, offsetB, offsetB);

        while (itemIteratorA.hasNext() && itemIteratorB.hasNext()) {
            if (!areEqual(itemIteratorA.next(), itemIteratorB.next())) return false;
        }

        return !(itemIteratorA.hasNext() || itemIteratorB.hasNext());
    }

    // =================== Helpers ===================================
    public static void indexCheck(int index, int length) {
        if (index < 0 || length <= index) throw new IndexOutOfBoundsException(index);
    }

    public static void nullCheck(Object nullable) {
        if (nullable == null) throw new NullPointerException();
    }

    public static int hashCodeOf(Object o) {
        return o == null ? 0 : o.hashCode();
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
    public static int quickHash(Object a, Object b) {
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
    public static int quickHash(Object[] objects) {
        int result = 0;
        for (final var obj : objects) {
            result = quickHash(result, obj);
        }

        return result;
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

    private static Object[] withoutRange(Object[] original, int start, int length) {
        if (length <= 0) return original;
        if (start == 0 && length == original.length) return EMPTY_ARRAY;
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
        if (size < 1 || array == null || size >= array.length) {
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

    /**
     * Produces a balanced node tree from a list of arrays. The arrays become the items in the leafs.
     *
     * @param partitions The partitions array to draw from
     * @param index      index in the partitions array
     * @param count      the number of partitions
     */
    private static Node fromPartitions(Object[][] partitions, int index, int count) {
        if (count <= 0) {
            return EMPTY_LEAF;
        } else if (count == 1) {
            return new Leaf(partitions[index]);
        } else {
            final var remainder = count % 2;
            final var leftCount = (count / 2) + (remainder > 0 ? 1 : 0);
            final var rightCount = count / 2;

            return new Structure(
                    fromPartitions(partitions, index, leftCount),
                    fromPartitions(partitions, index + leftCount, rightCount));
        }
    }

    private static boolean areEqual(Object a, Object b) {
        if (a == null) {
            if (b == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return a.equals(b);
        }
    }

    // ======================== Inner Classes ===============================
    private static class boolRef {
        public boolean value;

        public boolRef(boolean value) {
            this.value = value;
        }
    }

    private static class intRef {
        public int value;

        public intRef(int value) {
            this.value = value;
        }
    }

    private interface Node {
        int leafCount();

        int itemCount();

        int totalQuickHash();

        int weight();

        int balanceFactor();
    }

    private static class Structure implements Node {
        public final Node left;
        public final Node right;
        public final int itemCount;
        public final int leafCount;
        private Integer totalQuickHashCache = null;
        private final Object totalQuickHashCacheLock = new Object();
        private final int weight;
        private final int balanceFactor;

        public Structure(Node left, Node right) {
            nullCheck(left);
            nullCheck(right);

            this.left = left;
            this.right = right;


            this.itemCount = left.itemCount() + right.itemCount();
            this.leafCount = left.leafCount() + right.leafCount();
            this.weight = left.weight() + right.weight();
            this.balanceFactor = right.weight() - left.weight();
        }

        public int leafCount() {
            return leafCount;
        }

        public int itemCount() {
            return itemCount;
        }

        public int totalQuickHash() {
            if (totalQuickHashCache != null) return totalQuickHashCache;

            synchronized (totalQuickHashCacheLock) {
                if (totalQuickHashCache == null) {
                    totalQuickHashCache = quickHash(left.totalQuickHash(), right.totalQuickHash());
                }
            }

            return totalQuickHashCache;
        }

        public int weight() {
            return weight;
        }

        public int balanceFactor() {
            return balanceFactor;
        }
    }

    private static class Leaf implements Node {
        public final Object[] items;
        private Integer totalQuickHashCache = null;
        private final Object totalQuickHashCacheLock = new Object();

        public Leaf(Object[] items) {
            this.items = items == null ? new Object[0] : items;
        }

        public int totalQuickHash() {
            if (totalQuickHashCache != null) return totalQuickHashCache;

            synchronized (totalQuickHashCacheLock) {
                if (totalQuickHashCache == null) {
                    totalQuickHashCache = quickHash(items);
                }
            }

            return totalQuickHashCache;
        }

        public int leafCount() {
            return 1;
        }

        public int itemCount() {
            return items.length;
        }

        public int weight() {
            return items.length > 0 ? 1 : 0;
        }

        public int balanceFactor() {
            return 0;
        }
    }

    public class SelfIterator implements Iterator<T> {
        private final ItemIterator<T> itemIterator = new ItemIterator<>(root);

        public boolean hasNext() {
            return itemIterator.hasNext();
        }

        public T next() {
            return itemIterator.next();
        }
    }

    private static class ItemIterator<T> implements Iterator<T> {
        final Node node;
        final LeafIterator leafIterator;
        Leaf currentLeaf = null;
        int index = 0;
        int offset = 0;

        private ItemIterator(Node node, LeafIterator leafIterator, Leaf currentLeaf, int index, int offset) {
            this.node = node;
            this.leafIterator = leafIterator;
            this.currentLeaf = currentLeaf;
            this.index = index;
            this.offset = offset;
        }

        public ItemIterator(Node node) {
            this.node = node;
            this.leafIterator = new LeafIterator(node);
        }

        public boolean hasNext() {
            return index < node.itemCount();
        }

        public T next() {
            if (index >= node.itemCount()) {
                return null;
            }

            if (currentLeaf == null) {
                initializeCurrentLeaf();
            }


            while (index >= offset + currentLeaf.items.length) {
                advanceToNextLeaf();
            }

            return (T) currentLeaf.items[(index++) - offset];
        }

        private void advanceToNextLeaf() {
            offset += currentLeaf.items.length;
            currentLeaf = leafIterator.next();

            if (currentLeaf == null) throw new IllegalStateException();
        }

        private void initializeCurrentLeaf() {
            currentLeaf = leafIterator.next();

            if (currentLeaf == null) throw new IllegalStateException();
        }
    }

    private static class LeafIterator implements Iterator<Leaf> {
        Stack<Node> location = null;
        int progress = 0;
        final Node node;

        public LeafIterator(Node node) {
            this.node = node;
        }

        public boolean hasNext() {
            return progress < node.leafCount();
        }


        public Leaf next() {
            if (!hasNext()) return null;

            ++progress;
            if (location == null) {
                initializeLocation();
                return currentLeaf();
            } else {
                advanceToNextLeaf();
                return currentLeaf();
            }
        }

        private Leaf currentLeaf() {
            if (location.peek() instanceof Leaf l) {
                return l;
            } else {
                throw new IllegalStateException();
            }
        }

        private void advanceToNextLeaf() {
            Node child = location.pop();
            Node parent = location.peek();

            while (child == ((Structure) parent).right) {
                child = parent;
                location.pop();
                parent = location.peek();
            }

            location.push(((Structure) parent).right);

            moveDownToLeaf();
        }


        private void moveDownToLeaf() {
            Node current = location.peek();

            while (current instanceof Structure s) {
                location.push(current = s.left);
            }
        }

        private void initializeLocation() {
            location = new Stack<>();
            location.push(node);
            moveDownToLeaf();
        }
    }
}
