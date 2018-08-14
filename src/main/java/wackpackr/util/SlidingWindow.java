package wackpackr.util;

/**
 * Simple utility that mimics a kind of "sliding window policy" cache, that only retains the latest
 * N inbound elements, where N is the given cache size. More precisely, once the cache is full, it
 * dumps oldest elements from one end as new ones are inserted at the other (FIFO).
 *
 * <p>In practice, the cache is implemented as a circular array. Whenever the underlying array is
 * full, the head pointer wraps around, so that each new element overwrites the oldest element.</p>
 *
 * <p>Cache size is determined at instantiation, and cannot be changed thereafter.</p>
 *
 * <p>Beside the head pointer, there is a separate read pointer ("cursor") that allows arbitrary
 * access to the cache window, similar to most I/O tools (such as {@link java.io.RandomAccessFile}).
 * Control of the cursor is delegated fully to the user, and it never moves by itself. Trying to
 * read or move beyond either end of the cache window results in an exception.</p>
 *
 * <p>{@code null} elements are permitted.</p>
 *
 * @author Juho Juurinen
 * @param <E> the class of elements stored in a sliding window instance
 */
public class SlidingWindow<E>
{
    /**
     * Backing array for storing elements, using circular access.
     */
    private final Object[] queue;

    /**
     * Maximum number of elements retained in queue at any one time.
     */
    private final int size;

    /**
     * Read pointer into the underlying array.
     */
    private int cursor = 0;

    /**
     * Array index to which last inserted element was written. Also, tells how many elements have
     * been inserted altogether (minus one). Due to circular access, must use modulo "window size"
     * when reading from or writing to the array.
     */
    private int head = -1;

    public SlidingWindow(int windowSize)
    {
        if (windowSize < 1)
            throw new IllegalArgumentException();

        this.size = windowSize;
        this.queue = new Object[size];
    }

    /**
     * Returns current position of the read pointer.
     *
     * @return current cursor position
     */
    public int cursor()
    {
        return cursor;
    }

    /**
     * Inserts given element at head of window. If maximum window size has been reached, oldest
     * element at end is removed and returned at the same time. Otherwise returns {@code null}.
     *
     * @param e element to insert
     * @return element displaced by insertion, or null
     */
    public E insert(E e)
    {
        head++;

        E out = (E) queue[head % size];
        queue[head % size] = e;

        return out;
    }

    /**
     * Moves cursor forward to the next position.
     *
     * @throws IndexOutOfBoundsException if trying to move beyond cache window
     */
    public void move()
    {
        move(1);
    }

    /**
     * Moves cursor forward or backward from its current position by given offset.
     *
     * @param offset distance to move
     * @throws IndexOutOfBoundsException if trying to move beyond cache window
     */
    public void move(int offset)
    {
        int i = cursor + offset;
        throwExceptionIfOutOfBounds(i);

        cursor = i;
    }

    /**
     * Reads, but does not remove, element at current cursor position. Cursor position is not
     * affected as a result.
     *
     * @return element at current cursor position
     * @throws IndexOutOfBoundsException if trying to read beyond cache window
     */
    public E read()
    {
        return read(0);
    }

    /**
     * Reads, but does not remove, element at given offset from current cursor position. Cursor
     * position is not affected as a result.
     *
     * @param offset number of elements to jump over
     * @return element at given offset from current cursor position
     * @throws IndexOutOfBoundsException if trying to read beyond cache window
     */
    public E read(int offset)
    {
        int i = cursor + offset;
        throwExceptionIfOutOfBounds(i);

        return (E) queue[i % size];
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private void throwExceptionIfOutOfBounds(int i)
    {
        if (i < 0 || i < head - size || i > head)
            throw new IndexOutOfBoundsException();
    }
}
