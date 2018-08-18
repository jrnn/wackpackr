package wackpackr.util;

/**
 * A dynamically resizing byte array, whose outward behaviour is supposed to resemble the {@link
 * StringBuilder} class, more or less.
 *
 * @author Juho Juurinen
 */
public final class ByteString
{
    private byte[] bytes = new byte[8];
    private int size = 0;

    /**
     * Constructs a new, empty ByteString.
     */
    public ByteString() {}

    /**
     * Constructs a new ByteString from an arbitrary number of bytes, in the order they are given.
     *
     * @param bs byte sequence of arbitrary length
     */
    public ByteString(byte... bs)
    {
        append(bs);
    }

    /**
     * Constructs a new ByteString by gluing together an arbitrary number of ByteString instances,
     * in the order they are given.
     *
     * @param bs arbitrary number of ByteString instances
     */
    public ByteString(ByteString... bs)
    {
        int n = 8;
        int newSize = 0;

        for (ByteString b : bs)
        {
            newSize += b.size;
            n = Math.max(n, b.bytes.length);
        }
        while (n < newSize)
            n <<= 1;

        bytes = new byte[n];
        for (ByteString b : bs)
            append(b);
    }

    /**
     * Returns the number of bytes in the ByteString.
     *
     * @return number of bytes in the ByteString
     */
    public int size()
    {
        return size;
    }

    /**
     * Returns the byte at the given index.
     *
     * @param index zero-based index of the byte to return
     * @return byte at the given index
     * @throws IndexOutOfBoundsException if given index is out of bounds
     */
    public byte byteAt(int index)
    {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();

        return bytes[index];
    }

    /**
     * Returns all bytes in the ByteString as an array, in proper sequence.
     *
     * <p>There are no references between the ByteString and the returned array, so the caller can
     * safely modify the array without fear of undesired side-effects.</p>
     *
     * @return an array containing all bytes stored in the ByteString
     */
    public byte[] getBytes()
    {
        return copyRange(0, size);
    }

    /**
     * Returns all bytes in the ByteString as an array, beginning from the given index and extending
     * to the end of the ByteString.
     *
     * <p>There are no references between the ByteString and the returned array, so the caller can
     * safely modify the array without fear of undesired side-effects.</p>
     *
     * @param beginIndex zero-based beginning index, inclusive
     * @return an array containing all bytes stored in the ByteString starting from the given index
     * @throws IndexOutOfBoundsException if the given index is negative or greater than the size of
     *         the ByteString
     */
    public byte[] getBytes(int beginIndex)
    {
        return copyRange(beginIndex, size);
    }

    /**
     * Returns the bytes within the given bounds in the ByteString as an array.
     *
     * <p>There are no references between the ByteString and the returned array, so the caller can
     * safely modify the array without fear of undesired side-effects.</p>
     *
     * @param beginIndex zero-based beginning index, inclusive
     * @param endIndex zero-based ending index, exclusive
     * @return an array containing all bytes stored in the ByteString within the given bounds
     * @throws IndexOutOfBoundsException if you're doing something wrong
     */
    public byte[] getBytes(int beginIndex, int endIndex)
    {
        return copyRange(beginIndex, endIndex);
    }

    /**
     * Returns an exact clone of the ByteString. The clone is a new, separate instance, so no
     * modification to it is carried over to the original.
     *
     * @return exact clone of the ByteString
     */
    public ByteString copy()
    {
        return new ByteString(this);
    }

    /**
     * Appends an arbitrary number of bytes to the end of the ByteString, in the order they are
     * given.
     *
     * @param bs byte sequence of arbitrary length
     * @return a reference to this object
     */
    public ByteString append(byte... bs)
    {
        int newSize = size + bs.length;

        if (newSize > bytes.length)
            expand(newSize);

        System.arraycopy(bs, 0, bytes, size, bs.length);
        size = newSize;

        return this;
    }

    /**
     * Appends the bytes stored in the given ByteString instance to the end of this ByteString, in
     * the same order.
     *
     * @param bs a ByteString instance
     * @return a reference to this object
     */
    public ByteString append(ByteString bs)
    {
        int newSize = size + bs.size;

        if (newSize > bytes.length)
            expand(newSize);

        System.arraycopy(bs.bytes, 0, bytes, size, bs.size);
        size = newSize;

        return this;
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private void expand(int newSize)
    {
        int n = bytes.length;

        while (n < newSize)
            n <<= 1;

        byte[] newBytes = new byte[n];
        System.arraycopy(bytes, 0, newBytes, 0, size);

        bytes = newBytes;
    }

    private byte[] copyRange(int start, int end)
    {
        int length = end - start;

        if (start < 0 || length < 0 || size < end)
            throw new IndexOutOfBoundsException();

        byte[] copy = new byte[length];
        System.arraycopy(bytes, start, copy, 0, length);

        return copy;
    }
}
