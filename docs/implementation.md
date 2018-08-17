## Implementation notes

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
currently only contains a kind of wrapper class that encapsulates ByteArrayI/O
from standard Java library. This class offers methods for all I/O needs of the
compressors, most importantly the ability to read and write individual bits (the
standard I/O tools do not operate below byte level). Since ByteArrayI/O are not
complicated, they could be replaced with own implementations at some point, if
only to completely eliminate reliance on standard library.

[wackpackr.util](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/util)
is an inconspicuous but invaluable package, containing all the data structures
and other utilities that the compressors rely on. These classes often provide
functionality only to the extent required by the compressors, and may at times
be so tailored for the occasion, that they are practically unserviceable in a
more general context.

[wackpackr.web](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/web)
is there only to provide a web-based UI — utterly irrelevant.

### A few words on the compressors

**Huffman**
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

**LZSS**
- Somewhat haphazard implementation, that roughly does the following things when
  compressing:
  - Runs the inbound file through a "sliding window" that consists of a short
    16-byte lookahead buffer, and a longer 4096-byte prefix buffer: whenever the
    window "slides" forward, next bytes in the file flow into the lookahead part,
    at the same time displacing oldest bytes at the end of the prefix part.
  - On each step, searches backwards through the prefix for the longest match of
    the byte sequence currently in lookahead buffer.
  - If a match is found, writes to output an offset-length pair that states
    "the next 'length' bytes are the exact same ones as starting 'offset' bytes
    backward from here"; if no match is found, then writes to output the first
    byte in lookahead as-is.
  - So, in essence, the idea is to encode recurring patterns in the file as
    (shorter) references to previous occurrences.
  - Two bytes are reserved for these pointers, 12 bits for offset and 4 bits for
    length — hence the prefix and lookahead buffer sizes of 4096 vs. 16.
- And when decompressing:
  - Simply, reads through the compressed binary, and just decodes the pointers
    as they come (copying back-references from the already-decompressed tail
    part).
- Either way, the file is read through only once. The only potentially heavy
  part is the pattern search when compressing. A brute-force approach where the
  whole prefix buffer is searched on each step results in `O(n^2)` time, which
  is out of the question. More clever strategies are needed to minimize the
  search work.
- This implementation tries to follow a technique set out in Deflate, where the
  positions of three-byte sequences are memorized (as they enter the prefix
  buffer), so that the search can always be limited only to positions where at
  least the initial three bytes match. Statistically, this reduces the number of
  pattern matching attempts significantly: assuming an even distribution of byte
  values, there should be on average _only one_ search attempt per step.
- All in all, this means that also LZSS can be expected to run in linear time.

**LZW**
- (WORK IN PROGRESS)

### What could be done better?

- Everything can always be done better. There are probably dozens and dozens of
  bigger and smaller optimizations that could be pursued.
