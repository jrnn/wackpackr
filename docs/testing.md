## Testing documentation

### Test coverage

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
part of the compressor testing. This probably is not an issue? If they weren't
working as supposed, surely the compression tests would fail?

There is and will be no automatic testing for anything web/UI related, as these
are just nice-to-have features for convenience.

### Performance testing

As briefly mentioned above, each compressor class is tested for performance as
part of the overall test suite — but no thresholds have yet been set in terms of
speed and compression rate. The tests pass as long as nothing is "lost in
translation".

Each time running the tests produces a small dataset on performance with
different kinds of input and filesizes. I haven't yet looked at the data in
depth, but a quick-and-dirty analysis suggests the following:
- Compression rate
  - LZSS and LZW achieve consistently lower rates than Huffman
  - LZW outperforms the others in text, often getting below 50%
  - LZSS outperforms the others in images, often getting close to 60%
  - All three fail with pseudorandom byte streams, resulting in compression
    rates > 1.00; LZW in particular fails spectacularly, bloating the data to
    1.4~1.5x its original size
- Compression speed
  - Huffman is by far the fastest
  - LZW comes in second, working on average three times slower than Huffman
  - LZSS is clearly slowest, taking on average 7~8 times longer(!) than Huffman
- Decompression speed
  - Curiously, LZSS performs fastest (in stark contrast to compression speeds)
  - Huffman follows close on the heels of LZSS, at ~1.5 its speed
  - LZW performs on average 4x slower than LZSS

More thorough analysis will be done later, backed up with charts. Before doing
so, perhaps the algorithms (especially LZSS and LZW) can still be optimized to
some extent. I've already managed to speed up LZSS compression 100x from the
initial brute-force version, but clearly something is still amiss. Also LZW is
now working 10-20x faster than the first version, but there's still room for
improvement, I believe.

Generally, a somewhat disheartening observation is that most file types cannot
be packed any further by wackpackr — quite the opposite, wackpackr actually
makes most files **larger**(!). Basically, only "raw" file types can be packed.
