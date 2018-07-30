package wackpackr.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MinHeapTest
{
    @Test
    public void heapKnowsWhetherItIsEmpty()
    {
        MinHeap<Integer> h = new MinHeap<>();
        assertTrue(h.isEmpty());

        h.add(1337);
        assertFalse(h.isEmpty());

        h.pop();
        assertTrue(h.isEmpty());
    }

    @Test
    public void heapKnowsHowManyElementsItHas()
    {
        MinHeap<Integer> h = new MinHeap<>();
        assertEquals(0, h.size());

        for (int i = 1; i <= 100; i++)
        {
            h.add(i);
            assertEquals(i, h.size());
        }

        int i = 100;
        while (!h.isEmpty())
        {
            h.pop();
            i--;
            assertEquals(i, h.size());
        }

        assertEquals(0, h.size());
    }

    @Test
    public void peekDoesNotRemoveElementAtHead()
    {
        MinHeap<Integer> h = new MinHeap<>();

        for (int i : new int[]{1, 13, 42, 666, 1337})
            h.add(i);

        for (int i = 0; i < 10; i++)
            assertEquals(1, (int) h.peek());

        assertEquals(1, (int) h.pop());
    }

    @Test
    public void peekReturnsNullWhenHeapIsEmpty()
    {
        MinHeap<Integer> h = new MinHeap<>();
        assertNull(h.peek());

        for (int i = 0; i < 10; i++) h.add(i);
        assertNotNull(h.peek());

        while (!h.isEmpty()) h.pop();
        assertNull(h.peek());
    }

    @Test(expected = NullPointerException.class)
    public void addingNullThrowsException()
    {
        MinHeap<Integer> h = new MinHeap<>();
        h.add(null);
    }

    @Test(expected = NoSuchElementException.class)
    public void poppingEmptyHeapThrowsException()
    {
        MinHeap<Integer> h = new MinHeap<>();
        h.pop();
    }

    @Test
    public void elementsArePoppedInRightOrder()
    {
        Integer[] is = {1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610};
        String[] ss = {"all", "are", "base", "belong", "to", "us", "your"};

        byte b = 0;
        HuffNode[] ns = {
            new HuffNode(b, 1), new HuffNode(b, 2), new HuffNode(b, 3), new HuffNode(b, 5),
            new HuffNode(b, 8), new HuffNode(b, 13), new HuffNode(b, 21), new HuffNode(b, 34)
        };

        addRandomlyThenPopAll(Integer.class, is);
        addRandomlyThenPopAll(String.class, ss);
        addRandomlyThenPopAll(HuffNode.class, ns);
    }

    @Test
    public void popAlwaysGivesLeastElement()
    {
        MinHeap<Integer> h = new MinHeap<>();

        addFewThenPopFew(h, new int[]{233, 144, 5, 21, 89}, new int[]{5, 21});
        addFewThenPopFew(h, new int[]{2584, 1, 34, 3, 4181}, new int[]{1, 3});
        addFewThenPopFew(h, new int[]{8, 2, 13, 377, 55}, new int[]{2, 8});
        addFewThenPopFew(h, new int[]{987, 610, 1, 6765, 1597}, new int[]{1, 13});
    }

    private <T> void addRandomlyThenPopAll(Class<T> cls, T[] ts)
    {
        MinHeap<T> h = new MinHeap<>();
        List<T> l = Arrays.asList(Arrays.copyOf(ts, ts.length));

        Collections.shuffle(l);
        l.forEach(t -> h.add(t));

        for (T t : ts)
            assertEquals(h.pop(), t);
    }

    private void addFewThenPopFew(MinHeap<Integer> h, int[] adds, int[] pops)
    {
        for (int n : adds)
            h.add(n);

        for (int n : pops)
            assertEquals(n, (int) h.pop());
    }
}
