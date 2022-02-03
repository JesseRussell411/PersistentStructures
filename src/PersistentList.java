import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.StreamSupport;

// TODO node iterator
// TODO default Comparator
// TODO default sort
// TODO default isSorted status with cache
// FOR NEXT TIME build in clever reverse mechanism


public class PersistentList<T> implements Iterable<T> {
    private static final int LEAF_SIZE = 32;
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private static final Leaf EMPTY_LEAF = new Leaf(EMPTY_ARRAY);

    private Node root;

    /**
     * stores the root nodes of known equal or non-equal PersistentLists. It stores the root nodes because the equality of Node is identity based, not value based.
     */
    private final Map<Node, Boolean> equalityCache = Collections.synchronizedMap(new WeakHashMap<>());
    private volatile WeakReference<Node> reverseCache = new WeakReference<>(null);
    private final Object reverseCacheLock = new Object();

    private volatile Integer hashCache = null;
    private final Object hashCacheLock = new Object();

    private PersistentList(Node root) {
        nullCheck(root);
        this.root = root;
    }

    public PersistentList(T[] initialValue) {
        this(fromArray(initialValue));
    }

    public PersistentList(List<T> initialValue) {
        this(fromArray(initialValue.toArray()));
    }

    public PersistentList(Iterable<T> initialValue) {
        this(fromArray(StreamSupport.stream(initialValue.spliterator(), false).toArray()));
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
        if (hashCache != null) return hashCache;

        synchronized (hashCacheLock) {
            if (hashCache == null) {
                var hash = 0;

                for (final var item : this) {
                    hash = Objects.hash(hash, item == null ? 0 : item);
                }

                hashCache = hash;
            }
        }

        return hashCache;
    }

