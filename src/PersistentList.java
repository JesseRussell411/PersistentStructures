import java.util.Arrays;
import java.util.Objects;

public class PersistentList<T> {
    private static final int LEAF_HOLDER_LIMIT = 3;
    private static final LeafHolder EMPTY_LEAF_HOLDER = new LeafHolder(new Object[0]);

    private final Node root;

    private PersistentList(Node root) {
        this.root = root;
    }

    public int size() {
        return leafCountOf(root);
    }

    private T get(int index, Node n) {
        if (n == null) {
            return null;
        }

        if (n instanceof Structure s) {
            if (index < leafCountOf(s.left)) {
                return get(index, s.left);
            } else {
                return get(index - leafCountOf(s.left), s.right);
            }
        } else if (n instanceof LeafHolder h) {
            final var items = h.items;
            return items == null ? null : (T) items[index];
        } else {
            return null;
        }
    }

    public T get(int index) {
        return get(index, root);
    }

    private Node set(int index, T value, Node n) {
        if (n instanceof Structure s) {
            if (index < leafCountOf(s.left)) {
                return new Structure(set(index, value, s.left), s.right);

            } else {
                return new Structure(s.left, set(index - leafCountOf(s.left), value, s.right));
            }
        } else if (n instanceof LeafHolder h) {
            final var items = Arrays.copyOf(h.items, h.items.length);
            items[index] = value;
            return new LeafHolder(items);
        }
    }

    public PersistentList<T> set(int index, T value) {
        return new PersistentList<>(set(index, value, root));
    }

    private static class boolRef {
        public boolean value;

        public boolRef(boolean value) {
            this.value = value;
        }
    }

    private Node insert(int index, Object[] values, Node n, boolRef toBalance) {
        if (n instanceof Structure s) {
            if (index < leafCountOf(s.left)) {
                return new Structure(insert(index, values, s.left, toBalance), s.right);
            } else {
                return new Structure(s.left, insert(index - leafCountOf(s.left), values, s.right, toBalance));
            }
        } else if (n instanceof LeafHolder h) {
            final var items = withInsertedValues(h.items, index, values);
            if (items.length < LEAF_HOLDER_LIMIT) {
                toBalance.value = false;
                return new LeafHolder(items);
            } else {
                toBalance.value = true;
                final var partitions = partition(items, LEAF_HOLDER_LIMIT);
                return fromPartitions(partitions, 0, partitions.length);
            }
        } else {
            throw new NullPointerException();
        }
    }

    /**
     * @param partitions The partitions array to draw from
     * @param index      index in the partitions array
     * @param count      the number of partitions
     */
    private Node fromPartitions(Object[][] partitions, int index, int count) {
        if (count == 1) {
            return new LeafHolder(partitions[index]);
        } else {
            final var remainder = count % 2;
            final var leftCount = (count / 2) + (remainder > 0 ? 1 : 0);
            final var rightCount = count / 2;

            return new Structure(
                    fromPartitions(partitions, index, leftCount),
                    fromPartitions(partitions, index + leftCount, rightCount));
        }
    }

    private Node _insert(int index, Object[] values) {
        if (index < 0 || size() < index || values == null || values.length == 0) {
            return root;
        }
        // TODO optimize?: balanced might not be the most optimized thing here.
        final var toBalance = new boolRef(false);
        final var result = insert(index, values, root, toBalance);
        return toBalance.value ? balanced(result) : result;
    }

    public PersistentList<T> insert(int index, T[] values) {
        return new PersistentList<>(_insert(index, values));
    }

    private Node remove(int index, int length, Node n) {
        if (n instanceof Structure s) {
            if (index < leafCountOf(s.left)) {
                if (length <= leafCountOf(s.left) - index) {
                    return new Structure(remove(index, length, s.left), s.right);
                } else {
                    final var leftPortionLength = leafCountOf(s.left) - index;
                    final var rightPortionLength = length - leftPortionLength;

                    return new Structure(
                            remove(index, leftPortionLength, s.left),
                            remove(index + leftPortionLength, rightPortionLength, s.right));
                }
            } else {
                return new Structure(s.left, remove(index + leafCountOf(s.left), length, s.right));
            }
        } else if (n instanceof LeafHolder h) {
            final var items = Arrays.copyOf(h.items, index);
            return new LeafHolder(items);
        }
    }

    private Node _remove(int index, int length) {

    }

    private Node _add(int index, T value) {
        return _insert(index, new Object[]{value});
    }

    public PersistentList<T> add(int index, T value) {
        return new PersistentList<>(_add(index, value));
    }


