import java.util.Arrays;
import java.util.Objects;

public class ImmutableList<T> {
    private static final int LEAF_SIZE = 32;
    private static final Leaf EMPTY_LEAF = new Leaf(new Object[0]);
    private volatile Node root;

    private ImmutableList(Node root) {
        this.root = root;
    }

    public ImmutableList(T... items) {
        throw new NotImplementedException();
    }

    public ImmutableList(Iterable<T> items) {
        throw new NotImplementedException();
    }

    public ImmutableList(ImmutableList<T> items) {
        root = items.root;
    }

    public int size() {
        return root.itemCount();
    }

    //=====================================
    // single item manipulation
    //=====================================
    public ImmutableList<T> add(int index, T item) {
        Utils_lists.requireIndexInBounds(index, size() + 1);
        return new ImmutableList<>(add(root, index, item));
    }

    public T get(int index) {
        Utils_lists.requireIndexInBounds(index, size());
        return (T) get(root, index);
    }

    public ImmutableList<T> remove(int index) {
        Utils_lists.requireIndexInBounds(index, size());
        return new ImmutableList<>(remove(root, index));
    }

    public ImmutableList<T> swap(int index, T item) {
        Utils_lists.requireIndexInBounds(index, size());
        return new ImmutableList<>(swap(root, index, item));
    }

    // stack and queue operations
    public ImmutableList<T> put(T item) {
        return add(size(), item);
    }

    public ImmutableList<T> pop() {
        return remove(size() - 1);
    }

    public ImmutableList<T> push(T item) {
        return add(0, item);
    }

    public ImmutableList<T> pull() {
        return remove(0);
    }

    //=====================================
    // multi item manipulation
    //=====================================
    public ImmutableList<T> insert(int index, ImmutableList<T> items, int itemsStart, int length) {
        Utils_lists.requireIndexInBounds(index, size());
        Objects.requireNonNull(items);
        if (items.size() <= 0) return this;

        return new ImmutableList<>(insert(root, index, items.root, itemsStart, length));
    }

    public ImmutableList<T> insert(int index, ImmutableList<T> items) {
        return insert(index, items, 0, items.size());
    }

    public ImmutableList<T> get(int index, int length) {
        Utils_lists.requireIndexInBounds(index, size());
        if (length <= 0) return new ImmutableList<>(EMPTY_LEAF);

        return new ImmutableList<>(get(root, index, length));
    }

    public ImmutableList<T> remove(int index, int length) {
        Utils_lists.requireIndexInBounds(index, size());
        if (length <= 0) return this;

        return new ImmutableList<>(remove(root, index, length));
    }

    public ImmutableList<T> replace(int index, ImmutableList<T> items) {
        Utils_lists.requireIndexInBounds(index, size());
        Objects.requireNonNull(items);
        if (items.size() <= 0) return this;
        // TODO: create iterator, then toArray, then use array method for replace.
        throw new NotImplementedException();
    }

    public ImmutableList<T> append(ImmutableList<T> items, int itemsStart, int length) {
        return insert(size(), items, itemsStart, length);
    }

    public ImmutableList<T> append(ImmutableList<T> items) {
        return append(items, 0, items.size());
    }

    public ImmutableList<T> pop(int length) {
        throw new NotImplementedException();
    }

    public ImmutableList<T> prepend(ImmutableList<T> items, int itemsStart, int length) {
        throw new NotImplementedException();
    }

    public ImmutableList<T> prepend(ImmutableList<T> items) {
        return prepend(items, 0, items.size());
    }

