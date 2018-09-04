Testing documentation
=====================

Test coverage
-------------

See [coveralls](https://coveralls.io/github/jrnn/wackpackr?branch=master) for
latest coverage reports.

Classes in [io](https://github.com/jrnn/wackpackr/tree/master/src/test/java/wackpackr/io)
and [util](https://github.com/jrnn/wackpackr/tree/master/src/test/java/wackpackr/util)
packages have extensive unit testing, with near-100% coverage.

The [core](https://github.com/jrnn/wackpackr/tree/master/src/test/java/wackpackr/core)
compressor classes are also quite well covered. The test suite, besides
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

___

Performance testing, round #1
-----------------------------

Each of the three compressors was, in turn, tested with three kinds of data:
(1) text, (2) image, and (3) random. For text, I used [my old M.A. thesis](https://github.com/jrnn/wackpackr/blob/master/src/test/java/wackpackr/test.txt)
which constitutes ~100 pages of fairly varied, academic English, in raw .txt
format; for image, I used [a photo of a sunset over a lake](https://github.com/jrnn/wackpackr/blob/master/src/test/java/wackpackr/test.bmp)
in raw .bmp format; and for random, just a pseudorandom stream of bytes
generated with java.lang.ThreadLocalRandom.

Tests were run with different file sizes, from 64kB to 1,024kB, in 64kB steps.
When necessary, the input text and image files were simply concatenated with
themselves to reach target file sizes: for instance, the thesis is only 243kB,
so you'd "copy-paste" it twice over, and then trim at 512kB to get a text file
of appropriate size.

Tests were run 20 times, for a total of **2,880** observations on compression
and decompression speed, rate, and fidelity. The following summary looks at the
(geometric) mean of these results, weighted by file size.

#### Fidelity

- None of the compressors failed to decompress a file to its exact initial form
  at any point during testing.

#### Speed

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

#### Compression efficiency

- Huffman is fast, but does not achieve as low compression rates as LZW and LZSS.
- LZW handles text better than LZSS, while LZSS handles images better than LZW.
- All three fail with random data. LZW and LZSS not only fail, but they do so
  miserably, resulting actually in notably larger file sizes than the input data.
- The above point only reveals that my implementations do not audit their own
  performance in any way, but keep on "compressing" even if they cannot reach a
  smaller file size.

![Figure 4](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig04.png)

Let's still look at the key metrics — compression throughput and rate — on a
scatter plot. Here, the rate is mapped as inverse, so that on each axis the
bigger the value, the better.

![Figure 1](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig01.png)

#### Conclusions?

Based on these tests, it should be safe to conclude that all compressors work
in linear time with respect to input size. Some are obviously slower, some
faster, but the difference is always a constant, at most on the order of 10~20.
This is the expected outcome from slapdash complexity analysis done in the
[implementation notes](https://github.com/jrnn/wackpackr/blob/master/docs/implementation.md).

___

Performance testing, round #2
-----------------------------

Each of the three compressors was, in turn, tested with the following widely
used test files:
- [Calgary and Cantebury corpora](http://corpus.canterbury.ac.nz/descriptions/)
- [enwik8](http://prize.hutter1.net/)
- [Waterloo image repository](http://links.uwaterloo.ca/Repository.html)

Tests were run 10 times, for a total of **1,860** observations on compression
and decompression speed, rate, and fidelity. The following summary looks at the
(geometric) mean of these results, weighted by file size.

#### Fidelity

- None of the compressors failed to decompress a file to its exact initial form
  at any point during testing.

#### Speed

The same findings as in round #1 appear to apply here equally:
- Huffman is stable, and takes the cake in compression speed.
- LZW is quite solid, typically running at half the speed of Huffman.
- LZSS has a bipolar disorder which, in fact, is even more pronounced with these
  data: for instance, LZSS unpacks the Calgary corpus **27 times** faster than
  packing it !!!

![Figure 6](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig06.png)
![Figure 7](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig07.png)

#### Compression efficiency

Again, the results seem to confirm what was said earlier:
- Huffman consistently loses to the other two.
- With text, LZW consistently beats LZSS by a small margin.
- With images (in this case, Waterloo), LZSS has an advantage over LZW.
- These data are routinely used in compression benchmarking, so no fiascos were
  seen (like with random bytestreams earlier)

![Figure 8](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig08.png)
![Figure 5](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig05.png)

One aspect that was overlooked previously is how stable performance is over
several rounds of compression and decompression. A couple of observations on
this:
- The compressors are deterministic. If you compress a file 100 times, you get
  the *exact* same output every single time.
- Compression and decompression time varies. If you pack and unpack a file 100
  times, you'll clock a different time on each run.
- This variance can be quantified to some extent from the test results, simply
  by looking at the standard deviation. The following charts plot the familiar
  average throughputs by compressor and corpus, and paint the average +/-
  standard deviation range for each.
- Clearly, variance is quite limited, i.e. we can expect compressor performance
  to be more or less the same on each run.

![Figure 9](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig09.png)
![Figure 10](https://github.com/jrnn/wackpackr/blob/master/docs/figures/fig10.png)

#### Conclusions?

The second round of testing confirms what was concluded after the first round.
All compressors tick in `~O(n)` time, and perform in a similar fashion with
typical compression benchmark data as with the more limited and questionable
data used in the first testing round.

___

So how does wackpackr compare?
------------------------------

Regrettably I did not have time to look up good benchmark data, but based on a
couple of quick sources ([1](https://tukaani.org/lzma/benchmarks.html), [2](https://bbengfort.github.io/observations/2017/06/07/compression-benchmarks.html)),
the following tentative observations can be made:
- The more recent of the two benchmarks suggests that, on a fairly "normal"
  laptop (Macbook Pro), commercial bzip2 and gzip applications compress at
  10—15MB/s, and decompress at 30—120MB/s.
- If these data points are reliable, then my implementations are not as shoddy
  as I expected in compression speed. Huffman and LZW can operate at comparable
  figures. My decompression speeds, though, are clearly lacking. Only LZSS can
  do more than 30MB/s.
- The benchmarks suggest that bzip2 and gzip routinely reach 25—33% compression
  rates with "real" data (e.g. the mishmash of files that come with a Linux
  distro), and even go way below 20% with textual data.
- My implementations rarely come even close to these numbers. But this is hardly
  a surprise. Also, the underlying techniques are not the same: bzip2 apparently
  is based on Burrows–Wheeler, and gzip is based on DEFLATE (which, to
  oversimplify, is LZSS + Huffman with a whole lot of optimisations).
