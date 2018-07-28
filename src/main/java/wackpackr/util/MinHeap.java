package wackpackr.util;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Simple binary min-heap providing just the basic operations: <strong>adding</strong> and <strong>
 * popping</strong> elements in O(log n) time, and <strong>peeking</strong> in constant time.
 *
 * Elements are ordered by their natural ordering. Trying to add elements that do not implement the
 * {@link Comparable} interface results in {@code ClassCastException}.
 *
 * {@code null} elements are not allowed. Trying to add a {@code null} element results in {@code
 * NullPointerException}.
 *
 * The heap is unbounded, though it stores elements in an array. Similar to e.g. {@link ArrayList},
 * array capacity is expanded or decreased at certain threshold "fill rates", however so that adding
 * elements is still possible in amortized O(log n) time.
 *
 * Not thread-safe.
 *
 * @author Juho Juurinen
 * @param <E> the class of elements held in a heap instance
 */
public class MinHeap<E>
{
    /**
     * Initial and minimum size to allocate for the heap array.
     */
    private static final int MIN_CAPACITY = 8;

    /**
     * Generic array used for storing elements in heap. Emulates a balanced binary tree.
     */
    private Object[] heap;

    /**
     * Number of elements in heap.
     */
    private int size;

    public MinHeap()
    {
        this.heap = new Object[MIN_CAPACITY];
        this.size = 0;
    }

    public boolean isEmpty()
    {
        return size < 1;
    }

    public int size()
    {
        return size;
    }

    /**
     * Retrieves (but does not remove) the element at the head of the heap.
     *
     * @return Smallest element in the heap, or {@code null} if heap is empty.
     */
    public E peek()
    {
        return isEmpty()
                ? null
                : (E) heap[0];
    }

    /**
     * Inserts the given element into the heap.
     *
     * @param  e Element to insert into the heap.
     * @throws NullPointerException if given element is null.
     * @throws ClassCastException if given element does not implement {@link Comparable} interface.
     */
    public void add(E e)
    {
        if (e == null)
            throw new NullPointerException();

        int i = size;
        size++;

        if (size > heap.length / 2)
            expand();

        while (i > 0 && isLessThan(e, heap[parent(i)]))
        {
            heap[i] = heap[parent(i)];
            i = parent(i);
        }

        heap[i] = e;
    }

    /**
     * Retrieves and removes the element at the head of the heap.
     *
     * @return Smallest element in the heap.
     * @throws NoSuchElementException if heap is empty.
     */
    public E pop()
    {
        if (isEmpty())
            throw new NoSuchElementException();

        E e = (E) heap[0];
        size--;
        heap[0] = heap[size];

        if (size < heap.length / 4)
            contract();

        heapify(0);
        return e;
    }

    /* Private helper methods below, no comments or description given. */

    private void heapify(int i)
    {
        int left = left(i);
        int right = right(i);

        if (right != -1)
        {
            int smaller = isLessThan(heap[left], heap[right])
                    ? left
                    : right;

            if (isLessThan(heap[smaller], heap[i]))
            {
                swap(i, smaller);
                heapify(smaller);
            }
        }
        else if (left == size && isLessThan(heap[left], heap[i]))
            swap(i, left);
    }

    private void swap(int i, int j)
    {
        E e = (E) heap[i];
        heap[i] = heap[j];
        heap[j] = e;
    }

    private int parent(int i)
    {
        return i / 2;
    }

    private int left(int i)
    {
        return (2 * i) > size
                ? -1
                : 2 * i;
    }

    private int right(int i)
    {
        return (2 * i + 1) > size
                ? -1
                : 2 * i + 1;
    }

    private void contract()
    {
        if (heap.length / 2 >= MIN_CAPACITY)
            heap = Arrays.copyOfRange(heap, 0, heap.length / 2);
    }

    private void expand()
    {
        heap = Arrays.copyOfRange(heap, 0, heap.length * 2);
    }

    private boolean isLessThan(Object smaller, Object bigger)
    {
        Comparable<? super E> c = (Comparable<? super E>) smaller;
        return c.compareTo((E) bigger) == -1;
    }
}
