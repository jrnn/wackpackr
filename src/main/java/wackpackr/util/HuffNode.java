package wackpackr.util;

import java.util.ArrayDeque;

/**
 * Node in a Huffman code tree. Implements {@link Comparable} interface so that natural ordering is
 * by weight, from lightest to heaviest.
 *
 * Three types of Nodes need to be differentiated: (1) internal nodes, which always have exactly two
 * child nodes; (2) leaf nodes, which always have exactly zero children; (3) a special pseudo-EoF
 * node, which is also a leaf node. In practice the type is decided at instantiation, i.e. there is
 * a separate constructor for each.
 *
 * @author Juho Juurinen
 */
public class HuffNode implements Comparable<HuffNode>
{
    private final byte value;
    private final long weight;
    private final HuffNode left;
    private final HuffNode right;
    private boolean eof = false;

    /**
     * Constructor for leaf nodes. Used when initialising single-node trees before combining them
     * into a Huffman tree.
     *
     * @param value - byte value associated with this node
     * @param weight - number of occurrences of {@param value} in processed file
     */
    public HuffNode(byte value, long weight)
    {
        this.value = value;
        this.weight = weight;
        this.left = this.right = null;
    }

    /**
     * Constructor for internal nodes. Used when building a Huffman tree by combining "lesser"
     * trees. Weight is calculated directly as the sum of children's weights. Neither of the child
     * nodes can be {@code null}.
     *
     * @param left - root Node of a subtree whose leaf nodes contain only byte values with lower
     *               frequencies than leaf nodes further up from this node
     * @param right - root Node of a subtree whose leaf nodes contain only byte values with lower
     *                frequencies than leaf nodes further up from this node
     */
    public HuffNode(HuffNode left, HuffNode right)
    {
        assert left != null && right != null;

        this.value = 0;
        this.weight = left.weight + right.weight;
        this.left = left;
        this.right = right;
    }

    /**
     * Constructor for Pseudo-EoF node. Used exactly once when building a Huffman tree from a
     * frequency table. The node's weight is set as negative to ensure that it takes a relatively
     * low position in the tree.
     */
    public HuffNode()
    {
        this.value = 0;
        this.weight = -1;
        this.left = this.right = null;
        this.eof = true;
    }

    public boolean isLeaf()
    {
        return (left == null && right == null);
    }

    public byte getValue()
    {
        return value;
    }

    public long getWeight()
    {
        return weight;
    }

    public HuffNode getLeft()
    {
        return left;
    }

    public HuffNode getRight()
    {
        return right;
    }

    public boolean isEoF()
    {
        return eof;
    }

    /**
     * Sets node as a pseudo-EoF marker. This breaks the pattern of distinguishing node types with
     * constructors, but is needed when decoding Huffman trees from compressed binary (including
     * this information directly in the encoded form would compromise efficiency).
     */
    public void setEoF()
    {
        this.eof = true;
    }

    @Override
    public int compareTo(HuffNode o)
    {
        return Long.compare(weight, o.getWeight());
    }


    /* --- BELOW JUST SOME TEMPORARY BULLSHIT METHODS FOR DEBUGGING PURPOSES --- */

    
    @Override
    public String toString()
    {
        return isEoF()
                ? "[ EOF ]"
                : "[" + value + ", " + weight + "]";
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
