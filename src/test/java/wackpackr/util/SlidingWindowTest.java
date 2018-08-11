package wackpackr.util;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class SlidingWindowTest
{
    private static final int SIZE = 1000;
    private static final Random RANDOM = ThreadLocalRandom.current();

    private SlidingWindow<Integer> sw;

    @Before
    public void before()
    {
        sw = new SlidingWindow<>(SIZE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void windowSizeMustBeAtLeastOne()
    {
        SlidingWindow<Integer> impossible = new SlidingWindow<>(0);
        impossible.insert(1);
    }

    @Test
    public void insertReturnsNullUntilWindowIsFull()
    {
        for (int i = 0; i < SIZE; i++)
            Assert.assertNull(sw.insert(1337));

        Assert.assertNotNull(sw.insert(1337));
    }

    @Test
    public void ifWindowIsFullThenElementsAreDumpedFIFO()
    {
        int[] elems = new int[SIZE * 5];
        for (int i = 0; i < SIZE * 5; i++)
            elems[i] = RANDOM.nextInt();

        for (int i = 0; i < SIZE; i++)
            sw.insert(elems[i]);

        for (int i = 0; i < SIZE * 3; i++)
            Assert.assertEquals(
                    elems[i],
                    (int) sw.insert(elems[i + SIZE])
            );
    }

    @Test
    public void readPointerIsAtZeroByDefault()
    {
        Assert.assertTrue(sw.cursor() == 0);
    }

    @Test
    public void readPointerCanBeMovedBackwardAndForward()
    {
        int position = 1;
        for (int i = 0; i < SIZE; i++)
            sw.insert(1337);

        sw.move();
        Assert.assertTrue(sw.cursor() == position);

        for (int offset : new int[]{ 1, 2, 3, -4, 5, 6, -7, 8, 9, -10 })
        {
            position += offset;
            sw.move(offset);

            Assert.assertEquals(
                    sw.cursor(),
                    position
            );
        }
    }

    @Test
    public void canReadAtDifferentOffsets()
    {
        int[] elems = new int[SIZE * 5];

        for (int i = 0; i < SIZE; i++)
        {
            int elem = RANDOM.nextInt();
            elems[i] = elem;
            sw.insert(elem);
        }

        sw.move(SIZE / 2);
        int position = sw.cursor();
        Assert.assertTrue(sw.read() == elems[position]);

        for (int offset : new int[]{ 1, 2, 3, -4, 5, 6, -7, 8, 9, -10 })
            Assert.assertEquals(
                    (int) sw.read(offset),
                    elems[position + offset]
            );
    }

    @Test
    public void readingBeyondWindowThrowsException()
    {
        int exceptions = 0;
        sw.insert(1337);

        for (int offset : new int[]{ -1, 2 })
            if (throwsExpectedExceptionOnRead(
                    offset,
                    IndexOutOfBoundsException.class))
                exceptions++;

        for (int i = 0; i < SIZE; i++)
            sw.insert(1337);

        for (int offset : new int[]{ -SIZE, SIZE + 2 })
            if (throwsExpectedExceptionOnRead(
                    offset,
                    IndexOutOfBoundsException.class))
                exceptions++;

        Assert.assertTrue(exceptions == 4);
    }

    @Test
    public void movingCursorBeyondWindowThrowsException()
    {
        int exceptions = 0;
        sw.insert(1337);

        for (int offset : new int[]{ -1, 1 })
            if (throwsExpectedExceptionOnMove(
                    offset,
                    IndexOutOfBoundsException.class))
                exceptions++;

        for (int i = 0; i < SIZE; i++)
            sw.insert(1337);

        for (int offset : new int[]{ -SIZE, SIZE + 1 })
            if (throwsExpectedExceptionOnMove(
                    offset,
                    IndexOutOfBoundsException.class))
                exceptions++;

        Assert.assertTrue(exceptions == 4);
    }

    private boolean throwsExpectedExceptionOnRead(int offset, Class cls)
    {
        try
        {
            sw.read(offset);
        }
        catch (Exception e)
        {
            return e.getClass() == cls;
        }

        return false;
    }

    private boolean throwsExpectedExceptionOnMove(int offset, Class cls)
    {
        try
        {
            sw.move(offset);
        }
        catch (Exception e)
        {
            return e.getClass() == cls;
        }

        return false;
    }
}
