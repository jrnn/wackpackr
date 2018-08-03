package wackpackr.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.Assert;
import org.junit.Test;

public class MinHeapTest
{
    @Test
    public void heapKnowsWhetherItIsEmpty()
    {
        MinHeap<Integer> h = new MinHeap<>();
        Assert.assertTrue(h.isEmpty());

        h.add(1337);
        Assert.assertFalse(h.isEmpty());

        h.pop();
        Assert.assertTrue(h.isEmpty());
    }

    @Test
    public void heapKnowsHowManyElementsItHas()
    {
        MinHeap<Integer> h = new MinHeap<>();
        Assert.assertEquals(0, h.size());

        for (int i = 1; i <= 100; i++)
        {
            h.add(i);
            Assert.assertEquals(i, h.size());
        }

        int i = 100;
        while (!h.isEmpty())
        {
            h.pop();
            i--;
            Assert.assertEquals(i, h.size());
        }

        Assert.assertEquals(0, h.size());
    }

    @Test
    public void peekDoesNotRemoveElementAtHead()
    {
        MinHeap<Integer> h = new MinHeap<>();

        for (int i : new int[]{1, 13, 42, 666, 1337})
            h.add(i);

        for (int i = 0; i < 10; i++)
            Assert.assertEquals(1, (int) h.peek());

        Assert.assertEquals(1, (int) h.pop());
    }

    @Test
    public void peekReturnsNullWhenHeapIsEmpty()
    {
        MinHeap<Integer> h = new MinHeap<>();
        Assert.assertNull(h.peek());

        for (int i = 0; i < 10; i++) h.add(i);
        Assert.assertNotNull(h.peek());

        while (!h.isEmpty()) h.pop();
        Assert.assertNull(h.peek());
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
        MinHeap<Integer> h1 = new MinHeap<>();
        MinHeap<String> h2 = new MinHeap<>();

        addFewThenPopFew(h1, new Integer[]{233, 144, 5, 21, 89}, new Integer[]{5, 21});
        addFewThenPopFew(h1, new Integer[]{2584, 1, 34, 3, 4181}, new Integer[]{1, 3});
        addFewThenPopFew(h1, new Integer[]{8, 2, 13, 377, 55}, new Integer[]{2, 8});
        addFewThenPopFew(h1, new Integer[]{987, 610, 1, 6765, 1597}, new Integer[]{1, 13});

        addFewThenPopFew(h2, new String[]{"never", "gonna", "give", "you", "up"}, new String[]{"give", "gonna"});
        addFewThenPopFew(h2, new String[]{"never", "gonna", "let", "you", "down"}, new String[]{"down", "gonna"});
        addFewThenPopFew(h2, new String[]{"never", "gonna", "run", "around"}, new String[]{"around", "gonna"});
        addFewThenPopFew(h2, new String[]{"and", "desert", "you"}, new String[]{"and", "desert", "let"});
    }

    private <T> void addRandomlyThenPopAll(Class<T> cls, T[] ts)
    {
        MinHeap<T> h = new MinHeap<>();
        List<T> l = Arrays.asList(Arrays.copyOf(ts, ts.length));

        Collections.shuffle(l);
        l.forEach(t -> h.add(t));

        for (T t : ts)
            Assert.assertEquals(h.pop(), t);
    }

    private <T> void addFewThenPopFew(MinHeap<T> h, T[] adds, T[] pops)
    {
        for (T t : adds)
            h.add(t);
        for (T t : pops)
            Assert.assertEquals(t, h.pop());
    }
}
