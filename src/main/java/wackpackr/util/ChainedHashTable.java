package wackpackr.util;

/**
 * Limited application of a hash table for storing key-value mappings, specifically using byte
 * sequences (of arbitrary length) as keys.
 *
 * Hash collisions are handled with separate chaining, i.e. elements whose key hashes to the same
 * bucket are stored sequentially in a linked list.
 *
 * The backing array is not dynamically resized. Hash table size must be given at instantiation, and
 * remains fixed thereafter. For this reason, this class should not be used unless the maximum
 * number of elements is definitely known in advance, so that the size can be set optimally.
 *
 * For hashing, the technique used in {@link String} is blatantly copied, because (based on some
 * quick testing) it results in a very uniform distribution. So, as long as table size is sensibly
 * set, basic operations of adding, searching, and removing should work in constant time.
 *
 * @author Juho Juurinen
 * @param <E> the class of the values stored in a hash table instance
 */
public class ChainedHashTable<E>
{
    /**
     * Underlying array whose indexes connect hash values to lists where elements of a certain key
     * are stored.
     */
    private final CircularDoublyLinkedList<E>[] buckets;

    /**
     * Number of buckets: that is, size of the backing array. Used as modulo when hashing.
     */
    private final int size;

    /**
     * Constructs a new, empty hash table. Number of buckets (i.e. table size) must be given, and
     * is not resized at any later point. Assumption is that caller knows the maximum number of
     * elements to be stored at once, and knows how to pick an optimal size based on the load factor
     * they aim to have (hint: preferably a prime number not close to any power of two).
     *
     * @param size number of buckets in hash table
     */
    public ChainedHashTable(int size)
    {
        this.size = size;
        this.buckets = new CircularDoublyLinkedList[size];
    }

    /**
     * Returns an array containing all elements that associate to the given key's hash value. If no
     * such elements exist, returns an empty array.
     *
     * @param key byte sequence of arbitrary length
     * @return values associated to the given key's hash
     */
    public Object[] getValues(byte... key)
    {
        return safeGet(hash(key)).toArray();
    }

    /**
     * Inserts given value to hash table, to a bucket determined by the given key's hash value.
     *
     * @param value element to store into hash table
     * @param key byte sequence associated to element to be inserted
     */
    public void insert(E value, byte... key)
    {
        safeGet(hash(key)).insert(value);
    }

    /**
     * Deletes the "oldest" element associated to the given key's hash value.
     *
     * Note that this is a dangerous method, because there's no guarantee which element exactly is
     * deleted due to hash collisions. Use only if you understand the implications.
     *
     * @param key byte sequence of arbitrary length
     */
    public void deleteOldest(byte... key)
    {
        safeGet(hash(key)).removeLast();
    }


    /* --- Private helper methods below, no comments or description given. --- */


    private int hash(byte... key)
    {
        int h = 0;

        for (byte b : key)
            h = 31 * h + b;

        return (h % size + size) % size;
    }

    private CircularDoublyLinkedList<E> safeGet(int i)
    {
        if (buckets[i] == null)
            buckets[i] = new CircularDoublyLinkedList<>();

        return buckets[i];
    }
}
