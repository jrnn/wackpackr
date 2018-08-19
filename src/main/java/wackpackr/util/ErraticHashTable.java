package wackpackr.util;

/**
 * Very, very limited and "rogue" application of a hash table that distributes elements into buckets
 * by the hash value of their accompanying key, but then tosses the key out the window. This results
 * in some abnormal behaviour: while it is guaranteed that a stored element can be retrieved with
 * the key it is associated with, there is no guarantee that the key retrieves <em>only</em> this
 * element — rather, more than one element whose keys just happen to hash to the same bucket may
 * also be retrieved at the same time.
 *
 * <p>While the elements can be of any type, and {@code null} elements are permitted, specifically
 * only byte sequences (of arbitrary length) are accepted as keys.</p>
 *
 * <p>Only two methods are offered: (1) adding an element with a certain key; and (2) retrieving all
 * elements whose keys hash to the same bucket (as described above).</p>
 *
 * <p>The backing array is not dynamically resized. Hash table size must be given at instantiation,
 * and remains fixed thereafter. For this reason, this class should not be used unless the maximum
 * number of elements is definitely known in advance, so that the size can be set optimally.</p>
 *
 * <p>All things considered, due to its erratic behaviour and laughably limited functionality, this
 * class should not be used unless the caller understands exactly what they're signing up for.</p>
 *
 * <p>For hashing, the technique used in {@link String} is blatantly copied, because it gives a
 * very uniform distribution with random byte sequences. So, if table size is sensibly set, the
 * operations work in constant time.</p>
 *
 * @author Juho Juurinen
 * @param <E> the class of elements stored in a hash table instance
 */
public class ErraticHashTable<E>
{
    private final CircularDoublyLinkedList<E>[] buckets;
    private final int size;

    /**
     * Constructs a new, empty hash table. Number of buckets (table size) must be given, and is not
     * resized at any later point. Assumption is that caller knows the maximum number of elements to
     * be stored at once, and knows how to pick an optimal size based on the preferred load factor.
     *
     * @param size number of buckets in hash table
     */
    public ErraticHashTable(int size)
    {
        this.size = size;
        this.buckets = new CircularDoublyLinkedList[size];
    }

    /**
     * Returns a list of all elements whose keys hash to the same bucket as the given key. If no
     * such elements exist, returns an empty list.
     *
     * @param key byte sequence of arbitrary length
     * @return elements in the bucket determined by the hash value of the given key
     */
    public CircularDoublyLinkedList<E> get(byte... key)
    {
        return safeGet(hash(key));
    }

    /**
     * Adds given element to a bucket determined by the hash value of the associated key.
     *
     * @param e element to add to hash table
     * @param key byte sequence of arbitrary length
     */
    public void put(E e, byte... key)
    {
        safeGet(hash(key)).add(e);
    }

    /**
     * Calculates the variance-to-mean ratio (VMR) of bucket lengths, which is supposed to quantify
     * how evenly the table has dispersed its elements (ratio within range of 0.9 ~ 1.1 is good).
     * Only used in testing, hence package-private.
     *
     * @return variance-to-mean ratio of bucket lengths
     */
    double calculateVMR()
    {
        int elemSum = 0;
        int[] elemCount = new int[size];

        for (int i = 0; i < size; i++)
            if (buckets[i] != null && !buckets[i].isEmpty())
            {
                elemSum += buckets[i].size();
                elemCount[i] = buckets[i].size();
            }

        double mean = 1.0 * elemSum / size;
        double sqDiffs = 0;

        for (int elems : elemCount)
            sqDiffs += Math.pow((elems - mean), 2);

        return ((sqDiffs / size) / mean);
    }


    /*------PRIVATE HELPER METHODS BELOW, NO COMMENTS OR DESCRIPTION GIVEN------*/


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
