Implementation notes
--------------------

### Code organization

[wackpackr.config](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/config)
contains nothing of interest.

[wackpackr.core](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/core)
is the heart of the exercise — the compression algorithms. The `...Compressor`
classes act as points of entry, offering methods for compressing byte arrays and
decompressing them back to their original form. These classes implement a simple
`Compressor` interface, just so that they all can be handled with the same code
e.g. in performance testing. Part of the (de)compression logic is decoupled into
helper classes, for no other reason than trying to keep things "neat".

[wackpackr.io](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/io)
only contains a kind of wrapper class that encapsulates ByteArrayI/O from
standard Java library. This class offers methods for all I/O needs of the
compressors, most importantly the ability to read and write individual bits (the
standard I/O tools do not operate below byte level).

[wackpackr.util](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/util)
contains all the data structures and other utilities that the compressors rely
on. These classes often provide functionality only to the extent required by the
compressors, and for this reason might be practically unserviceable in a more
general context.

[wackpackr.web](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/web)
is there only to provide a web-based UI — utterly irrelevant.

___

### A few words on the compressors

#### Huffman [REWRITE]

- By-the-book implementation, that roughly does the following things when
  compressing:
  - Reads the inbound file and counts number of occurences (frequencies) of each
    byte value.
  - Builds a Huffman tree that associates each byte value with a unique bit
    sequence, so that frequent values have shorter and less common ones longer
    encodings.
  - Encodes this tree to the header of the compressed binary, so that it can be
    recovered when decompressing ...
  - ... and finally encodes the inbound file by replacing each byte with its new
    (hopefully shorter) code as instructed by the Huffman tree
- And when decompressing:
  - Rebuilds the Huffman tree from the header of the compressed binary, and ...
  - ... with this tree, reverse-engineers the code back to the original byte
    values.
- Both ways, complexity is dominated by the part where the bulk code is, symbol
  by symbol, translated to/from its Huffman encoded form. Here, each byte is
  read exactly once, and for each byte, the tree is walked from root to a leaf.
  The tree in practice is a binary search tree, so each search takes `O(log N)`
  time, where `N` = number of nodes.
- The number of nodes in the tree is very limited compared to the number of
  bytes in processed files (some ~500 vs. millions). Hence, it should be fair to
  say the logarithmic part is trivial, and that the operations can be expected
  to run in linear time with respect to file size.

[INTRODUCE AND LINK THE RELEVANT CLASSES HERE?]

#### LZSS (Lempel–Ziv–Storer–Szymanski)

LZSS achieves compression by replacing recurring patterns in the data with
back-references to an earlier occurrence. A back-reference simply is a pair of
numbers: *offset* and *length*. Offset tells how many bytes to backtrack in the
data, and length tells how many bytes to read from that position. In other
words, a back-reference states that "the next *length* bytes are the same ones
as seen *offset* bytes backward."

In this implementation, two bytes are reserved for a back-reference: 12 bits for
offset, and 4 bits for length. This means that the maximum distance to backtrack
is 4095 bytes, and the maximum length for a recurring pattern is 15 bytes. Also,
since a back-reference takes up two bytes, only patterns longer than (or equal
to) three bytes are referenced. This further means that *length* always implies
a "plus three", bumping maximum length up to 18 bytes.

To tell the difference between back-references and literal data, a one-bit flag
is used: *literal blocks* (one byte) are preceded by a 0, and *pointer blocks*
(two-byte back-reference) are preceded by a 1. This chips away a bit at the
compression rate — in fact, if there's little to no recurrence in the data, the
encoded form can be bigger than the original. But even so, in most cases the
trade-off is justifiable.

Because there's a limit how far back you can go, and how long patterns you can
repeat, LZSS does not need to look at an entire file at once; instead, it only
needs to look at a buffer of definite size, that "slides" through the file as
the algorithm runs. In this implementation, the buffer size is 4095 + 18 = 4113
bytes. At each step, the aim is to find the longest (partial or complete) match
of the 18 "lookahead" bytes in the buffer, and if a match of at least the first
three bytes is found, a back-reference is written instead of the literal bytes.

So, with the basics in place, compression roughly goes as follows:
- Run the inbound file through a "sliding window" of fixed length. As the window
  slides forward, next bytes in the file flow in from one end, and "oldest"
  bytes drop out from the other end.
- At each step, scan the window for the best match of the 18-byte "lookahead"
  sequence.
- If a match is found, write a pointer block (i.e. back-reference); if not,
  write a literal block.
- Slide the window forward by `X` bytes, where `X` is the length of the best
  match found in the above step.
- Rinse and repeat, until reaching end of file.

