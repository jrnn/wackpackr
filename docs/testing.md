## Testing documentation

### Test coverage

See [coveralls](https://coveralls.io/github/jrnn/wackpackr?branch=master) for
latest coverage reports.

Classes in wackpackr.io and wackpackr.util packages have extensive unit testing,
with near-100% coverage.

The core compressor classes also *look to be* well covered, but the tests are
currently very, very superficial. Also, the helper classes are not tested at all
individually, only indirectly as part of the compressor testing. This probably
is not an issue?

There is and will be no automatic testing for anything web/UI related, as these
are just nice-to-have features for convenience.

### Performance testing

No systematic performance testing yet...

The web UI reports some basic metrics (compression rate and time). Based on
rough, non-systematic observations, Huffman compression performs 4-5 times
faster than LZSS, but LZSS can reach better compression rates; as for
decompression, on the contrary, LZSS is clearly faster than Huffman.

Through a few rounds of improving the LZSS "longest match search" technique,
compression performance has incrementally improved. The current version works,
on average, 100 times faster than the initial brute-force implementation.

Idea is to extend this so that results of each compression/decompression test
done through the web UI are stored in a database. Over time, this would then
give some idea of aggregate performance of each implemented algorithm, grouped
e.g. by file type.

Generally, a somewhat disheartening observation is that most file types cannot
be packed any further by wackpackr — quite the opposite, wackpackr actually
makes most files **larger**(!) When processing raw text, compression rates of
40-60% are very common, and can at times get as low as ~20%.
