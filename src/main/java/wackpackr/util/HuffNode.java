package wackpackr.util;

import java.util.ArrayDeque;

/**
 * Node in a Huffman code tree. Ordered by weight from lightest to heaviest.
 *
 * @author Juho Juurinen
 */
public class HuffNode implements Comparable<HuffNode>
{
    public int value;
    public final long weight;
    public final HuffNode left;
    public final HuffNode right;

    /**
     * Leaf constructor, used when initialising single-node trees.
     *
     * @param value
     * @param weight
     */
    public HuffNode(int value, long weight)
    {
        this.value = value;
        this.weight = weight;
        this.left = this.right = null;
    }

    /**
     * Internal node constructor, used when combining "lesser" trees. Value is set as -1 to
     * indicate non-leaf node. Weight is calculated directly from nodes given as parameter.
     *
     * @param left
     * @param right
     */
    public HuffNode(HuffNode left, HuffNode right)
    {
        assert left != null && right != null;

        this.left = left;
        this.right = right;
        this.value = -1;
        this.weight = left.weight + right.weight;
    }

    public boolean isLeaf()
    {
        if (value < 0)
            return false;

        return (left == null && right == null);
    }

    @Override
    public int compareTo(HuffNode o)
    {
        return Long.compare(weight, o.weight);
    }


    /* --- BELOW JUST SOME TEMPORARY BULLSHIT METHODS FOR DEBUGGING PURPOSES --- */

    
    @Override
    public String toString()
    {
        return "[" + value + ", " + weight + "]";
    }

    public static void printTree(HuffNode root)
    {
        HuffNode node;
        ArrayDeque<HuffNode> P = new ArrayDeque<>();
        ArrayDeque<HuffNode> Q = new ArrayDeque<>();
        P.offer(root);
        while (!P.isEmpty())
        {
            System.out.print("      ");
            while (!P.isEmpty())
                Q.offer(P.poll());
            while (!Q.isEmpty())
            {
                node = Q.poll();
                System.out.print("  " + node);
                if (!node.isLeaf())
                {
                    P.offer(node.left);
                    P.offer(node.right);
                }
            }
            System.out.println();
        }
    }
}
