## Testing documentation

### Test coverage

See [coveralls](https://coveralls.io/github/jrnn/wackpackr?branch=master) for
latest coverage reports.

Classes in wackpackr.io and wackpackr.util packages have extensive unit testing,
with near-100% coverage.

The core compressor classes are also quite well covered. The test suite, besides
must-have functionality tests, also runs several rounds of compressing and
decompressing in turn with random, textual, and image data of various sizes.
These tests, however, only check that the data decompresses to its initial form,
i.e. there are no requirements on performance for a test to pass.

Compressor helper classes are not tested at all individually, only indirectly as
part of the compressor testing. This probably is not an issue?

There is and will be no automatic testing for anything web/UI related, as these
are just nice-to-have features for convenience.

### Performance testing

As briefly mentioned above, each compressor class is tested for performance as
part of the overall test suite — but no thresholds have yet been set in terms of
speed and compression rate. The tests pass as long as nothing is "lost in
translation".

Though each time running the tests produces a small dataset on performance with
different kinds of input and filesizes, I haven't yet collected the data nor
done any analysis. Based just on casual observation, it is quite obvious that
Huffman compression performs often at least 4-5 times faster than LZSS, and LZSS
performs somewhat faster than LZW; but on the other hand, LZSS and LZW can at
best reach much lower compression rates than Huffman, with LZSS typically
outperforming LZW. In comparison, decompression is quite fast with all
algorithms.

Through a few rounds of improving the LZSS "longest match search" technique,
compression performance has incrementally improved. The current version works,
on average, 100 times faster than the initial brute-force implementation. LZW
decompression has been cut down to 10~20% time of the initial haphazard version.
In particular, LZW compression needs still to be improved.

Generally, a somewhat disheartening observation is that most file types cannot
be packed any further by wackpackr — quite the opposite, wackpackr actually
makes most files **larger**(!) When processing raw text, compression rates of
40-60% are very common, and can at times get as low as ~20%.
