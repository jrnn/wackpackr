Project statement
-----------------

### Purpose and objectives

Aim of the project is to implement a number of "legendary" lossless compression
algorithms, including the necessary data structures, from scratch. Language of
choice is Java.

The ambition by no means is to build a workable compressor to go up against the
bzip2s and 7zips out there, but rather to get acquainted with the basics of
compression in a hands-on manner (and grab a few ECTS while doing so).

Besides implementing the algorithms, another focus area is comparing the
performance of the different implementations in terms of speed, space, and
fidelity.

### Scope

Scope is limited to three algorithms. For a little variation, both entropy and
dictionary encoding techniques are represented.

For entropy encoding, the obvious choice is **Huffman**.

For dictionary encoding, one algorithm is picked from each of the two main
Lempel-Ziv families: **LZSS** from the LZ77, and **LZW** from the LZ78 family.

With these choices, the following data structures need to be implemented:
- For Huffman: min-heap, prefix tree
- For LZW: prefix tree (more complex than the one for Huffman)
- For LZSS: hash table, linked list, queue-like "sliding window" utility

### Input/output

Files of any kind are submitted for compression through a web-based UI. Then,
the submitted file is compressed and decompressed with each implemented
algorithm. Finally, the UI prints a simple report on the performance of each
algorithm: how long compression vs. decompression took, what compression rate
was achieved, and whether the file was still intact after decompression.

The decompressed file itself is not given out. This is to emphasize that the
project is not intended for real-life compression needs, only for testing the
implemented compressors against one another.

### Sources

As usual, Wikipedia was the point of entry in familiarizing the subject:
[Huffman](https://en.wikipedia.org/wiki/Huffman_coding),
[Lempel-Ziv](https://en.wikipedia.org/wiki/LZ77_and_LZ78),
[LZW](https://en.wikipedia.org/wiki/LZW),
[LZSS](https://en.wikipedia.org/wiki/LZSS).

[This](http://ethw.org/History_of_Lossless_Data_Compression_Algorithms) was
useful in understanding how compression algorithms are categorized and how they
relate to one another.

Materials from a past Helsinki University course on data compression techniques
have been very instructive:
[Huffman coding](https://www.cs.helsinki.fi/u/puglisi/dct2015/slides2.pdf),
[dictionary compression](https://www.cs.helsinki.fi/u/puglisi/dct2015/slides7.pdf).

Juha Nieminen's excellent notes on [efficient LZW implementation](http://warp.povusers.org/EfficientLZW/index.html)
have been studied and leveraged meticulously.

Michael Dipperstein's notes on [LZSS implementation](http://michael.dipperstein.com/lzss/)
were also particularly useful, and gave a lot of ideas for improving the longest
match search.