    private Node balanced(Node n) {
        if (n instanceof Structure s) {
            Node result = s;
            while (Math.abs(result.balance()) > 1) {
                if (result.balance() < -1) {
                    result = rotatedRight(balanced(s.left), s, balanced(s.right));
                } else if (result.balance() > 1) {
                    result = rotatedLeft(balanced(s.left), s, balanced(s.right));
                }
            }

            return result;
        } else {
            return n;
        }
    }

    private Node pruned(Node n){
        if (n instanceof Structure s){
            if (leafCountOf(s.left) == 0){
                return pruned(s.right);
            } if (leafCountOf(s.right) == 0){
                return pruned(s.left);
            } else {
                return s;
            }
        } else {
            return n;
        }
    }

    private Structure rotatedRight(Structure s) {
        if (s.left instanceof Structure left) {
            return new Structure(left.left, new Structure(left.right, s.right));
        } else {
            return s;
        }
    }

    private Structure rotatedLeft(Structure s) {
        if (s.right instanceof Structure right) {
            return new Structure(new Structure(s.left, right.left), right.right);
        } else {
            return s;
        }
    }

    private Node rotatedRight(Node left, Node n, Node right) {
        if (left instanceof Structure left_s) {
            return new Structure(left_s.left, new Structure(left_s.right, right));
        } else {
            return n;
        }
    }

    private Node rotatedLeft(Node left, Node n, Node right) {
        if (right instanceof Structure right_s) {
            return new Structure(new Structure(left, right_s.left), right_s.right);
        } else {
            return n;
        }
    }


    private static int totalHashFrom(Node n) {
        return n == null ? 0 : n.totalHash();
    }

    private static int leafCountOf(Node n) {
        return n == null ? 0 : n.leafCount();
    }

    private static int weightOf(Node n) {
        return n == null ? 0 : n.weight();
    }

    private static int balanceFrom(Node n) {
        return n == null ? 0 : n.balance();
    }

    private static Object[] withInsertedValues(Object[] array, int index, Object[] values) {
        if (values == null || values.length == 0 || index < 0 || array.length < index) {
            return array;
        }

        final var result = new Object[array.length + values.length];

        for (int i = 0; i < index; ++i) {
            result[i] = array[i];
        }

        for (int i = index; i < index + values.length; ++i) {
            result[i] = values[i];
        }

        for (int i = index + values.length; i < array.length + values.length) {
            result[i] = array[i - values.length];
        }

        return result;
    }

    private static Object[][] partition(Object[] array, int size) {
        if (size < 1 || array == null || size >= array.length) {
            return new Object[][]{array};
        }

        final var remainder = array.length % size;
        final var wholeCount = array.length / size

        final var result = new Object[wholeCount + (remainder > 0 ? 1 : 0)][];

        for (int i = 0; i < wholeCount - 1; ++i) {
            final var index = i * size;
            result[i] = Arrays.copyOfRange(array, index, index + size);
        }

        if (remainder > 0) {
            final var index = (wholeCount) * size;
            result[wholeCount] = Arrays.copyOfRange(array, index, index + remainder);
        }

        return result;
    }

    private interface Node {
        int leafCount();

        int totalHash();

        int weight();

        int balance();
    }

    private static class Structure implements Node {
        public final Node left;
        public final Node right;
        public final int leafCount;
        private Integer totalHash = null;
        private final int weight;
        private final int balance;

        public Structure(Node left, Node right) {
            this.left = left;
            this.right = right;

            this.leafCount = leafCountOf(left) + leafCountOf(right);
            this.weight = weightOf(left) + weightOf(right);
            this.balance = weightOf(right) - weightOf(left);
        }

        public int leafCount() {
            return leafCount;
        }

        public int totalHash() {
            if (totalHash == null) {
                totalHash = Objects.hash(totalHashFrom(left), totalHashFrom(right));
            }

            return totalHash;
        }

        public int weight() {
            return weight;
        }

        public int balance() {
            return balance;
        }
    }

    private static class LeafHolder implements Node {
        public final Object[] items;
        private Integer totalHash = null;

        public LeafHolder(Object[] items) {
            this.items = items == null ? new Object[0] : items;
        }

        public int totalHash() {
            if (totalHash == null) {
                totalHash = Arrays.hashCode(items);
            }

            return totalHash;
        }

        public int leafCount() {
            return items.length;
        }

        public int weight() {
            return items.length > 0 ? 1 : 0;
        }

        public int balance() {
            return 0;
        }
    }
}
