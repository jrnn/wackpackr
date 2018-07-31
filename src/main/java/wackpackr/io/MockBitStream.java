package wackpackr.io;

/**
 * Helper class that mimics a bit stream with a String of 1s and 0s. Intended for use only in tests.
 *
 * @author Juho Juurinen
 */
public class MockBitStream
{
    private String s;

    public MockBitStream(String s)
    {
        this.s = s;
    }

    public boolean hasNext()
    {
        return !s.isEmpty();
    }

    public int length()
    {
        return s.length();
    }

    public boolean nextBit()
    {
        return next(1) == 1;
    }

    public byte nextByte()
    {
        return (byte) next(8);
    }

    public long next32Bits()
    {
        return next(32);
    }

    private long next(int n)
    {
        if (s.length() < n)
            throw new ArrayIndexOutOfBoundsException();

        long next = Long.parseLong(s.substring(0, n), 2);
        s = s.substring(n);

        return next;
    }
}
