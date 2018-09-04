Implementation notes
--------------------

### Code organization

[wackpackr.core](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/core)
is the heart of the exercise — the compression algorithms. The `...Compressor`
classes act as points of entry, offering methods for compressing byte arrays and
decompressing them back to their original form. These classes implement a simple
`Compressor` interface, just so that they all can be handled with the same code
e.g. in performance testing. Part of the (de)compression logic is decoupled into
helper classes, for no other reason than trying to keep things "neat".

[wackpackr.io](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/io)
only contains a kind of wrapper class that encapsulates `ByteArrayI/O` from
standard Java library. This class offers methods for all I/O needs of the
compressors, most importantly the ability to read and write individual bits (the
standard I/O tools do not operate below byte level). It would not have been a
big deal to replace `ByteArrayI/O` with something DIY, but here we are. In any
case, this is the only place in the application logic where standard library is
resorted to (apart from `System.arraycopy()` all over the place...)

[wackpackr.util](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/util)
contains all the data structures and other utilities that the compressors rely
on. These classes often provide functionality only to the extent required by the
compressors, and for this reason might be practically unserviceable in a more
general context. A good example of this is [ErraticHashTable](https://github.com/jrnn/wackpackr/blob/master/src/main/java/wackpackr/util/ErraticHashTable.java),
which is built for the very specific use case of LZSS longest match searching.

[wackpackr.web](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/web)
is there only to provide a web-based UI — utterly irrelevant. Actually it was
something of a miscalculation to do the UI like this. Command line would have
been sufficient, if not better.

___

### A few words on the compressors

Three compression algorithms are implemented: **Huffman**, **LZW**, and **LZSS**.
The basic idea of each is explained below, with some sidenotes on these specific
implementations.

#### Huffman

Huffman achieves compression by mapping variable-length codes to byte values, so
that frequently occurring bytes get shorter codes, and less frequent ones *vice
versa*. Simply put, Huffman forms a statistical model of the input file by
counting the number of occurrences of each byte, and then rewrites those bytes
one by one using as few bits as possible.

For the variable-length encoding to work, the compressed bit stream must be
unambiguous as to where one code ends and another starts. That is, no code can
be a prefix of any other code. This is done by structuring the codes as a prefix
tree, where byte values are stored in leaves, and the codes correspond to paths
from the root node to a leaf node. The decoder, then, just needs to walk the
tree, over and over, and write down the byte value it finds whenever hitting a
leaf.

The prefix tree is built "bottom-up" like this:
1. Create a leaf node for each unique byte value, and order them by frequency.
2. Grab two nodes with the lowest frequency.
3. Create an internal node whose frequency is the sum of the two nodes'
   frequencies, and assign the two nodes as its children.
4. Put this new node in with the other nodes, on its right place by frequency.
5. Keep repeating steps 2 to 4 until all nodes are in one tree.

In a sense, the prefix tree is the "key" to encoding and decoding a file, and
the form of the tree is determined by the file. Hence, to be able to decode the
compressed file, the tree used in encoding must be included in it. This
implementation stores the tree at the head of the compressed binary using
pre-order traversal, so that it can be extracted and reconstructed with a
"reverse" (in-order) traversal by the decoder.

Compression and decompression are very similar. Basically, (1) first you form
the prefix tree, and (2) then rewrite the file using that tree. The encoder has
a bit more work, as it needs to read the file through once to calculate byte
frequencies, and then a second time to encode it. The decoder gets the tree
straight from the header, and needs to read the file through only once.

But before we conclude that compressing and decompressing run in `O(n)` time,
where `n` = file size, we need to note that since a (binary) tree is involved,
there's a logarithmic component to overall complexity: `O(n log x)`, where `x` =
number of nodes in the tree. However, seeing that the prefix tree can at most
have 256 leaves, and thus ~512 nodes, the `log x` part melts down to a constant
— especially since `n`, by contrast, can easily number in the millions.

So, it is fair to say that Huffman can be expected to run in linear time with
respect to file size. This has been confirmed in performance testing: average
throughput (MB/s) is more or less the same irrespective of file size.

#### LZSS (Lempel–Ziv–Storer–Szymanski)

LZSS achieves compression by replacing recurring patterns in the data with
back-references to an earlier occurrence. A back-reference simply is a pair of
numbers: *offset* and *length*. Offset tells how many bytes to backtrack in the
data, and length tells how many bytes to read from that position. In other
words, a back-reference states that "the next *length* bytes are the same ones
as *offset* bytes backward."

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
repeat, LZSS does not need to look at an entire file at once. Instead, it only
needs to look at a buffer of definite size, that "slides" through the file as
the algorithm runs. In this implementation, the buffer size is 4095 + 18 = 4113
bytes. At each step, the aim is to find the longest (partial or complete) match
of the 18 "lookahead" bytes in the buffer, and if a match of at least the first
three bytes is found, a back-reference is written instead of the literal bytes.

So, to sum up, compression roughly goes as follows:
- Run the inbound file through a "sliding window" of fixed length. As the window
  slides forward, next bytes in the file flow in from one end, and "oldest"
  bytes drop out from the other end.
- At each step, scan the window for the best match of the 18-byte "lookahead"
  sequence.
- If a match is found, write a pointer block (i.e. back-reference); if not,
  write a literal block.
- Slide the window forward by `x` bytes, where `x` is the length of the best
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
key difference between the two: the longest pattern match in compression. This
is the potential bottleneck. A brute-force approach, where the whole buffer is
scanned, results in massive waste, something on the order of `~O(n²)` time. More
clever strategies are needed to minimize the search work.

This implementation tries to mimic the technique used in DEFLATE: the positions
of three-byte sequences within the sliding window are memorized in a hash table
(byte triple as key, position as value), so that the search can be limited only
to positions where at least the initial three bytes match. Statistically, this
reduces the number of matching attempts significantly. Assuming an even
distribution of byte values, there should on average be *only one* search
attempt per step. Therefore, this implementation of LZSS should be able to
handle both compression and decompression in `~O(n)` time.

The above has been confirmed in performance testing: average throughput (MB/s)
is more or less the same irrespective of file size. However, there's a stark
difference between compression vs. decompression speed. Compressing a file can
be 20+ times slower than decompressing it. This does not undermine the
observations about time complexity, but rather suggests that there's something
amiss with the encoder.

#### LZW (Lempel–Ziv–Welch)

The basic idea is the same as LZSS: recurring byte sequences are replaced with
shorthands. However, whereas LZSS uses back-references to preceding data, LZW
maintains a *separate dictionary* of encountered sequences and uses *dictionary
indexes* as the reference.

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

The logic of compression vs. decompression is not described here step by step.
It is sufficient to note that, in both cases, the file is read through exactly
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
different approach, utilizing a prefix tree that takes full advantage of the
indexing properties and only reserves as much memory as needed, yet still can
handle the search and insertion in what amounts to constant time. (The exact
mechanics are elaborated in the relevant classes' JavaDoc.)

Furthermore, for compression efficiency, this implementation uses variable bit
sizes: only so many bits are reserved as necessary when writing an index. This
is determined by the running dictionary size. For instance, if the dictionary
has 666 entries, then we know that no more than 10 bits are needed to express
any index at that point. When index 1,024 is reached, the bit size is bumped up
to 11. This little optimisation does not compromise time complexity in any way.

Performance testing indicates that this implementation runs in constant time, as
expected. Throughput (MB/s) is more or less the same irrespective of file size.

___

### What could be done better?

[BinaryIO.java](https://github.com/jrnn/wackpackr/blob/master/src/main/java/wackpackr/io/BinaryIO.java):

- I have a feeling that this might be a bottleneck. If so, it affects all
  compressors equally. I tried to optimise the I/O operations a couple of times,
  especially in the cases when a bit sequence is read or written "over" the byte
  boundaries in the underlying `ByteArrayI/O`, but did not observe any speed
  improvement.

[Huffman](https://github.com/jrnn/wackpackr/blob/master/src/main/java/wackpackr/core/HuffCompressor.java):

- I'm quite happy with this one and haven't noted any improvement ideas,
  although there probably are dozens of things that could be done better.

[LZSS](https://github.com/jrnn/wackpackr/blob/master/src/main/java/wackpackr/core/LZSSCompressor.java):

- LZSS ticks me off the most. LZSS compression speed is remarkably slow compared
  to the others. Considering how fast the decompression works, it must boil down
  to the longest match search. Despite going through a few incremental tweaks
  and fixes to the search technique, and seeing substantial improvements at each
  step, it *still* sucks.
- To improve compression efficiency, there are quite a few tricks that could
  still be tried. For one, instead of encoding pointer and literal blocks at a
  fixed width (1 vs. 2 bytes), could use e.g. Huffman encoding to trim out some
  bits here and there.

[LZW](https://github.com/jrnn/wackpackr/blob/master/src/main/java/wackpackr/core/LZWCompressor.java):

- I'm fairly happy with how this turned out. Nonetheless...
- Could monitor compression efficiency and explicitly reset the dictionary if
  the running ratio drops below certain threshold.
- Instead of resetting the dictionary, one option would be to dump least-used
  entries one by one. This allegedly can improve compression efficiency.

Other:

- There likely are also no small number of potential improvements on the data
  structures, but I haven't jotted anything down, so I wouldn't even know where
  to begin.
- For example, the root cause for LZSS's sluggish compression performance could
  very well lie in any of the DIY data structures it employs.
- ...almost forgot — unit test coverage!! Compressor helper classes currently
  have no unit tests whatsoever. This is a howling disgrace, and would not even
  be that much work to make right.
