package wackpackr.util;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ByteStringTest
{
    private static final int N = 1024;
    private final byte[] bytes = new byte[N];
    private ByteString bs;

    @Before
    public void before()
    {
        ThreadLocalRandom.current().nextBytes(bytes);
        bs = new ByteString(bytes);
    }

    @Test
    public void byteStringKnowsHowManyBytesItHas()
    {
        bs = new ByteString();

        for (int i = 0; i < N; i++)
        {
            Assert.assertTrue(bs.size() == i);
            bs.append(bytes[i]);
        }
    }

    @Test
    public void canAccessBytesInArbitraryIndex()
    {
        for (int i = 0; i < N; i++)
            Assert.assertTrue(bs.byteAt(i) == bytes[i]);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void accessingNegativeIndexThrowsException()
    {
        bs.byteAt(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void accessingIndexAboveStringSizeThrowsException()
    {
        bs = new ByteString()
                .append(Arrays.copyOfRange(bytes, 0, 383));

        bs.byteAt(bs.size());
    }

    @Test
    public void copiesAreIdenticalToOriginal()
    {
        ByteString copy = bs.copy();

        Assert.assertTrue(bs.size() == copy.size());
        Assert.assertArrayEquals(
                bs.getBytes(),
                copy.getBytes()
        );
    }

    @Test
    public void modifyingCopiesDoesNotAffectOriginal()
    {
        int initialSize = bs.size();
        byte[] initialBytes = bs.getBytes();

        ByteString copy = bs.copy();
        copy.append((byte) 1);

        Assert.assertTrue(bs.size() == initialSize);
        Assert.assertArrayEquals(
                bs.getBytes(),
                initialBytes
        );
    }

    @Test
    public void modifyingBytesDoesNotAffectOriginal()
    {
        byte[] bytesOut = bs.getBytes();
        ThreadLocalRandom.current().nextBytes(bytesOut);

        Assert.assertArrayEquals(
                bytes,
                bs.getBytes()
        );
    }

    @Test
    public void bytesAreAppendedCorrectly()
    {
        bs = new ByteString();

        for (int i = 0, j = 1; i < N; i += j, j++)
            bs
                    .append(Arrays.copyOfRange(
                            bytes,
                            i,
                            Math.min(i + j, N)
                    ));

        Assert.assertArrayEquals(
                bytes,
                bs.getBytes()
        );
    }

    @Test
    public void byteStringsAreAppendedCorrectly()
    {
        bs = new ByteString();

        for (int i = 0, j = 1; i < N; i += j, j++)
            bs
                    .append(new ByteString(
                            Arrays.copyOfRange(
                                    bytes,
                                    i,
                                    Math.min(i + j, N)
                            )));

        Assert.assertArrayEquals(
                bytes,
                bs.getBytes()
        );
    }

    @Test
    public void appendingMethodsCanBeChained()
    {
        byte[] expected = new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 };

        bs = new ByteString()
                .append()
                .append((byte) 1)
                .append((byte) 2, (byte) 3)
                .append(new byte[]{ 4, 5, 6 })
                .append(new ByteString())
                .append(new ByteString((byte) 7))
                .append(new ByteString((byte) 8, (byte) 9))
                .append(new ByteString(new byte[]{ 10, 11, 12 }))
                .append(new ByteString(
                        new ByteString((byte) 13),
                        new ByteString((byte) 14, (byte) 15),
                        new ByteString(new byte[]{ 16, 17, 18 })
                ));

        Assert.assertArrayEquals(
                expected,
                bs.getBytes()
        );
    }

    @Test
    public void canCombineAnyNumberOfByteStringsIntoNewOne()
    {
        int p, q;
        byte[] b;
        ByteString[] o;

        for (int i = 0, j = 1, k = 1; i < N; j++, i += k, k = j * j)
        {
            k = Math.min(k, N - i);
            b = Arrays.copyOfRange(bytes, i, i + k);
            p = (int) Math.ceil(1.0 * k / j);
            o = new ByteString[p];

            for (q = 0; q < p; q++)
                o[q] = new ByteString(Arrays.copyOfRange(
                        b,
                        q * j,
                        Math.min(q * j + j, k)
                ));

            bs = new ByteString(o);
            Assert.assertArrayEquals(
                    b,
                    bs.getBytes()
            );
        }
    }
}
