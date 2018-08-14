package wackpackr.util;

/**
 * Limited application of a circular, doubly linked list with a sentinel node. Offers only a few
 * basic operations, nowhere near as extensive as e.g. the {@link java.util.List} interface.
 *
 * <p>Elements are always added to the end of the list, and hence ordered "chronologically" from
 * the oldest to the most recently added element. Duplicate and {@code null} elements are permitted.
 * </p>
 *
 * <p>Performance presumably is what can be expected from a linked list with zero optimisation. In
 * other words, this is not a smart choice for anything, unless using a linked list in particular
 * can be justified.</p>
 *
 * @author Juho Juurinen
 * @param <E> the class of elements stored in a list instance
 */
public class CircularDoublyLinkedList<E>
{
    /**
     * Dummy node that marks the beginning and end of the list, such that {@code sentinel.next}
     * points at the first and {@code sentinel.prev} at the last element. In particular, when the
     * list is empty, {@code sentinel.prev == sentinel == sentinel.next}.
     */
    private final Node sentinel = new Node();

    /**
     * Number of elements in the list.
     */
    private int size = 0;

    /**
     * Returns the number of elements in the list.
     *
     * @return number of elements in the list
     */
    public int size()
    {
        return size;
    }

    /**
     * Returns {@code true} if and only if the list contains exactly zero elements.
     *
     * @return true if the list is empty
     */
    public boolean isEmpty()
    {
        return (size < 1);
    }

    /**
     * Returns {@code true} if the given element occurs at least once in the list.
     *
     * @param e element whose presence is tested
     * @return true if the list contains given element
     */
    public boolean contains(E e)
    {
        return (search(e) != null);
    }

    /**
     * Appends the given element to the end of the list.
     *
     * @param e element to add to the list
     */
    public void add(E e)
    {
        Node node = new Node(e);
        size++;

        sentinel.prev.next = node;
        sentinel.prev = node;
    }

    /**
     * Returns the element at the given position in the list.
     *
     * @param index zero-based index of the element to return
     * @return element at the given position in the list
     * @throws IndexOutOfBoundsException if given index is beyond the list range
     */
    public E get(int index)
    {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        Node node = sentinel.next;

        for (; index > 0; index--)
            node = node.next;

        return node.elem;
    }

    /**
     * Reads through the list and removes the first occurrence of the given element, if one exists.
     * The list remains unchanged if it does not contain the given element.
     *
     * @param e element to remove from list, if present
     */
    public void remove(E e)
    {
        deleteNode(search(e));
    }

    /**
     * Removes the element at the beginning of the list: that is, the "oldest" of all elements in
     * the list. If the list is empty, nothing happens.
     */
    public void removeFirst()
    {
        if (!isEmpty())
            deleteNode(sentinel.next);
    }

    /**
     * Removes the element at the end of the list: that is, the most recently added of all elements
     * in the list. If the list is empty, nothing happens.
     */
    public void removeLast()
    {
        if (!isEmpty())
            deleteNode(sentinel.prev);
    }

    /**
     * Returns all elements in this list as an array in proper sequence: that is, in the order they
     * were added to the list.
     *
     * <p>There are no references between the list elements and the returned array, so the caller
     * can safely modify the array without fear of undesired side-effects.</p>
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
     * Returns all elements in this list as an array, ordered from the last element on the list to
     * the first: that is, from the most recently added to the oldest one.
     *
     * <p>There are no references between the list elements and the returned array, so the caller
     * can safely modify the array without fear of undesired side-effects.</p>
     *
     * @return an array containing all list elements from last to first
     */
    public Object[] toArrayReverse()
    {
        int i = 0;
        Object[] elems = new Object[size];

        for (Node node = sentinel.prev; node != sentinel; node = node.prev)
            elems[i++] = node.elem;

        return elems;
    }

    /**
     * Nested helper class restricted only for use by the parent class. Defines a minimal list node,
     * used as building blocks of the list. There are separate constructors for the pseudo sentinel
     * node, vs. actual nodes that hold list elements. The constructors partially handle linking the
     * new node with respect to the sentinel node.
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

        Node(E elem)
        {
            this.elem = elem;
            this.prev = sentinel.prev;
            this.next = sentinel;
        }
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private Node search(E e)
    {
        Node node = sentinel.next;

        if (e == null)
            node = searchNull();
        else
            while (node != sentinel && !e.equals(node.elem))
                node = node.next;

        return (node == sentinel)
                ? null
                : node;
    }

    private Node searchNull()
    {
        Node node = sentinel.next;

        while (node != sentinel && node.elem != null)
            node = node.next;

        return node;
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
