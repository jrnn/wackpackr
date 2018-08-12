package wackpackr.util;

/**
 * Limited application of a circular, doubly linked list with a sentinel node. List operations are
 * provided only to the extent needed for purposes of the wackpackr project.
 *
 * As is conventional of linked lists, elements are always added to the beginning of the list, so
 * that they are ordered from most recently added to the "oldest" one. This is the order given e.g.
 * on calling {@link #toArray()}.
 *
 * Performance presumably is what can be expected from a linked list with zero optimisation. In
 * other words, this is not a smart choice for anything, apart from contexts where the use of linked
 * lists in particular can be justified.
 *
 * {@code null} elements are permitted.
 *
 * @author Juho Juurinen
 * @param <E> the class of elements stored in a list instance
 */
public class CircularDoublyLinkedList<E>
{
    /**
     * Dummy node that marks the beginning and end of the list, such that {@code sentinel.next} is
     * the first and {@code sentinel.prev} is the last element. In particular, when the list is
     * empty, {@code sentinel.next = sentinel.prev = sentinel}.
     */
    private final Node sentinel = new Node();

    /**
     * Number of elements in the list.
     */
    private int size = 0;

    /**
     * Inserts the given element at the beginning of the list.
     *
     * @param e element to insert to list
     */
    public void insert(E e)
    {
        Node node = new Node(e);
        size++;

        sentinel.next.prev = node;
        sentinel.next = node;
    }

    /**
     * Reads through the list starting from the most recently added element, and removes the first
     * occurrence of the given element, if one exists. The list remains unchanged if it does not
     * contain the given element.
     *
     * @param e element to remove from list, if present
     */
    public void remove(E e)
    {
        deleteNode(search(e));
    }

    /**
     * Removes the element in the last position of the list: that is, the "oldest" of all elements
     * in the list. If the list is empty, nothing happens.
     */
    public void removeLast()
    {
        if (sentinel.prev != sentinel)
            deleteNode(sentinel.prev);
    }

    /**
     * Returns all elements in this list as an array, ordered from the most recently added to the
     * "oldest" one.
     *
     * There are no references between the list elements and the returned array, so the caller can
     * safely modify the array without fear of undesired side-effects.
     *
     * @return an array containing all list elements from first to last
     */
    public Object[] toArray()
    {
        int i = 0;
        Object[] elems = new Object[size];

        for (Node node = sentinel.next; node != sentinel; node = node.next)
            elems[i++] = node.elem;

        return elems;
    }

    /**
     * Nested helper class restricted only for use by the parent class. Describes a minimal list
     * node, instances of which are used as building blocks of the list. There are separate
     * constructors for the "pseudo" sentinel node, vs. actual nodes that hold list elements. The
     * linking of new nodes at head of the list is handled by the constructor.
     */
    private final class Node
    {
        final E elem;
        Node prev, next;

        Node()
        {
            this.elem = null;
            this.prev = this.next = this;
        }

        Node(E e)
        {
            this.elem = e;
            this.prev = sentinel;
            this.next = sentinel.next;
        }
    }


    /* --- Private helper methods below, no comments or description given. --- */


    private Node search(E e)
    {
        Node node = sentinel.next;

        while (node != sentinel && !e.equals(node.elem))
            node = node.next;

        return (node == sentinel)
                ? null
                : node;
    }

    private void deleteNode(Node node)
    {
        if (node == null)
            return;

        size--;
        node.next.prev = node.prev;
        node.prev.next = node.next;
    }
}
