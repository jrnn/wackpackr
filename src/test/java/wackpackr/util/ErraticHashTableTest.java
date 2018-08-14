package wackpackr.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ErraticHashTableTest
{
    private final Random RANDOM = ThreadLocalRandom.current();
    private ErraticHashTable<Integer> ht;

    @Before
    public void before()
    {
        ht = new ErraticHashTable<>(6151);
    }

    @Test
    public void canGetElementsWithByteSequenceOfVariableLength()
    {
        byte[] key = new byte[3];

        for (int i = 0; i < 4096; i++)
        {
            RANDOM.nextBytes(key);
            ht.put(RANDOM.nextInt(), key);
        }
        for (int i = 1; i <= 100; i++)
        {
            key = new byte[i];
            RANDOM.nextBytes(key);
            Assert.assertNotNull(ht.get(key));
        }
    }

    @Test
    public void canPutElementsWithByteSequenceOfVariableLength()
    {
        for (int i = 1; i <= 1000; i++)
        {
            byte[] key = new byte[i];
            RANDOM.nextBytes(key);
            ht.put(RANDOM.nextInt(), key);
        }
    }

    @Test
    public void getReturnsEmptyListIfKeyHashesToEmptyBucket()
    {
        Assert.assertTrue(ht.get((byte) -1).isEmpty());
    }

    @Test
    public void getReturnsRightElementButNotNecessarilyOnlyThatElement()
    {
        int k = 0;
        byte[][] keys = new byte[3][16];
        Map<ByteTriple, Integer> pairs = new HashMap<>();

        for (int i = 0; i < 3; i++)
            RANDOM.nextBytes(keys[i]);

        for (Byte a : keys[0])
            for (Byte b : keys[1])
                for (Byte c : keys[2])
                {
                    ht.put(k, a, b, c);
                    pairs.putIfAbsent(new ByteTriple(a, b, c), k);
                    k++;
                }

        for (Byte a : keys[0])
            for (Byte b : keys[1])
                for (Byte c : keys[2])
                {
                    int i = pairs.get(new ByteTriple(a, b, c));

                    Assert.assertTrue(ht.get(a, b, c).size() >= 1);
                    Assert.assertTrue(ht.get(a, b, c).contains(i));
                }
    }

    @Test
    public void nullElementsArePermitted()
    {
        for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++)
            ht.put(null, b, b, b);

        for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++)
            Assert.assertTrue(ht
                    .get(b, b, b)
                    .contains(null)
            );
    }

    @Test
    public void dispersionIsGoodGivenOptimalTableSize()
    {
        byte[] key = new byte[3];
        double[] loadFactors = { 0.75, 1.50, 2.25, 3.00, 3.75 };
        int[] nicePrimes = { 193, 383, 769, 1531, 3067, 6143, 12289, 24571 };

        for (int prime : nicePrimes)
            for (double loadFactor : loadFactors)
            {
                int elems = (int) (prime * loadFactor);
                double x = 0;

                for (int i = 0; i < 10; i++)
                {
                    ht = new ErraticHashTable<>(prime);

                    for (int k = 0;  k < elems; k++)
                    {
                        RANDOM.nextBytes(key);
                        ht.put(1, key);
                    }

                    x += ht.calculateVMR();
                }

                Assert.assertTrue(
                        0.9 <= (x / 10) && (x / 10) <= 1.1
                );
            }
    }

    private final class ByteTriple
    {
        private final byte[] bs;

        ByteTriple(byte a, byte b, byte c)
        {
            this.bs = new byte[]{ a, b, c };
        }

        byte[] get()
        {
            return bs;
        }

        @Override
        public boolean equals(Object o)
        {
            if (o == null || o.getClass() != getClass())
                return false;

            byte[] bt = ((ByteTriple) o).get();
            return (bs[0] == bt[0] &&
                    bs[1] == bt[1] &&
                    bs[2] == bt[2]);
        }

        @Override
        public int hashCode()
        {
            return new String(bs).hashCode();
        }
    }
}
