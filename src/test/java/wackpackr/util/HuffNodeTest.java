package wackpackr.util;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class HuffNodeTest
{
    private static final Stack<HuffNode> LEAF_NODES = new Stack<>();
    private static final Random RANDOM = ThreadLocalRandom.current();

    @BeforeClass
    public static void before()
    {
        for (int i = 0; i < 999; i++)
            LEAF_NODES.push(new HuffNode(
                    (byte) RANDOM.nextInt(),
                    RANDOM.nextInt())
            );
    }

    @Test
    public void nodesCreatedWithLeafNodeConstructorsHaveNoChildren()
    {
        LEAF_NODES.push(new HuffNode());

        for (int i = 0; i < 100; i++)
        {
            HuffNode node = LEAF_NODES.pop();

            Assert.assertNull(node.getLeft());
            Assert.assertNull(node.getRight());
            Assert.assertTrue(node.isLeaf());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateLeafNodeWithoutChildren()
    {
        HuffNode node = new HuffNode(
                null,
                null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateLeafNodeWithOnlyLeftChild()
    {
        HuffNode node = new HuffNode(
                LEAF_NODES.pop(),
                null
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotCreateLeafNodeWithOnlyRightChild()
    {
        HuffNode node = new HuffNode(
                null,
                LEAF_NODES.pop()
        );
    }

    @Test
    public void internalNodeWeightIsSumOfItsChildrensWeights()
    {
        HuffNode node, left, right;
        ArrayDeque<HuffNode> queue = new ArrayDeque<>();
        queue.offer(new HuffNode());

        for (int i = 0; i < 100; i++)
            queue.offer(LEAF_NODES.pop());

        while (queue.size() > 1)
        {
            left = queue.poll();
            right = queue.poll();
            node = new HuffNode(left, right);

            Assert.assertEquals(
                    node.getWeight(),
                    left.getWeight() + right.getWeight()
            );
            queue.offer(node);
        }
    }

    @Test
    public void onlyEofNodeConstructorSetsEofAsTrue()
    {
        Assert.assertTrue(new HuffNode().isEoF());
        Assert.assertFalse(LEAF_NODES.pop().isEoF());
        Assert.assertFalse(getInternalNode().isEoF());
    }

    @Test
    public void onlyLeafNodesCanBeSetAsEof()
    {
        HuffNode node;
        LEAF_NODES.push(new HuffNode());

        for (int i = 0; i < 10; i++)
        {
            node = LEAF_NODES.pop();
            node.setEoF();
            Assert.assertTrue(node.isEoF());
        }

        node = getInternalNode();
        node.setEoF();
        Assert.assertFalse(node.isEoF());
    }

    @Test
    public void nodesAreOrderedByWeightFromSmallestToLargest()
    {
        HuffNode[] nodes = new HuffNode[200];

        for (int i = 0; i < 100; i++)
            nodes[i] = LEAF_NODES.pop();
        for (int i = 100; i < 200; i++)
            nodes[i] = getInternalNode();

        Arrays.sort(nodes);
        for (int i = 0, j = 1; j < 200; i++, j++)
            Assert.assertFalse(nodes[i].getWeight() > nodes[j].getWeight());
    }

    private HuffNode getInternalNode()
    {
        return new HuffNode(
                LEAF_NODES.pop(),
                LEAF_NODES.pop()
        );
    }
}
