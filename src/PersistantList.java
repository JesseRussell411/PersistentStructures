public class PersistantList<T> {
    private static final EMPTY_LEAF = new Leaf(new Object[0]);

    private final Node data;

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
