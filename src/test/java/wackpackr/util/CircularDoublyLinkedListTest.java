package wackpackr.util;

import java.util.concurrent.ThreadLocalRandom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CircularDoublyLinkedListTest
{
    private final int N = 100;
    private int[] elems;
    private CircularDoublyLinkedList<Integer> L;

    @Before
    public void before()
    {
        L = new CircularDoublyLinkedList<>();
        elems = ThreadLocalRandom.current().ints(N).toArray();
    }

    @Test
    public void listKnowsWhetherItIsEmpty()
    {
        Assert.assertTrue(L.isEmpty());

        L.add(1337);
        Assert.assertFalse(L.isEmpty());

        L.removeFirst();
        L.removeLast();
        Assert.assertTrue(L.isEmpty());
    }

    @Test
    public void listKnowsHowManyElementsItHas()
    {
        Assert.assertTrue(L.size() == 0);

        for (int i = 1; i <= N; i++)
        {
            L.add(i);
            Assert.assertTrue(L.size() == i);
        }
        for (int i = N; i > 0; i--)
        {
            Assert.assertTrue(L.size() == i);
            L.remove(i);
        }

        Assert.assertTrue(L.size() == 0);
    }

    @Test
    public void listKnowsWhetherItContainsCertainElement()
    {
        int i, k, n = 10;

        for (k = 1; k <= n; k++)
        {
            for (i = 1; i <= n; i++)
                if (i < k)
                    Assert.assertTrue(L.contains(i));
                else
                    Assert.assertFalse(L.contains(i));

            for (i = 0; i < k; i++)
                L.add(k);
        }

        for (k = 1; k <= n; k++)
            for (i = 0; i < n / 2; i++)
                L.remove(k);

        for (k = 1; k <= n; k++)
            if (k <= n / 2)
                Assert.assertFalse(L.contains(k));
            else
                Assert.assertTrue(L.contains(k));
    }

    @Test
    public void elementsAreAddedToEndOfListAndRetrievableInThatOrder()
    {
        for (int i = 0; i < N; i++)
        {
            L.add(elems[i]);

            for (int k = 0; k < L.size(); k++)
                Assert.assertTrue(L.get(k) == elems[k]);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getThrowsExceptionIfNegativeIndex()
    {
        L.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getThrowsExceptionIfIndexGreaterThanListSize()
    {
        for (int i = 0; i < N; i++)
            L.add(i);

        L.get(N);
    }

    @Test
    public void canAddAndRemoveNullElements()
    {
        Assert.assertFalse(L.contains(null));

        for (int i = 0; i < N; i++)
        {
            L.add(null);
            Assert.assertTrue(L.contains(null));
        }
        for (int i = 0; i < N; i++)
        {
            Assert.assertTrue(L.contains(null));
            L.add(i);
            L.remove(null);
        }

        Assert.assertFalse(L.contains(null));
    }

    @Test
    public void removeDoesNothingIfElementNotPresent()
    {
        int i = 0;

        for (; i < N / 2; i++)
            L.add(i);

        Object[] before = L.toArray();

        for (; i < N; i++)
            L.remove(i);

        Assert.assertArrayEquals(
                before,
                L.toArray()
        );
    }

    @Test
    public void removeDoesNothingIfListIsEmpty()
    {
        Object[] before = L.toArray();

        for (int i = 0; i < N; i++)
        {
            L.remove(i);
            L.removeFirst();
            L.removeLast();
        }

        Assert.assertArrayEquals(
                before,
                L.toArray()
        );
    }

    @Test
    public void removeFirstOccurrenceOnly()
    {
        int[] ints = { 4, 2, 1, 3, 5, 2, 1, 10, 8, 7, 8, 10, 4, 7, 3, 9, 6, 6, 5, 9 };

        for (int i : ints)
            L.add(i);

        for (int i = 1; i <= 10; i++)
            L.remove(i);

        for (int i = 1; i <= 10; i++)
            Assert.assertTrue(L.contains(i));

        Assert.assertArrayEquals(
                L.toArray(),
                new Object[]{ 2, 1, 8, 10, 4, 7, 3, 6, 5, 9 }
        );
    }

    @Test
    public void removeFirstAlwaysRemovesElementAtBeginningOfList()
    {
        for (int i = 0; i < N; i++)
            L.add(elems[i]);

        for (int i = 0; i < N; i++)
        {
            Assert.assertTrue(L.get(0) == elems[i]);
            L.removeFirst();
        }
    }

    @Test
    public void removeLastAlwaysRemovesElementAtEndOfList()
    {
        for (int i = 0; i < N; i++)
            L.add(elems[i]);

        while (!L.isEmpty())
        {
            int i = L.size() - 1;
            Assert.assertTrue(L.get(i) == elems[i]);
            L.removeLast();
        }
    }

    @Test
    public void toArrayWorksCorrectly()
    {
        Object[] elemsAsObject = new Object[N];

        for (int i = 0; i < N; i++)
        {
            L.add(elems[i]);
            elemsAsObject[i] = elems[i];
        }

        Assert.assertArrayEquals(
                L.toArray(),
                elemsAsObject
        );
    }

    @Test
    public void toArrayReverseWorksCorrectly()
    {
        Object[] elemsAsObject = new Object[N];

        for (int i = 0; i < N; i++)
        {
            L.add(elems[i]);
            elemsAsObject[i] = elems[N - i - 1];
        }

        Assert.assertArrayEquals(
                L.toArrayReverse(),
                elemsAsObject
        );
    }
}
