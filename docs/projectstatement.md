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
fidelity — in other words, testing which one fares best.

### Scope

In order to run comparisons, the minimum requirement is to implement at least
_two_ algorithms. For a little variation, idea is to pick at least one entropy
encoding vs. at least one dictionary encoding algorithm.

For entropy encoding, the obvious choice is Huffman.

For dictionary encoding, the choice is between the two main Lempel-Ziv families,
namely LZ77 and LZ78. Furthermore, a choice must be made between vanilla version
vs. some later derivative (such as LZSS, LZMA, LZW).

If time permits, it would be nice to do something from _both_ LZ families. Also,
it would be nice to give some combined approach a try (e.g. DEFLATE), but this
probably is too ambitious.

As for data structures, at least (min)heap and an "upside-down" binary tree
structure are needed for Huffman. Further data structures depend on the selected
LZ algorithm(s). For instance, LZ77 probably needs something queue-like for its
sliding window, while LZ78 likely would need a search trie for its dictionary.

On a further note, it is still not clear what data structures and/or sorcery is
needed in performance testing. For instance, how to verify that a file
decompresses to its exact original form — MD5 checksums or similar?

### Input/output

Files (of any kind) are submitted for compression through a web-based UI. Then,
the submitted file is compressed and decompressed with each implemented
algorithm. Finally, the UI prints a simple report on the performance of each
algorithm: how long it took, what compression rate it achieved, and whether the
file was still intact after decompression.

At this stage, plan is that the decompressed file itself is not part of the
output, because such files are utterly useless outside the narrow context of
this project.

A nice add-on feature would be statistics on average performance of each
algorithm, broken down e.g. by file type. This would require connecting to a
database to record test results over time, but that's no big deal.

### Sources

As usual, Wikipedia was the point of entry in familiarizing the subject:
[Huffman](https://en.wikipedia.org/wiki/Huffman_coding),
[LZ](https://en.wikipedia.org/wiki/LZ77_and_LZ78).

[This](http://ethw.org/History_of_Lossless_Data_Compression_Algorithms) was
useful in understanding how compression algorithms are categorized and how they
relate to one another.

Materials from a past Helsinki University course on data compression techniques
have been particularly priceless:
[Huffman coding](https://www.cs.helsinki.fi/u/puglisi/dct2015/slides2.pdf),
[dictionary compression](https://www.cs.helsinki.fi/u/puglisi/dct2015/slides7.pdf).

Youtube is also filled with introductory videos that are variably helpful. To
name a few:
[1](https://www.youtube.com/watch?v=JsTptu56GM8),
[2](https://www.youtube.com/watch?v=umTbivyJoiI),
[3](https://www.youtube.com/watch?v=goOa3DGezUA),
[4](https://www.youtube.com/watch?v=Jqc418tQDkg).