    // ====================================
    // internal manipulation -- single item
    // ====================================
    private static Node add(Node node, int index, Object item) {
        if (node instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return balance(new Branch(
                        add(branch.left, index, item),
                        branch.right));
            } else {
                return balance(new Branch(
                        branch.left,
                        add(branch.right, index - branch.left.itemCount(), item)));
            }
        } else if (node instanceof Leaf leaf) {
            return buildNode(Utils_lists.withAddition(leaf.items, index, item));
        } else throw new NullPointerException();
    }

    private static Object get(Node node, int index) {
        if (node instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return get(branch.left, index);
            } else {
                return get(branch.right, index - branch.left.itemCount());
            }
        } else if (node instanceof Leaf leaf) {
            return leaf.items[Utils_lists.requireIndexInBounds(index, leaf.items.length)];
        } else throw new NullPointerException();
    }

    private static Node remove(Node node, int index) {
        if (node instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return pruneAndBalance(new Branch(
                        remove(branch.left, index),
                        branch.right));
            } else {
                return pruneAndBalance(new Branch(
                        branch.left,
                        remove(branch.right, index - branch.left.itemCount())));
            }
        } else if (node instanceof Leaf leaf) {
            if (leaf.items.length <= 1) {
                return EMPTY_LEAF;
            } else {
                return new Leaf(Utils_lists.without(leaf.items, index));
            }
        } else throw new NullPointerException();
    }

    private static Node swap(Node node, int index, Object item) {
        if (node instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return new Branch(
                        swap(branch.left, index, item),
                        branch.right);
            } else {
                return new Branch(
                        branch.left,
                        swap(branch.right, index - branch.left.itemCount(), item));
            }
        } else if (node instanceof Leaf leaf) {
            if (Objects.equals(leaf.items[index], item)) return leaf;

            return new Leaf(Utils_lists.withSwap(leaf.items, index, item));
        } else throw new NullPointerException();
    }

    // ====================================
    // internal manipulation -- multi item -- Node
    // ====================================
    private static Node insert(Node node, int index, Node items, int start, int length) {
        return insert(node, index, get(items, start, length));
    }

    private static Node insert(Node node, int index, Node items) {
        if (node instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return pruneAndBalance(new Branch(
                        insert(branch.left, index, items),
                        branch.right));
            } else {
                return pruneAndBalance(new Branch(
                        branch.left,
                        insert(branch.right, index - branch.left.itemCount(), items)));
            }
        } else if (node instanceof Leaf leaf) {
            // TODO: optimize
            final var preceding = Arrays.copyOfRange(leaf.items, 0, index);
            final var following = Arrays.copyOfRange(leaf.items, index, leaf.items.length);

            return pruneAndBalance(insert(
                    insert(items,
                            0,
                            preceding),
                    preceding.length + items.itemCount(),
                    following));
        } else throw new NullPointerException();
    }

    private static Node get(Node node, int index, int length) {
        if (index + length < node.itemCount()) {
            return _get(node, index, length);
        } else {
            return _get(node, index, node.itemCount() - index);
        }
    }

    private static Node _get(Node node, int index, int length) {
        if (length <= 0) return EMPTY_LEAF;

        if (node instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                if (index + length < branch.left.itemCount()) {
                    return _get(branch.left, index, length);
                } else {
                    final var leftPortion = branch.left.itemCount() - index;
                    return pruneAndBalance(balance(new Branch(
                            _get(branch.left, index, leftPortion),
                            _get(branch.right, 0, length - leftPortion))));
                }
            } else {
                return _get(branch.right, index - branch.left.itemCount(), length);
            }
        } else if (node instanceof Leaf leaf) {
            return new Leaf(Arrays.copyOfRange(leaf.items, index, index + length));
        } else throw new NullPointerException();
    }

    private static Node remove(Node node, int index, int length) {
        if (length <= 0) return node;
        if (index <= 0 && length >= node.itemCount()) return EMPTY_LEAF;

        if (index + length < node.itemCount()) {
            return _remove(node, index, length);
        } else {
            return _remove(node, index, node.itemCount() - index);
        }
    }

    private static Node _remove(Node node, int index, int length) {
        if (node instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                if (index + length < branch.left.itemCount()) {
                    return pruneAndBalance(
                            new Branch(
                                    _remove(branch.left, index, length),
                                    branch.right));
                } else {
                    final var leftPortion = branch.left.itemCount() - index;
                    return pruneAndBalance(new Branch(
                            _remove(branch.left, index, leftPortion),
                            _remove(branch.right, 0, length - leftPortion)));
                }
            } else {
                return pruneAndBalance(new Branch(
                        branch.left,
                        _remove(branch.right, index - branch.left.itemCount(), length)));
            }
        } else if (node instanceof Leaf leaf) {
            if (length == leaf.items.length) return EMPTY_LEAF;

            return new Leaf(Utils_lists.without(leaf.items, index, length));
        } else throw new NullPointerException();
    }

    // ====================================
    // internal manipulation -- multi item -- Array
    // ====================================
    private static Node insert(Node node, int index, Object[] items) {
        return insert(node, index, items, 0, items.length);
    }

    private static Node insert(Node node, int index, Object[] items, int start, int length) {
        if (start + length < items.length) {
            return _insert(node, index, items, start, length);
        } else {
            return _insert(node, index, items, start, items.length - start);
        }
    }

    private static Node _insert(Node node, int index, Object[] items, int start, int length) {
        if (length == 0) return node;

        if (node instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                return balance(new Branch(
                        _insert(branch.left, index, items, start, length),
                        branch.right));
            } else {
                return balance(new Branch(
                        branch.left,
                        _insert(branch.right, index - branch.left.itemCount(), items, start, length)));
            }
        } else if (node instanceof Leaf leaf) {
            return buildNode(
                    Utils_lists.withInsertion(leaf.items, index, items, start, length));
        } else throw new NullPointerException();
    }

    private static Node replace(Node node, int index, Object[] items, int start, int length) {
        if (start + length >= items.length) {
            return _replace(node, index, items, start, items.length - start);
        } else {
            return _replace(node, index, items, start, length);
        }
    }

    private static Node _replace(Node node, int index, Object[] items, int start, int length) {
        if (length <= 0) return node;

        if (node instanceof Branch branch) {
            if (index < branch.left.itemCount()) {
                if (index + length < branch.left.itemCount()) {
                    return new Branch(
                            _replace(branch.left, index, items, start, length),
                            branch.right);
                } else {
                    final var leftPortion = branch.left.itemCount() - index;
                    return new Branch(
                            _replace(branch.left, index, items, start, leftPortion),
                            _replace(branch.right, 0, items, start + leftPortion, length - leftPortion));
                }
            } else {
                return new Branch(
                        branch.left,
                        _replace(branch.right, index - branch.left.itemCount(), items, start, length));
            }
        } else if (node instanceof Leaf leaf) {
            final var newItems = new Object[leaf.items.length];
            System.arraycopy(items, start, newItems, index, length);
            return new Leaf(newItems);
        } else throw new NullPointerException();
    }

    // ====================================
    // internal utils
    // ====================================
    private static Node pruneAndBalance(Node node) {
        return balance(prune(node));
    }

    private static Node prune(Node node) {
        if (node instanceof Branch branch) {
            if (branch.right.itemCount() == 0) {
                return branch.left;
            } else if (branch.left.itemCount() == 0) {
                return branch.left;
            } else {
                return branch;
            }
        }
    }

    private static Node balance(Node node) {
        if (node.absoluteBalanceFactor() <= 1) return node;

        var result = node;

        for (int loopCount = 0; loopCount < node.nodeCount(); loopCount++) {
            if (node.balanceFactor() < 0) {
                final var rotatedRight = rotateRight(node);
                if (rotatedRight.absoluteBalanceFactor() < result.absoluteBalanceFactor()) {
                    result = rotatedRight;
                } else {
                    return result;
                }
            } else {
                final var rotatedLeft = rotateLeft(node);
                if (rotatedLeft.absoluteBalanceFactor() < result.absoluteBalanceFactor()) {
                    result = rotatedLeft;
                } else {
                    return result;
                }
            }
        }

        return result;
    }

    private static Node rotateLeft(Node node) {
        if (node instanceof Branch branch && branch.right instanceof Branch rightBranch) {
            return new Branch(new Branch(branch.left, rightBranch.left), rightBranch.right);
        } else return node;
    }

    private static Node rotateRight(Node node) {
        if (node instanceof Branch branch && branch.left instanceof Branch leftBranch) {
            return new Branch(leftBranch.left, new Branch(leftBranch.right, branch.right));
        } else return node;
    }

    private static Node buildNode(Object[] items) {
        Objects.requireNonNull(items);
        if (items.length <= LEAF_SIZE) return new Leaf(items);

        final var partitions = Utils_lists.partition(items, LEAF_SIZE);
        return buildNode(partitions, 0, partitions.length);
    }

    private static Node buildNode(Object[][] partitions, int start, int length) {
        if (length <= 0) return EMPTY_LEAF;

        else if (length == 1) {
            return buildNode(partitions[start]);
        } else {
            final var rightPortion = length / 2;
            final var leftPortion = length - rightPortion;
            return new Branch(
                    buildNode(partitions, start, leftPortion),
                    buildNode(partitions,
                            start + leftPortion,
                            rightPortion));
        }
    }

    //=====================================
    // internal classes
    //=====================================
    private interface Node {
        int absoluteBalanceFactor();

        int balanceFactor();

        int itemCount();

        int leafCount();

        int nodeCount();

        int depth();
    }

    private static class Branch implements Node {
        final Node left;
        final Node right;
        final int itemCount;
        final int leafCount;
        final int nodeCount;
        final int depth;

        Branch(Node left, Node right) {
            this.left = left;
            this.right = right;

            this.itemCount = left.itemCount() + right.itemCount();
            this.leafCount = left.leafCount() + right.leafCount();
            this.nodeCount = left.nodeCount() + right.nodeCount() + 1;
            this.depth = Math.max(left.depth(), right.depth()) + 1;
        }

        public int balanceFactor() {
            return right.depth() - left.depth();
        }

        public int itemCount() {
            return itemCount;
        }

        public int leafCount() {
            return leafCount;
        }

        public int nodeCount() {
            return nodeCount;
        }

        public int depth() {
            return depth;
        }
    }

    private static class Leaf implements Node {
        final Object[] items;

        Leaf(Object[] items) {
            this.items = items;
        }

        public int absoluteBalanceFactor() {
            return 0;
        }

        public int balanceFactor() {
            return 0;
        }

        public int itemCount() {
            return items.length;
        }

        public int leafCount() {
            return 1;
        }

        public int nodeCount() {
            return 1;
        }

        public int depth() {
            return 1;
        }
    }
}
