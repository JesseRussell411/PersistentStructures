import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PersistentList<T> {
    private static final int LEAF_SIZE = 4;

    private final Node data;
    private final boolean reversed;

    //==============
    // constructors
    //==============
    public PersistentList() {
        this.data = new Leaf();
        this.reversed = false;
    }

    public PersistentList(PersistentArray<T> items) {
        this.data = nodeFrom(items);
        this.reversed = false;
    }

    public PersistentList(T[] items) {
        this(new PersistentArray<>(items));
    }

    public PersistentList(Iterable<T> items) {
        this(new PersistentArray<>(items));
    }

    public PersistentList(Stream<T> items) {
        this(new PersistentArray<>(items));
    }

    PersistentList(Node data, boolean reversed) {
        Objects.requireNonNull(data);
        this.data = data;
        this.reversed = reversed;
    }

    //===========
    // utilities
    //===========
    private Node nodeFrom(PersistentArray<T> items) {
        return nodeFrom(items, false);
    }

    private Node nodeFrom(PersistentArray<T> items, boolean reverseLeafs) {
        if (items.size() <= LEAF_SIZE) {
            return new Leaf(reverseLeafs ? items.reverse() : items);
        } else {
            final var partitions = ArrayUtils.partition(items.getItems(), LEAF_SIZE, items.isReversed(), reverseLeafs);
            return nodeFromPartitions(partitions);
        }
    }

    private Node nodeFromPartitions(Object[][] partitions) {
        return nodeFromPartitions(partitions, 0, partitions.length, false, false);
    }

    private Node nodeFromPartitions(Object[][] partitions, boolean reversed) {
        return nodeFromPartitions(partitions, 0, partitions.length, reversed, false);
    }

    private Node nodeFromPartitions(Object[][] partitions, boolean reversed, boolean reverseLeafs) {
        return nodeFromPartitions(partitions, 0, partitions.length, reversed, reverseLeafs);
    }

    private Node nodeFromPartitions(Object[][] partitions, int start, int count, boolean reversed, boolean reverseLeafs) {
        if (count == 1) {
            return new Leaf(new PersistentArray<T>(partitions[start], reversed ^ reverseLeafs));
        } else {
            final var rightPortion = count / 2;
            final var leftPortion = count - rightPortion;

            return new Branch(
                    nodeFromPartitions(partitions, start, leftPortion, reversed, reverseLeafs),
                    nodeFromPartitions(partitions, start + leftPortion, rightPortion, reversed, reverseLeafs)
            );
        }
    }

    //==================
    // internal classes
    //==================
    private interface Node {
        int absoluteBalanceFactor();

        int balanceFactor();

        int itemCount();

        int leafCount();

        int nodeCount();

        int depth();
    }

    private class Branch implements Node {
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

        public int absoluteBalanceFactor() {
            return Math.abs(balanceFactor());
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

    private class Leaf implements Node {
        final PersistentArray<T> items;

        Leaf(PersistentArray<T> items) {
            this.items = items;
        }

        Leaf() {
            this(new PersistentArray<>());
        }

        public int absoluteBalanceFactor() {
            return 0;
        }

        public int balanceFactor() {
            return 0;
        }

        public int itemCount() {
            return items.size();
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
