package wackpackr.io;

/**
 * Name should say it all... A bullshit helper class that imitates a bit stream. Intended for use in
 * early stage of project, while still tinkering with Strings instead of 1s and 0s.
 *
 * @author jjuurine
 */
public class MockBitStream
{
    private String s;

    public MockBitStream(String s)
    {
        this.s = s;
    }

    public void write(String t)
    {
        s += t;
    }

    public boolean hasNext()
    {
        return !s.isEmpty();
    }

    public int nextBit()
    {
        return next(1);
    }

    public int nextByte()
    {
        return next(8);
    }

    @Override
    public String toString()
    {
        return s;
    }

    private int next(int n)
    {
        if (s.length() < n)
            throw new ArrayIndexOutOfBoundsException();

        int next = Integer.parseInt(s.substring(0, n), 2);
        s = s.substring(n);

        return next;
    }
}
