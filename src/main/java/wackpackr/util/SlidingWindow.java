package wackpackr.util;

/**
 * Simple utility that mimics a kind of "sliding window policy" cache, that only retains the latest
 * N inbound elements, where N is the given cache size. More precisely, once the cache is full, it
 * dumps oldest elements from one end whenever new ones are inserted at the other.
 *
 * Resembles a {@Link Queue} due to the FIFO principle, but is not meant as such: supports only
 * enqueueing, not dequeueing elements. However, allows reading any elements within the cache window
 * in a similar vein as most I/O tools (such as {@Link RandomAccessFile}).
 *
 * In practice the cache is implemented as a circular array. Whenever the underlying array is full,
 * the head pointer wraps around, so that each new element overwrites the oldest element in queue.
 * Cache size is determined at instantiation, and cannot be changed thereafter.
 *
 * Beside the head pointer, there is a separate read pointer ("cursor") that allows arbitrary access
 * to stored elements. Control of the cursor is delegated fully to the user, i.e. it does not move
 * by itself. Trying to read beyond either end of the cache window results in an exception.
 *
 * @author Juho Juurinen
 */
public class SlidingWindow
{
    /**
     * Array for storing elements, using circular access.
     */
    private final byte[] queue;

    /**
     * Maximum number of elements retained in queue at any one time.
     */
    private final int size;

    /**
     * Read pointer into the underlying array.
     */
    private int cursor = 0;

    /**
     * Number of elements inserted so far. Also, array index to which next inserted element is
     * written. Due to circular access, must use modulo "window size" when writing.
     */
    private int head = 0;

    public SlidingWindow(int windowSize)
    {
        this.size = windowSize;
        this.queue = new byte[size];
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
     * element at end of window is discarded at the same time.
     *
     * @param b element to insert
     */
    public void insert(byte b)
    {
        queue[head % size] = b;
        head++;
    }

    /**
     * Returns the number of elements that can be read from current cursor position. In particular,
     * returns 0 when cursor is at the last possible position.
     *
     * @return distance from current cursor position to last inserted element
     */
    public int available()
    {
        return head - cursor - 1;
    }

    /**
     * Move cursor forward to the next position.
     *
     * @throws IndexOutOfBoundsException if trying to move beyond cache window
     */
    public void move()
    {
        move(1);
    }

    /**
     * Move cursor forward or backward from its current position by given offset.
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
    public byte read()
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
    public byte read(int offset)
    {
        int i = cursor + offset;
        throwExceptionIfOutOfBounds(i);

        return queue[i % size];
    }


    /* --- Private helper methods below, no comments or description given. --- */


    private void throwExceptionIfOutOfBounds(int i)
    {
        if (i < 0 || i < head - size || i > head)
            throw new IndexOutOfBoundsException();
    }
}
