package wackpackr.util;

/**
 * Node in a Huffman code tree. Ordered by weight from lightest to heaviest.
 *
 * @author Juho Juurinen
 */
public class HuffNode implements Comparable<HuffNode>
{
    public final int value;
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
        return value > -1;
    }

    @Override
    public int compareTo(HuffNode o)
    {
        return Long.compare(weight, o.weight);
    }
}
