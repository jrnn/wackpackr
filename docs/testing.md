Testing documentation
---------------------

## Test coverage

See [coveralls](https://coveralls.io/github/jrnn/wackpackr?branch=master) for
latest coverage reports.

Classes in wackpackr.io and wackpackr.util packages have extensive unit testing,
with near-100% coverage.

The core compressor classes are also quite well covered. The test suite, besides
must-have functionality tests, also does several rounds of compressing and
decompressing in turn with random, textual, and image data of various sizes.
These tests, however, only check that the data decompresses to its initial form,
and there are no requirements on performance for a test to pass.

Compressor helper classes are not tested at all individually, only indirectly as
part of the compressor tests. For sake of completeness, these classes should be
unit tested as well.

There is and will be no automatic testing for anything web/UI related, as these
are just nice-to-have features for convenience. This unfortunately means that
100% coverage will not be reached. Boo-hoo.

## Performance testing, round #1

Each of the three compressors was, in turn, tested with three kinds of data:
(1) text, (2) image, and (3) random. For text, I used [my old M.A. thesis](https://github.com/jrnn/wackpackr/blob/master/src/test/java/wackpackr/test.txt)
which constitutes ~100 pages of fairly varied, academic English, in raw *.txt
format; for image, I used [a photo of a sunset over a lake](https://github.com/jrnn/wackpackr/blob/master/src/test/java/wackpackr/test.bmp)
in raw *.bmp format; and for random, just a pseudorandom stream of bytes
generated with java.lang.ThreadLocalRandom.

Tests were run with different file sizes, from 64kB to 1,024kB, in 64kB steps.
When necessary, the input text and image files were simply concatenated with
themselves to reach target file sizes: for instance, the thesis is only 243kB,
so you'd "copy-paste" it twice over, and then trim at 512kB to get a text file
of appropriate size.

Tests were run 20 times, for a total of 2,880 observations on compression and
decompression speed, rate, and fidelity. The following summary looks at the
(geometric) mean of these results, weighted by file size.

Speed:
- Huffman is very stable, regardless of whether compressing or decompressing, or
  what kind of data is being processed. In all scenarios, it can push ~25MB per
  second, which beats LZW and LZSS by a long shot especially in compression.
- LZSS is baffling. It is by far the slowest in compression (~3MB/s) but, in
  stark contrast, by far the fastest in decompression (~40MB/s). It unpacks
  files over ten times faster than it packs them(?!) The difference has to boil
  down to the longest pattern search — despite several rounds of optimisation, I
  must be doing something wrong there still...
- LZW falls in between Huffman and LZSS in compression speed, and takes last
  place in decompression. Like Huffman, the differences between compression vs.
  decompression are limited. Out of the three, LZW appears to be the most
  volatile to data type: for example, it can handle image data three times
  faster than a randomized byte stream.

![Figure 2](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig02.png)
![Figure 3](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig03.png)

Compression efficiency:
- Huffman is fast, but does not achieve as low compression rates as LZW and LZSS.
- LZW handles text better than LZSS, while LZSS handles images better than LZW.
- All three fail with random data. LZW and LZSS not only fail, but they do so
  miserably, resulting actually in larger file sizes than the input data.
- The above point only reveals that my implementations do not audit their own
  performance in any way, but keep on "compressing" even if they cannot reach a
  smaller file size.

![Figure 4](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig04.png)

Let's still look at the key metrics — compression throughput and rate — on a
scatter plot. Here, the rate is mapped as inverse, so that the bigger the value
on each axis, the better.

![Figure 1](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig01.png)

Based on these tests, it should be safe to conclude that all compressors work
in linear time with respect to input size. Some are obviously slower, some
faster, but the difference is always a constant, at most on the order of 10~15.
This is the expected outcome from slapdash complexity analysis.

## Performance testing, round #2

COMING SOON
