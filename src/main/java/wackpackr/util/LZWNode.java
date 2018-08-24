package wackpackr.util;

/**
 * Node in a trie-like structure used in LZW encoding.
 *
 * <p>A node represents a dictionary entry: an index, and a unique byte sequence associated to that
 * index. Each node stores the <b>index</b> number as-is, and the <b>last byte value</b> in the
 * corresponding sequence.</p>
 *
 * <p>As each node stores only one byte, the sequences are built by walking down the trie, starting
 * from the root and concatenating bytes encountered along the way. In other words, each node stores
 * a certain byte sequence by virtue of its position in the trie: and, by extension, that sequence
 * is a prefix to longer sequences stored by consequent nodes down the trie.</p>
 *
 * <p>Nodes have two kinds of pointers: child pointers that lead further down the trie; and lateral
 * pointers that chain nodes of the same depth, much like a linked list. The child pointers ({@code
 * left} and {@code right}) lead to nodes with the exact same prefix as the current node, but a
 * different byte value at the last position; the lateral pointer ({@code next}), in turn, leads to
 * the "first" node that takes as its prefix the byte sequence represented by the current node. The
 * idea is that the child pointers are followed until the byte value to append next is found; and
 * then the lateral pointer is followed to move on to the next position in the sequence.</p>
 *
 * <p>For example: to check if sequence [ 1, 2, 3, 4 ] exists in the trie, starting from the root,
 * first follow the {@code left} and {@code right} pointers, until reaching a node with value 1;
 * then from there follow the {@code next} pointer to a parallel node and, again, follow the {@code
 * left} and {@code right} pointers, until reaching a node with value 2; and so on.</p>
 *
 * <p>The child pointers are arranged as a non-balanced binary trie. So, at worst, checking for the
 * existence of a byte value in a certain position requires 256 operations. However, in practice
 * byte values are added to the (sub)trie randomly, which often results in a surprisingly balanced
 * structure. Hence, the search typically takes only logarithmic time.</p>
 *
 * <p>The lateral pointers simply chain nodes in a linear fashion, so tracing a long sequence can
 * be very time-consuming. However, there's a trick when operating the dictionary that avoids ever
 * having to look further than one {@code next} pointer, so optimising would make no difference.
 * Also, for the same reason, currently this class offers only methods where the prefix is known,
 * and searching/inserting is limited to the immediate next byte.</p>
 *
 * @author Juho Juurinen
 */
public class LZWNode
{
    private final int index;
    private final byte value;
    private LZWNode next = null, left = null, right = null;

    /**
     * Constructs a new node in an LZW trie, representing a dictionary entry with the given index,
     * and a byte sequence whose last byte is the given value.
     *
     * @param index LZW dictionary index
     * @param value last byte in the sequence associated to this index
     */
    public LZWNode(int index, byte value)
    {
        this.index = index;
        this.value = value;
    }

    /**
     * Searches for a node whose byte sequence is the byte sequence associated with this node
     * <b>plus</b> the given bytes appended to the end; returns that node if it exists, or {@code
     * null} otherwise.
     *
     * @param bytes bytes to append to the sequence represented by this node
     * @return sought-after node, if it exists
     */
    public LZWNode get(byte... bytes)
    {
        int n = bytes.length;

        switch (n)
        {
            case 0:
                return null;
            case 1:
                return search(bytes[0], next);
            default:
                LZWNode node = search(bytes[0], next);
                if (node == null)
                    return null;

                byte[] bytes_ = new byte[n - 1];
                System.arraycopy(bytes, 1, bytes_, 0, n - 1);

                return node.get(bytes_);
        }
    }

    /**
     * Inserts the given node to the trie, such that the new node represents the byte sequence that
     * equals to the sequence represented by this node appended with the given bytes.
     *
     * <p>Returns {@code true} if insertion was successful. Insertion fails if any of the prefixes
     * of the given sequence do not exist, or if the given sequence already exists in the trie.</p>
     *
     * @param node node to insert
     * @param bytes
     * @return
     */
    public boolean insert(LZWNode node, byte... bytes)
    {
        if (bytes.length == 0)
            return insert(node);

        LZWNode parent = get(bytes);
        return (parent == null)
                ? false
                : parent.insert(node);
    }

    /**
     * Returns the dictionary index stored in this node.
     *
     * @return dictionary index of this node
     */
    public int index()
    {
        return index;
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


    private LZWNode search(byte value, LZWNode root)
    {
        if (root == null)
            return null;

        if (value == root.value)
            return root;

        root = (value < root.value)
                ? root.left
                : root.right;

        return search(value, root);
    }

    private boolean insert(LZWNode node)
    {
        if (next != null)
            return insert(node, next);

        next = node;
        return true;
    }

    private boolean insert(LZWNode node, LZWNode root)
    {
        if (node.value == root.value)
            return false;

        if (node.value < root.value)
            if (root.left == null)
                root.left = node;
            else
                return insert(node, root.left);
        else
            if (root.right == null)
                root.right = node;
            else
                return insert(node, root.right);

        return true;
    }
}
