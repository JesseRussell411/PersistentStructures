import java.util.Iterator;
import java.util.function.Predicate;

public class PersistentStack<T> implements Iterable<T> {
    private final Node head;
    private final int size;
    private final int hashCode;

    public int size() {
        return size;
    }

    private static int getHashCode(Object o) {
        if (o == null) {
            return 0;
        } else {
            return o.hashCode();
        }
    }

    private static int combineHashCodes(int a, int b) {
        return (a ^ b) + b;
    }

    private static int uncombineHashCode(int r, int b) {
        return (r - b) ^ b;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public Iterator<T> iterator() {
        return new PersistentStackIterator<>(this);
    }

    private class Node {
        final Node next;
        final T value;

        public Node(Node next, T value) {
            this.next = next;
            this.value = value;
        }
    }

    private PersistentStack(int size, Node head, int hashCode) {
        this.size = size;
        this.head = head;
        this.hashCode = hashCode;
    }

    public PersistentStack() {
        head = null;
        size = 0;
        hashCode = 0;
    }

    public PersistentStack(Iterable<T> data) {
        final var node = nodeFromIterable(data, null, 0, 0);
        this.head = node.node;
        this.size = node.size;
        this.hashCode = node.hashCode;
    }

    public PersistentStack(T[] data) {
        final var node = nodeFromArray(data, null, 0, 0);
        this.head = node.node;
        this.size = node.size;
        this.hashCode = node.hashCode;
    }

    private static boolean nullableEquals(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else if (b == null) {
            return false;
        } else {
            return a.equals(b);
        }
    }


    @Override
    public boolean equals(Object other) {
        if (other instanceof PersistentStack otherPStack) {
            if (this == otherPStack) {
                return true;
            } else if (size() != otherPStack.size()) {
                return false;
            } else if (hashCode() != otherPStack.hashCode()) {
                return false;
            } else {
                var thisNode = head;
                var thatNode = otherPStack.head;

                while (thisNode != null) {
                    if (thisNode == thatNode) {
                        return true;
                    } else if (!nullableEquals(thisNode.value, thatNode.value)) {
                        return false;
                    } else {
                        thisNode = thisNode.next;
                        thatNode = thatNode.next;
                    }
                }

                return false;
            }
        } else {
            return false;
        }
    }

    private class NodeAndSizeAndHashCode {
        public final Node node;
        public final int size;
        public final int hashCode;

        public NodeAndSizeAndHashCode(Node node, int size, int hashCode) {
            this.node = node;
            this.size = size;
            this.hashCode = hashCode;
        }
    }

    private NodeAndSizeAndHashCode nodeFromIterable(Iterable<T> it, Node initialNode, int initialSize, int initialHashCode) {
        Node node = initialNode;
        int size = initialSize;
        int hashCode = initialHashCode;

        for (final var item : it) {
            node = new Node(node, item);
            ++size;
            hashCode = combineHashCodes(hashCode, getHashCode(item));
        }

        return new NodeAndSizeAndHashCode(node, size, hashCode);
    }

    private NodeAndSizeAndHashCode nodeFromArray(T[] arr, Node initialNode, int initialSize, int initialHashCode) {
        Node node = initialNode;
        int size = initialSize;
        int hashCode = initialHashCode;

        for (final var item : arr) {
            node = new Node(node, item);
            ++size;
            hashCode = combineHashCodes(hashCode, getHashCode(item));
        }

        return new NodeAndSizeAndHashCode(node, size, hashCode);
    }

    public PersistentStack<T> pop() {
        if (head == null) {
            return this;
        } else {
            return new PersistentStack<>(size - 1, head.next, uncombineHashCode(hashCode, getHashCode(head.value)));
        }
    }

    public PersistentStack<T> pop(int count) {
        if (count <= 0 || head == null) {
            return this;
        } else {
            Node current = head;
            int hashCode = this.hashCode;

            int i = 0;
            do {
                ++i;
                hashCode = uncombineHashCode(hashCode, getHashCode(current.value));
                current = current.next;
            } while (i < count && current != null);

            return new PersistentStack<>(size - i, current, hashCode);
        }
    }


    public PersistentStack<T> popUntil(Predicate<T> test) {
        if (test == null || head == null) {
            return this;
        } else {
            Node current = head;
            int hashCode = this.hashCode;

            int i = 0;
            do {
                ++i;
                hashCode = uncombineHashCode(hashCode, getHashCode(current.value));
                current = current.next;
            } while (!test.test(current.value) && current.next != null);

            return new PersistentStack<>(size - i, current, hashCode);
        }
    }

    public PersistentStack<T> put(T value) {
        return new PersistentStack<>(size + 1, new Node(head, value), combineHashCodes(hashCode, getHashCode(value)));
    }

    public PersistentStack<T> putMany(Iterable<T> items) {
        final var node = nodeFromIterable(items, head, size, hashCode);
        return new PersistentStack<>(node.size, node.node, node.hashCode);
    }

    public PersistentStack<T> putMany(T[] items) {
        final var node = nodeFromArray(items, head, size, hashCode);
        return new PersistentStack<>(node.size, node.node, node.hashCode);
    }

    public T peek() {
        if (head == null) {
            return null;
        } else {
            return head.value;
        }
    }

    public static class PersistentStackIterator<T> implements Iterator<T> {
        private PersistentStack<T>.Node current;

        public PersistentStackIterator(PersistentStack<T> stack) {
            current = stack.head;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if (current != null) {
                final var result = current.value;
                current = current.next;
                return result;
            } else {
                return null;
            }
        }
    }


}