    @Override
    public boolean equals(Object obj) {
        // check if the object is the right type
        if (obj instanceof PersistentList<?> other) {
            // try quick checks
            if (this == other) return true; // same instance
            if (root == other.root) return true;
            if (size() != other.size()) return false;
            if (size() == 0 && other.size() == 0) {
                consolidateInnards(other);
                return true;
            }

            // check the cache
            final var fromCache = checkEqualityFromCache(other);
            if (fromCache != null) {
                if (fromCache) {
                    consolidateInnards(other);
                    return true;
                } else {
                    return false;
                }
            }

            // Check hashCodes
            if (root.quickHash() != other.root.quickHash()) return false;
            if (hashCode() != other.hashCode()) return false;

            // Quick checks failed, full check needed. Result will be cached for later.
            if (nodesAreEqual(root, other.root)) {
                // add to cache
                storeEqualityInCache(other, true);
                consolidateInnards(other);

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
        final var fromLocal = equalityCache.get(other.root);
        final var fromOther = other.equalityCache.get(root);

        if (fromLocal == null) {
            return fromOther;
        } else {
            return fromLocal;
        }
    }

    private void storeEqualityInCache(PersistentList<?> other, boolean equality) {
        equalityCache.put(other.root, equality);
    }

    /**
     * ~ (!!! should only be called if a.equals(b) !!!) ~
     */
    private void consolidateInnards(PersistentList<?> other) {
        assert areEqual(this, other);
        if (root != other.root) {
            if (root instanceof Branch ab && ab.quickHashCache != null) {
                other.root = root;
            } else {
                root = other.root;
            }
        }

        if (hashCache == null) {
            if (other.hashCache != null) {
                hashCache = other.hashCache;
            }
        } else if (other.hashCache == null) {
            other.hashCache = hashCache;
        }

        // TODO fix cache loss problem, you know what I'm talking about, assuming you are me, if not, you problem don't know what I'm talking about.
        if (reverseCache.get() == null) {
            if (reverseCache.get() != null) {
                reverseCache = other.reverseCache;
            }
        } else if (other.reverseCache.get() == null) {
            other.reverseCache = reverseCache;
        }
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

    public T getFromEnd(int index) {
        return get(size() - 1 - index);
    }

    public PersistentList<T> set(int index, T value) {
        indexCheck(index, size());

        return new PersistentList<>(withReplacement(root, index, index + 1, new Object[]{value}, 0));
    }

    public PersistentList<T> setAtEnd(int index, T value) {
        return set(size() - 1 - index, value);
    }

    public PersistentList<T> insert(int index, T[] values) {
        indexCheck(index, size() + 1);
        if (values.length == 0) return this;

        return new PersistentList<>(withInsertion(root, index, values));
    }

    public PersistentList<T> insert(int index, PersistentList<T> values) {
        indexCheck(index, size() + 1);
        if (values.size() == 0) return this;

        return new PersistentList<>(withInsertion(root, index, values.root));
    }

    public PersistentList<T> insertAtEnd(int index, T[] values) {
        return insert(size() - values.length - index, values);
    }

    public PersistentList<T> insertAtEnd(int index, PersistentList<T> values) {
        return insert(size() - values.size() - index, values);
    }

    public PersistentList<T> add(int index, T value) {
        indexCheck(index, size() + 1);

        return new PersistentList<>(withInsertion(root, index, new Object[]{value}));
    }

    public PersistentList<T> addAtEnd(int index, T value) {
        return add(size() - 1 - index, value);
    }

    public PersistentList<T> add(T value) {
        return add(size(), value);
    }

    public PersistentList<T> concat(T[] values) {
        return insert(size(), values);
    }

    public PersistentList<T> concat(PersistentList<T> values) {
        return insert(size(), values);
    }

    public PersistentList<T> remove(int index, int length) {
        if (length <= 0) return this;

        indexCheck(index, size());

        final var end = index + length;
        if (end > size()) throw new IndexOutOfBoundsException(end);

        return new PersistentList<>(withoutRange(root, index, end));
    }

    public PersistentList<T> remove(int index) {
        return remove(index, 1);
    }

    public PersistentList<T> removeFromEnd(int index, int length) {
        return remove(size() - length - index, length);
    }

    public PersistentList<T> removeFromEnd(int index) {
        return remove(size() - 1 - index);
    }

    public PersistentList<T> subList(int start, int length) {
        if (length <= 0) return new PersistentList<>(EMPTY_LEAF);
        if (start == 0 && length == size()) return this;

        indexCheck(start, size());

        final var end = start + length;
        if (end > size()) throw new IndexOutOfBoundsException(end);

        return new PersistentList<>(subListFrom(root, start, end));
    }

    public PersistentList<T> subListFromEnd(int start, int length) {
        return subList(size() - length - start, length);
    }

    public PersistentList<T> pull() {
        if (size() == 0) return this;
        return remove(0);
    }

    public PersistentList<T> pop() {
        if (size() == 0) return this;
        return remove(size() - 1);
    }

    public PersistentList<T> push(T value) {
        return add(0, value);
    }

    public PersistentList<T> put(T value) {
        return add(value);
    }

    public T start() {
        return get(0);
    }

    public T end() {
        return get(size() - 1);
    }


    public PersistentList<T> repeat(int times) {
        final int BREAK_FACTOR = 3;
        if (times == 1) return this;
        if (times <= 0) return new PersistentList<>(EMPTY_LEAF);

        if (times <= BREAK_FACTOR) {
            var result = this;
            for (int i = 1; i < times; ++i) {
                result = result.concat(this);
            }
            return result;
        }

        final var factor = times / BREAK_FACTOR;
        final var remainder = times % BREAK_FACTOR;

        final var repeatedByFactor = this.repeat(factor);

        if (remainder == 0) {
            return repeatedByFactor.repeat(BREAK_FACTOR);
        } else {
            return repeatedByFactor.repeat(BREAK_FACTOR).concat(this.repeat(remainder));
        }
    }

    public PersistentList<T> sort(Comparator<T> comparator) {
        return new PersistentList<>(sorted(
                root,
                (a, b) -> comparator.compare((T) a, (T) b)));
    }

    public boolean isSorted(Comparator<T> comparator) {
        return isSorted(this, comparator);
    }

    public PersistentList<T> reverse() {
        if (size() <= 1) return this;
        var cached = reverseCache.get();
        if (cached != null) return new PersistentList<>(cached);

        synchronized (reverseCacheLock) {
            cached = reverseCache.get();
            if (cached == null) {
                cached = fromArray(reversed(toArray()));
                reverseCache = new WeakReference<>(cached);
            }
        }

        final var result = new PersistentList<T>(cached);
        result.reverseCache = new WeakReference<>(root);

        return result;
    }

    public List<T> toList() {
        final var result = new ArrayList<T>(size());

        for (final var item : this) {
            result.add(item);
        }

        return result;
    }

    public Object[] toArray() {
        final var result = new Object[size()];

        int index = 0;
        for (final var item : this) {
            result[index++] = item;
        }

        return result;
    }

    public Integer indexOf(T item) {
        // TODO maybe add a cache
        int index = 0;
        for (final var localItem : this) {
            if (areEqual(this, localItem)) return index;
            ++index;
        }
        return null;
    }

    public boolean contains(T item) {
        // TODO maybe add a cache
        for (final var localItem : this) {
            if (areEqual(this, localItem)) return true;
        }
        return false;

    }


    // ==================== Internal Modification ========================
    private static Object valueFrom(Node n, int index) {
        if (n instanceof Branch b) {
            if (index < b.left.itemCount()) {
                return valueFrom(b.left, index);
            } else {
                return valueFrom(b.right, index - b.left.itemCount());
            }
        } else if (n instanceof Leaf l) {
            return l.items[index];
        } else {
            return null;
        }
    }

    private static Node withInsertion(Node n, int index, Object[] values) {
        if (n instanceof Branch b) {
            if (index < b.left.itemCount()) {
                return shallowlyBalanced(
                        new Branch(
                                withInsertion(b.left, index, values),
                                b.right));
            } else {
                return shallowlyBalanced(
                        new Branch(
                                b.left,
                                withInsertion(b.right, index - b.left.itemCount(), values)));
            }
        } else if (n instanceof Leaf l) {
            final var items = withInsertion(l.items, index, values);

            if (items.length <= LEAF_SIZE) {
                return new Leaf(items);
            } else {
                return fromPartitions(
                        partition(items, LEAF_SIZE));
            }
        } else {
            throw new NullPointerException();
        }
    }

    private static Node withInsertion(Node n, int index, Node values) {
        if (n instanceof Branch b) {
            if (index < b.left.itemCount()) {
                return shallowlyBalanced(
                        new Branch(
                                withInsertion(b.left, index, values),
                                b.right));
            } else {
                return shallowlyBalanced(
                        new Branch(
                                b.left,
                                withInsertion(b.right, index - b.left.itemCount(), values)));
            }
        } else if (n instanceof Leaf l) {
            final var preceding = Arrays.copyOfRange(l.items, 0, index);
            final var following = Arrays.copyOfRange(l.items, index, l.items.length);

            return withInsertion(
                    withInsertion(values, values.itemCount(), following),
                    0, preceding);
        } else {
            throw new NullPointerException();
        }
    }

    private static Node withoutRange(Node n, int start, int end) {
        if (start == 0 && end == n.itemCount()) return EMPTY_LEAF;
        if (start == end) return n;

        if (n instanceof Branch b) {
            if (start < b.left.itemCount()) {
                if (end > b.left.itemCount()) {
                    return shallowlyBalanced(
                            shallowlyPruned(new Branch(
                                    withoutRange(b.left, start, b.left.itemCount()),
                                    withoutRange(b.right, 0, end - b.left.itemCount()))));
                } else {
                    return shallowlyBalanced(
                            shallowlyPruned(new Branch(
                                    withoutRange(b.left, start, end),
                                    b.right)));
                }
            } else {
                return shallowlyBalanced(
                        shallowlyPruned(new Branch(
                                b.left,
                                withoutRange(
                                        b.right,
                                        start - b.left.itemCount(),
                                        end - b.left.itemCount()))));
            }
        } else if (n instanceof Leaf l) {
            final var items = withoutRange(l.items, start, end - start);

            return items.length == 0 ? EMPTY_LEAF : new Leaf(items);
        } else {
            throw new NullPointerException();
        }
    }


    private static Node withReplacement(Node n, int start, int end, Object[] src, int srcStart) {
        final var length = end - start;
        final var srcEnd = srcStart + length;
        final var srcLength = srcEnd - srcStart;
        if (length <= 0) return n;

        if (n instanceof Branch b) {
            if (start < b.left.itemCount()) {
                if (end > b.left.itemCount()) {
                    final var leftPortion = b.left.itemCount() - start;
                    return new Branch(
                            withReplacement(b.left, start, b.left.itemCount(), src, srcStart),
                            withReplacement(b.right, 0, end - b.left.itemCount(), src, srcStart + leftPortion));
                } else {
                    return new Branch(
                            withReplacement(b.left, start, end, src, srcStart),
                            b.right);
                }
            } else {
                return new Branch(
                        b.left,
                        withReplacement(b.right, start - b.left.itemCount(), end - b.left.itemCount(), src, srcStart));
            }
        } else if (n instanceof Leaf l) {
            final var newItems = withReplacement(l.items, start, end, src, srcStart);
            if (newItems != l.items) {
                return new Leaf(newItems);
            } else {
                return l;
            }
        } else throw new NullPointerException();
    }

    private static Node subListFrom(Node n, int start, int end) {
        if (n instanceof Branch b) {
            if (start < b.left.itemCount()) {
                if (end > b.left.itemCount()) {
                    return shallowlyBalanced(
                            shallowlyPruned(new Branch(
                                    subListFrom(b.left, start, b.left.itemCount()),
                                    subListFrom(b.right, 0, end - b.left.itemCount()))));
                } else {
                    return shallowlyBalanced(
                            shallowlyPruned(new Branch(
                                    subListFrom(b.left, start, end),
                                    EMPTY_LEAF)));
                }
            } else {
                return shallowlyBalanced(shallowlyPruned(
                        new Branch(
                                EMPTY_LEAF,
                                subListFrom(b.right,
                                        start - b.left.itemCount(),
                                        end - b.left.itemCount()))));
            }
        } else if (n instanceof Leaf l) {
            if (start == 0 && end == l.items.length) return l;

            final var items = Arrays.copyOfRange(l.items, start, end);

            if (items.length == 0) {
                return EMPTY_LEAF;
            } else {
                return new Leaf(items);
            }
        } else {
            throw new NullPointerException();
        }
    }

    private static Node shallowlyBalanced(Node n) {
        // everything is awful
        var result = n;
        int bestAbsoluteBalanceFactor = result.absoluteBalanceFactor();

        for (int loopCount = 0; loopCount < n.weight(); ++loopCount) { // Failsafe in case the loop doesn't halt.
            if (result.balanceFactor() > 1) {
                final var rotatedLeftAbsoluteBalanceFactor = Math.abs(rotatedLeftBalanceFactor(result));

                if (rotatedLeftAbsoluteBalanceFactor < bestAbsoluteBalanceFactor) {
                    bestAbsoluteBalanceFactor = rotatedLeftAbsoluteBalanceFactor;
                    result = rotatedLeft(result);
                } else {
                    return result;
                }
            } else if (result.balanceFactor() < -1) {
                final var rotatedRightAbsoluteBalanceFactor = Math.abs(rotatedRightBalanceFactor(result));

                if (rotatedRightAbsoluteBalanceFactor < bestAbsoluteBalanceFactor) {
                    bestAbsoluteBalanceFactor = rotatedRightAbsoluteBalanceFactor;
                    result = rotatedRight(result);
                } else {
                    return result;
                }
            } else {
                return result;
            }
        }
        System.err.println("Loop count limit exceeded in PersistentList.shallowlyBalanced(Node n)");
        return result;
    }

    private static Node shallowlyPruned(Node n) {
        if (n.itemCount() == 0) {
            return EMPTY_LEAF;
        } else if (n instanceof Branch b) {
            if (b.left.itemCount() == 0) {
                return b.right;
            } else if (b.right.itemCount() == 0) {
                return b.left;
            } else {
                return b;
            }
        } else {
            return n;
        }
    }

    private static Node shallowlyPrunedAndBalanced(Node n) {
        return shallowlyBalanced(shallowlyPruned(n));
    }

    private static int rotatedLeftBalanceFactor(Node root) {
        if (root instanceof Branch b) {
            if (b.right instanceof Branch right) {
                // balanceFactor == right.weight - left.weight;
                // weight = left.weight + right.weight + 1    (the 1 is the weight of node itself)

                return right.right.weight() - (b.left.weight() + right.left.weight() + 1);
//                return new Structure(new Structure(b.left, right_b.left), right_b.right); from rotatedLeft(Node n)
            } else {
                return b.balanceFactor();
            }
        } else {
            return root.balanceFactor();
        }
    }

    private static int rotatedRightBalanceFactor(Node root) {
        if (root instanceof Branch b) {
            if (b.left instanceof Branch left) {
                // balanceFactor == right.weight - left.weight;
                // weight = left.weight + right.weight + 1    (the 1 is the weight of node itself)

                return (left.right.weight() + b.right.weight() + 1) - left.left.weight();
//                return new Structure(left_b.left, new Structure(left_b.right, b.right)); from rotatedRight(Node n)
            } else {
                return b.balanceFactor();
            }
        } else {
            return root.balanceFactor();
        }
    }

    private static Node rotatedLeft(Node root) {
        if (root instanceof Branch b) {
            if (b.right instanceof Branch right) {
                return new Branch(new Branch(b.left, right.left), right.right);
            } else {
                return b;
            }
        } else {
            return root;
        }
    }

    private static Node rotatedRight(Node root) {
        if (root instanceof Branch b) {
            if (b.left instanceof Branch left) {
                return new Branch(left.left, new Branch(left.right, b.right));
            } else {
                return b;
            }
        } else {
            return root;
        }
    }

    private static Boolean nodesAreEqual(Node a, Node b) {
        if (a == b) return true;
        if (a.itemCount() != b.itemCount()) return false;
        if (a.itemCount() == 0 && b.itemCount() == 0) return true;
        if (a.quickHash() != b.quickHash()) return false;

        return fullCheckValueEquality(a, b);

//        if (a instanceof Branch ab && b instanceof Branch bb) {
//            return nodesAreEqual(ab.left, bb.left) && nodesAreEqual(ab.right, bb.right);
//        } else {
//            return fullCheckValueEquality(a, b);
//        }
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

        final var itemIteratorA = new ItemIterator(a, leafIteratorA, currentLeafA, offsetA, offsetA);
        final var itemIteratorB = new ItemIterator(b, leafIteratorB, currentLeafB, offsetB, offsetB);

        while (itemIteratorA.hasNext() && itemIteratorB.hasNext()) {
            if (!areEqual(itemIteratorA.next(), itemIteratorB.next())) return false;
        }

        return !(itemIteratorA.hasNext() || itemIteratorB.hasNext());
    }

    private static Node sorted(Node n, Comparator<Object> comparator) {
        if (n.itemCount() == 1) return n;
        if (n.itemCount() == 0) return EMPTY_LEAF;

        if (n instanceof Leaf l) {
            if (isSorted(l.items, comparator)) return l;

            final var sortedItems = Arrays.copyOf(l.items, l.items.length);
            Arrays.sort(sortedItems, comparator);
            return new Leaf(sortedItems);

//            return new Leaf(Arrays.stream(l.items).sorted(comparator).toArray());
        } else if (n instanceof Branch b) {
            if (isSorted(new ItemsIterable(b), comparator)) return b;

            // sort the branches
            final var leftSorted = shallowlyPrunedAndBalanced(sorted(b.left, comparator));
            final var rightSorted = shallowlyPrunedAndBalanced(sorted(b.right, comparator));
//            final var leftSorted = sorted(b.left, comparator);
//            final var rightSorted = sorted(b.right, comparator);

            // skip merging if possible
            if (leftSorted.itemCount() == 0) {
                return rightSorted;
            } else if (rightSorted.itemCount() == 0) {
                return leftSorted;
            }

            // skip merging if possible
            if (comparator.compare(
                    valueFrom(leftSorted, leftSorted.itemCount() - 1),
                    valueFrom(rightSorted, 0)) < 0) {
                return new Branch(leftSorted, rightSorted);
            }

            // merge the sorted branches
            final var sortedItems = merged(
                    new ItemsIterable(leftSorted),
                    new ItemsIterable(rightSorted),
                    comparator);

            // partition the resulting items
            if (sortedItems.length <= LEAF_SIZE) {
                return new Leaf(sortedItems);
            } else {
                final var partitions = partition(sortedItems, LEAF_SIZE);
                return fromPartitions(partitions);
            }
        } else {
            throw new NullPointerException();
        }
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
    public static int nonOrderedHash(Object[] objects) {
        int result = 0;
        for (final var obj : objects) {
            result = nonOrderedHash(result, obj);
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

            if (!areEqual(original[indexO], newItem)) {
                containedChange = true;
            }

            result[indexO] = newItem;
        }

        for(int i = end; i < original.length; ++i){
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

            return new Branch(
                    fromPartitions(partitions, index, leftCount),
                    fromPartitions(partitions, index + leftCount, rightCount));
        }
    }

    private static Node fromPartitions(Object[][] partitions) {
        return fromPartitions(partitions, 0, partitions.length);
    }

    private static Node fromArray(Object[] array) {
        if (array.length <= LEAF_SIZE) {
            return new Leaf(array);
        } else {
            return fromPartitions(
                    partition(array, LEAF_SIZE));
        }
    }

    private static Node fromIterable(Iterable<Object> iterable) {
        return fromArray(StreamSupport.stream(iterable.spliterator(), false).toArray());
    }

    private static boolean areEqual(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    /**
     * ~ (!!! only call if a's items are equal to b's items !!!) ~
     */
    private static void consolidateCaches(Branch a, Branch b) {
        if (a.quickHashCache == null) {
            if (b.quickHashCache != null) {
                a.quickHashCache = b.quickHashCache;
            }
        } else {
            b.quickHashCache = a.quickHashCache;
        }
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

    private static boolean isSorted(Object[] array, Comparator<Object> comparator) {
        for (int i = 1; i < array.length; ++i) {
            if (comparator.compare(array[i - 1], array[i]) > 0) return false;
        }
        return true;
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

    private static <T> void reverse(T[] array) {
        for (int i = 0; i < array.length / 2; ++i) {
            final var temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    private static Object[] reversed(Object[] array) {
        final var result = new Object[array.length];
        for (int i = 0; i < array.length; ++i) {
            result[result.length - 1 - i] = array[i];
        }
        return result;
    }

    // ======================== Inner Classes ===============================
    private static class ItemsIterable implements Iterable<Object> {
        private final Node node;

        public ItemsIterable(Node node) {
            this.node = node;
        }

        public Iterator<Object> iterator() {
            return new ItemIterator(node);
        }
    }

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

        int quickHash();

        int weight();

        int balanceFactor();

        int absoluteBalanceFactor();
    }

    private static class Branch implements Node {
        final Node left;
        final Node right;
        final int itemCount;
        final int leafCount;
        final int weight;
        volatile Integer quickHashCache = null;

        public Branch(Node left, Node right) {
            nullCheck(left);
            nullCheck(right);

            this.left = left;
            this.right = right;


            this.itemCount = left.itemCount() + right.itemCount();
            this.leafCount = left.leafCount() + right.leafCount();
            this.weight = left.weight() + right.weight() + 1;
        }

        public int leafCount() {
            return leafCount;
        }

        public int itemCount() {
            return itemCount;
        }

        public int quickHash() {
            if (quickHashCache != null) return quickHashCache;

            synchronized (this) {
                if (quickHashCache == null) {
                    quickHashCache = nonOrderedHash(left.quickHash(), right.quickHash());
                }
            }

            return quickHashCache;
        }

        public int weight() {
            return weight;
        }

        public int balanceFactor() {
            return right.weight() - left.weight();
        }

        public int absoluteBalanceFactor() {
            return Math.abs(balanceFactor());
        }
    }

    private static class Leaf implements Node {
        final Object[] items;
        volatile Integer quickHashCache = null;

        public Leaf(Object[] items) {
            this.items = items == null ? new Object[0] : items;
        }

        public int quickHash() {
            if (quickHashCache != null) return quickHashCache;

            synchronized (this) {
                if (quickHashCache == null) {
                    quickHashCache = nonOrderedHash(items);
                }
            }

            return quickHashCache;
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

        public int absoluteBalanceFactor() {
            return 0;
        }
    }

    public class SelfIterator implements Iterator<T> {
        private final ItemIterator itemIterator = new ItemIterator(root);

        public boolean hasNext() {
            return itemIterator.hasNext();
        }

        public T next() {
            return (T) itemIterator.next();
        }
    }

    private static class ItemIterator implements Iterator<Object> {
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

        public Object next() {
            if (index >= node.itemCount()) {
                return null;
            }

            if (currentLeaf == null) {
                initializeCurrentLeaf();
            }


            while (index >= offset + currentLeaf.items.length) {
                advanceToNextLeaf();
            }

            return currentLeaf.items[(index++) - offset];
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
        Stack<Node> location;
        int progress;
        final Node node;

        public void reset() {
            location = null;
            progress = 0;
        }

        public LeafIterator(Node node) {
            this.node = node;
            reset();
        }

        public boolean hasNext() {
            return progress < node.leafCount();
        }

        public Leaf next() {
            if (!hasNext()) return null;

            ++progress;
            if (location == null) {
                initializeLocation();
            } else {
                advanceToNextLeaf();
            }
            return currentLeaf();
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

            while (child == ((Branch) parent).right) {
                child = parent;
                location.pop();
                parent = location.peek();
            }

            location.push(((Branch) parent).right);

            moveDownToLeaf();
        }


        private void moveDownToLeaf() {
            Node current = location.peek();

            while (current instanceof Branch b) {
                location.push(current = b.left);
            }
        }

        private void initializeLocation() {
            location = new Stack<>();
            location.push(node);
            moveDownToLeaf();
        }
    }
}


/**
 * Mimics C#'s IEnumerator which is preferable in some cases (though not all).
 */
class Enumerator<T> {
    public Enumerator(Iterator<T> iter) {
        this.iter = iter;
    }

    private Iterator<T> iter;
    private Boolean hadNext = null;
    private T current = null;

    public boolean moveNext() {
        advance();
        return hadNext;
    }

    public boolean unStarted() {
        return hadNext == null;
    }

    public boolean complete() {
        return hadNext != null && !hadNext;
    }

    public T current() {
        return current;
    }

    private void advance() {
        if (iter.hasNext()) {
            hadNext = true;
            current = iter.next();
        } else {
            hadNext = false;
            current = null;
        }
    }
}
