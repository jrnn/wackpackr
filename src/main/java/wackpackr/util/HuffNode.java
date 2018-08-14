package wackpackr.util;

import java.util.ArrayDeque;

/**
 * Node in a Huffman code tree. Implements {@link Comparable} interface so that natural ordering is
 * by weight, from smallest to largest.
 *
 * <p>Three types of Nodes are differentiated: (1) internal nodes, which always have exactly two
 * child nodes; (2) leaf nodes, which always have exactly zero children; (3) a special pseudo-EoF
 * node, which is also a leaf node. In practice the type is decided at instantiation, i.e. there is
 * a separate constructor for each.</p>
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
     * Constructs a new leaf node. Used when creating single-node trees before combining them into a
     * Huffman tree.
     *
     * @param value byte value associated with this node
     * @param weight number of occurrences of this byte value in processed file
     */
    public HuffNode(byte value, long weight)
    {
        this.value = value;
        this.weight = weight;
        this.left = this.right = null;
    }

    /**
     * Constructs a new internal node. Used when building a Huffman tree by combining "lesser"
     * trees.
     *
     * <p>Weight is calculated directly as the sum of children's weights. Neither of the child nodes
     * can be {@code null}.</p>
     *
     * <p>The subtrees starting from both child nodes should contain only byte values with lower
     * frequencies than leaf nodes further up the tree.</p>
     *
     * @param left pointer to root node of left subtree
     * @param right pointer to root node of right subtree
     * @throws IllegalArgumentException if trying to pass null as child node
     */
    public HuffNode(HuffNode left, HuffNode right)
    {
        if (left == null || right == null)
            throw new IllegalArgumentException("Internal nodes must have exactly two children");

        this.value = 0;
        this.weight = left.getWeight() + right.getWeight();
        this.left = left;
        this.right = right;
    }

    /**
     * Constructs a new pseudo-EoF node. Used exactly once when building a Huffman tree from a
     * frequency table.
     *
     * <p>The node's weight is set as negative to ensure that it takes a relatively low position in
     * the resultant tree.</p>
     */
    public HuffNode()
    {
        this.value = 0;
        this.weight = -1;
        this.left = this.right = null;
        this.eof = true;
    }

    /**
     * Sets node as a pseudo-EoF marker, if it is a leaf node. This breaks the pattern of
     * distinguishing node types with constructors, but is needed when decoding Huffman trees from
     * compressed binary (including this information directly in the encoded form would compromise
     * efficiency).
     */
    public void setEoF()
    {
        if (isLeaf())
            this.eof = true;
    }

    @Override
    public int compareTo(HuffNode o)
    {
        return Long.compare(weight, o.getWeight());
    }


    /*------BELOW JUST VANILLA GETTERS AND SETTERS------*/


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
