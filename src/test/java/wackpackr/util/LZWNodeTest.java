package wackpackr.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LZWNodeTest
{
    private final static int N = 5;
    private final static int U = 13;
    private final static Random RND = ThreadLocalRandom.current();

    private int count;
    private LZWNode root;
    private Set<Byte> bytes;

    @Before
    public void before()
    {
        count = 0;
        root = new LZWNode(count++, (byte) -1);
        bytes = new HashSet<>();

        while (bytes.size() < N)
            bytes.add((byte) ((RND.nextInt() % U + U) % U));

        putHelper(true);
    }

    @Test
    public void getReturnsNodeIfByteSequenceExistsAndNullOtherwise()
    {
        getHelper();
    }

    @Test
    public void cannotInsertDuplicateByteSequences()
    {
        putHelper(false);
    }

    @Test
    public void canRetrieveIndexAssociatedToByteString()
    {
        count = 1;
        indexHelper();
    }

    private void getHelper(byte... bs)
    {
        int n = bs.length;

        if (n >= N)
            return;

        for (byte b = 0; b < U; b++)
        {
            boolean expected = bytes.contains(b);
            byte[] bs_ = new byte[n + 1];

            for (int i = 0; i < n; i++)
            {
                bs_[i] = bs[i];
                if (!bytes.contains(bs[i]))
                    expected = false;
            }

            bs_[n] = b;
            LZWNode node = root.get(bs_);

            if (expected)
                Assert.assertNotNull(node);
            else
                Assert.assertNull(node);

            getHelper(bs_);
        }
    }

    private void putHelper(boolean expected, byte... bs)
    {
        int n = bs.length;

        if (n >= N)
            return;

        bytes.forEach(b -> {
            Assert.assertEquals(
                    expected,
                    root.insert(new LZWNode(count++, b), bs)
            );
            byte[] bs_ = new byte[n + 1];
            System.arraycopy(bs, 0, bs_, 0, n);
            bs_[n] = b;

            putHelper(expected, bs_);
        });
    }

    private void indexHelper(byte... bs)
    {
        int n = bs.length;

        if (n >= N)
            return;

        for (byte b = 0; b < U; b++)
        {
            byte[] bs_ = new byte[n + 1];
            System.arraycopy(bs, 0, bs_, 0, n);
            bs_[n] = b;

            LZWNode node = root.get(bs_);

            if (node != null)
                Assert.assertEquals(
                        count++,
                        node.index()
                );

            indexHelper(bs_);
        }
    }
}
