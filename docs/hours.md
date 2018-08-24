## Time tracking

| date | hours | cumulative | activity |
|:-----|:------|:-----------|:---------|
| 24.7. | 1 | 1 | basic project setup |
| 25.7. | 3 | 4 | reading up on subject, writing project statement |
| 25.7. | 4 | 8 | first cut Huffman compress (works only on Strings) |
| 26.7. | 4 | 12 | methods for parsing Huffman tree to/from 1s and 0s |
| 28.7. | 3 | 15 | replace java.util.PriorityQueue with DYI min-heap |
| 28.7. | 2 | 17 | min-heap unit testing |
| 29.7. | 2 | 19 | figuring out how to encode pseudo-EoF node in Huffman tree without giving up a slot in the 0-255 byte range |
| 29.7. | 1 | 20 | first cut Huffman decompress (only Strings...) |
| 29.7. | 5 | 25 | extending Huffman to work more generally with binary data |
| 30.7. | 2 | 27 | restructuring Huffman code |
| 31.7. | 4 | 31 | i/o wrapper tweaks and unit testing |
| 1.8. | 2 | 33 | further restructuring Huffman code |
| 3.8. | 2 | 35 | huffnode unit testing + quick tests for huffman compressor |
| 5.8. | 3 | 38 | reading up on LZ77 and LZSS |
| 5.8. | 2 | 40 | first cut LZ77/SS compress, with wasteful "brute-force" longest match search (not reflected in UI yet) |
| 6.8. | 2 | 42 | DYI sliding window utility |
| 6.8. | 2 | 44 | first cut LZ77/SS decompress |
| 7.8. | 4 | 48 | speed up LZSS by limiting longest match search to positions where first byte matches (now 50x faster on average) |
| 7.8. | 2 | 50 | restructuring and commenting LZSS code |
| 9.8. | 1 | 51 | baking ByteArrayI/O into custom i/o wrapper class, extending unit tests |
| 9.8. | 1 | 52 | sliding window unit testing |
| 9.8. | 1 | 53 | quick tests for LZSS compressor |
| 11.8. | 3 | 56 | speed up LZSS further by doing pattern matching on three-byte sequences (average compression time reduced by half) |
| 11.8. | 2 | 58 | DYI hash table with very limited usability |
| 12.8. | 2 | 60 | replace java.util.ArrayDeque in hash table with DYI linked list |
| 12.8. | 2 | 62 | linked list tweaking and unit testing |
| 13.8. | 1 | 63 | cleaning up code and comments |
| 14.8. | 2 | 65 | hash table tweaking and unit testing |
| 14.8. | 1 | 66 | prettier outer crust with bulma.io, UI for compressing files instead of text |
| 14.8. | 1 | 67 | javadoc formatting |
| 15.8. | 2 | 69 | reading a bit about LZ78 and LZW, and taking first steps with code |
| 16.8. | 2 | 71 | clumsy, malfunctioning LZW compression and decompression |
| 16.8. | 2 | 73 | reorganizing code in compressor classes |
| 16.8. | 2 | 75 | extending LZW |
| 17.8. | 4 | 79 | DYI ByteString utility with unit testing |
| 17.8. | 1 | 80 | updating documentation |
| 18.8. | 1 | 81 | minor tweaks to and more unit tests for i/o wrapper and bytestring |
| 18.8. | 1 | 82 | reworking compressor tests |
| 19.8. | 2 | 84 | speed up LZW decompression (now 5x faster on average) by using a simple array instead of a map ('duh...) |
| 19.8. | 2 | 86 | isolate dictionary-related stuff from LZW compressor to its own class |
| 19.8. | 2 | 88 | replacing java.util.TreeMap in LZW dictionary with some homebrewn shenanigans |
| 20.8. | 1 | 89 | working still a bit on the LZW trie... |
| 20.8. | 4 | 93 | trying to make LZW work with variable-width codes |
| 21.8. | 2 | 95 | finetuning I/O operations (expected speed improvement, but none observed...) |
| 22.8. | 2 | 97 | restructuring and commenting LZW code |
| 23.8. | 1 | 98 | still further work on LZW trie |
