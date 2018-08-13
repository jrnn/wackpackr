package wackpackr.util;

import java.util.NoSuchElementException;

/**
 * Simple binary min-heap providing just the basic operations: adding and popping elements in
 * O(log n) time, and peeking in constant time.
 *
 * Elements are ordered by their natural ordering. Trying to add elements that do not implement the
 * {@link Comparable} interface results in {@code ClassCastException}.
 *
 * {@code null} elements are not allowed. Trying to add a {@code null} element results in {@code
 * NullPointerException}.
 *
 * The heap is unbounded, though it stores elements in an array. Similar to e.g. {@link
 * java.util.ArrayList}, array capacity is expanded or decreased at certain threshold "load
 * factors", however so that adding and popping elements is still possible in amortised O(log n)
 * time.
 *
 * @author Juho Juurinen
 * @param <E> the class of elements held in a heap instance
 */
public class MinHeap<E>
{
    /**
     * Initial and minimum size to allocate for the backing array.
     */
    private static final int MIN_CAPACITY = 8;

    /**
     * Generic array used for storing the heap elements. Emulates a balanced binary tree.
     */
    private Object[] heap  = new Object[MIN_CAPACITY];

    /**
     * Number of elements in the heap.
     */
    private int size = 0;

    /**
     * Returns the number of elements in the heap.
     *
     * @return number of elements in the heap
     */
    public int size()
    {
        return size;
    }

    /**
     * Returns {@code true} if and only if the heap contains exactly zero elements.
     *
     * @return true if the heap is empty
     */
    public boolean isEmpty()
    {
        return (size < 1);
    }

    /**
     * Retrieves (but does not remove) the element at the head of the heap.
     *
     * @return smallest element in the heap, or {@code null} if heap is empty
     */
    public E peek()
    {
        return (isEmpty())
                ? null
                : (E) heap[0];
    }

    /**
     * Inserts the given element into the heap.
     *
     * @param e element to add to the heap
     * @throws NullPointerException if given element is null
     * @throws ClassCastException if given element does not implement {@link Comparable} interface
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
     * @return smallest element in the heap
     * @throws NoSuchElementException if heap is empty
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


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private void heapify(int i)
    {
        int left = left(i);
        int right = right(i);

        if (right != -1)
        {
            int smaller = (isLessThan(heap[left], heap[right]))
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
        return (i / 2);
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
        if (heap.length / 2 < MIN_CAPACITY)
            return;

        Object[] newHeap = new Object[heap.length / 2];
        System.arraycopy(heap, 0, newHeap, 0, size + 1);

        heap = newHeap;
    }

    private void expand()
    {
        Object[] newHeap = new Object[heap.length * 2];
        System.arraycopy(heap, 0, newHeap, 0, size + 1);

        heap = newHeap;
    }

    private boolean isLessThan(Object smaller, Object bigger)
    {
        Comparable<? super E> c = (Comparable<? super E>) smaller;
        return (c.compareTo((E) bigger) < 0);
    }
}