...and decompression:
- Similar to compression, run the encoded file through the "sliding window."
- When encountering pointer blocks, copy data from the already-decoded tail part
  as instructed by the back-reference.
- When encountering literal blocks, just write the literal byte as-is.
- Rinse and repeat, until reaching end of file.

Clearly, both in compression and decompression, the input file is read through
exactly once, and encoded/decoded on the go. This only involves simple, basic
operations (reading and writing bits and bytes). Hence, it would be nice to say
that both run in linear time with respect to file size. However, there is one
key difference between the two — the longest pattern match in compression. This
is the potential bottleneck. A brute-force approach where the whole buffer is
scanned results in massive waste, something on the order of `O(n²)` time. More
clever strategies are needed to minimize the search work.

This implementation tries to mimic the technique used in DEFLATE: the positions
of three-byte sequences within the sliding window are memorized in a hash table
(byte triple as key, position as value), so that the search can be limited only
to positions where at least the initial three bytes match. Statistically, this
reduces the number of matching attempts significantly. Assuming an even
distribution of byte values, there should on average be *only one* search
attempt per step. Therefore, this implementation of LZSS should be able to
handle both compression and decompression in `~O(n)` time.

[INTRODUCE AND LINK THE RELEVANT CLASSES HERE?]

#### LZW (Lempel–Ziv–Welch)

The basic idea is the same as LZSS: recurring byte sequences are replaced with
shorthands. However, whereas LZSS uses back-references to preceding data, LZW
maintains a separate dictionary of encountered sequences and uses dictionary
indexes as the reference.

Basically, as LZW reads through the file byte by byte, every time it encounters
a "substring" it has not seen yet, it stores that substring in the dictionary
under the next free index; and if that substring is encountered again, it writes
to output just the corresponding index.

The ingenious thing is that no metadata about the dictionary needs to be saved
for decompression purposes. In both cases, the dictionary is constructed "on the
fly" with the same logic of stepwise substring concatenation. The only
prerequisite is that both compression and decompression start with the same
initial dictionary — typically, placing all possible one-byte sequences in the
first 256 indexes. This implementation is otherwise standard, but reserves the
zero-index for the pseudo-EoF marker.

To avoid LZW running crazy with memory, dictionary size needs to have an upper
bound. Whenever the cap is reached, the dictionary is flushed to its initial
state. In this implementation, the cap is set at 65,535 (that is, an index
reference can at most take up 16 bits). So, all in all, index 0 = pseudo-EoF,
indexes 1—256 = basic one-byte values, and indexes 257—65,535 = byte sequences
of length 2 or more.

It should be clear from the above that a LZW compressed file, in practice, is
an uninterrupted chain of dictionary indexes. Therefore, unlike LZSS, LZW does
not need to "waste" bits to distinguish between this or that kind of block.

The logic of compression vs. decompression is not described here step by step:
it is sufficient to note that, in both cases, the file is read through exactly
once, while maintaining a dictionary on the side. At each step, the dictionary
is searched once, and a maximum of one new entry is put into it. So, we can see
that overall time complexity is determined by the search/insertion operations —
if these can be done in `O(1)` time, then LZW runs in linear time with respect
to file size.

How to ensure that the dictionary works in constant time? When decompressing,
this is easy. The decoder just needs to look up byte sequences by index, so an
ordinary array is all that's needed. Compression is trickier. The encoder needs
to look up indexes by byte sequence. This of course could be solved with a hash
table that, given the limited dictionary size, would not even need all that many
buckets to maintain a sensible load factor. However, this implementation takes a
different approach, relying on a prefix tree that takes full advantage of the
indexing properties and only reserves so much memory as needed, yet still can
handle the search and insertion in what amounts to constant time. (The exact
mechanics are elaborated in the relevant classes.)

Furthermore, for compression efficiency, this implementation uses variable bit
sizes: only so many bits are reserved as necessary when writing an index. This
is dictated by the running dictionary size. For instance, if the dictionary has
666 entries, then we know that no more than 10 bits are needed to express any
index at that point. When index 1,024 is reached, the bit size is bumped up to 11.
This little optimisation does not compromise time complexity in any way.

[INTRODUCE AND LINK THE RELEVANT CLASSES HERE?]

___

### What could be done better?

- BinaryIO : I have a hunch this might be or become a bottleneck. Tried to
  optimise it a couple times, but no performance improvement observed.
- LZSS : can the longest pattern search be improved still?
- LZSS : apply Huffman coding (adaptive?) for pointer and literal blocks for
  improved compression rate
- LZW : monitoring compression efficiency and "flushing" explicitly if it drops
  below certain threshold
- LZW : instead of resetting dictionary, free up space by dumping least-used
  entries?
