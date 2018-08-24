## Week #1

#### Main activities

- Picking a topic and trying to understand what it is about
- Basic project setup
- Baby steps with Huffman

#### Next in line

- Extending and refining Huffman implementation
- Replacing java.util.PriorityQueue with a DYI data structure
- Unit testing

#### Issues and questions

- None so far

## Week #2

#### Main activities

- Extending and restructuring Huffman code, should be more or less done now
- Writing a wrapper for I/O streams that can read and write one bit at a time
- Replacing java.util.PriorityQueue with own heap implementation
- Writing unit tests for classes that I'm fairly happy with and expect not to
  change or scrap

#### Next in line

- Deciding which incarnation of the various LZ algorithms to pick, and getting
  started on it
- Either (1) reorganizing Huffman code further, or (2) settling for current
  implementation and filling in missing tests

#### Issues and questions

- This is my first time fiddling with bits and bytes, which caused some headache
  but also good learning
  - Lost my bearings at first with the signed nature of the byte datatype in
    Java (wait, was it -128 or 255 or what?), especially when having to break
    bytes down to individual bits and/or move between datatypes
  - Was surprised to learn that the standard Input and OutputStreams do not
    operate below the byte level: putting together an "add-on" tool that can do
    I/O on binary level was a very instructive experience
- Would like to check if I'm at this point using something from standard library
  that should be replaced with DYI implementations. Are the following legal?
  - java.io.ByteArrayInputStream
  - java.io.ByteArrayOutputStream
  - java.util.Arrays
- Also, is it OK to resort to standard library in test classes?

## Week #3

#### Main activities

- Implementing something akin to LZSS (I think?)
- Writing a "sliding window" utility for LZSS that kind of resembles a queue

#### Next in line

- For LZSS, replacing current pattern search technique that naively only looks
  at the first byte, with a more "pro" approach that uses a chained hash table
  and works with the hash value of 3-byte sequences
- Related to the above, implementing relevant data structures (at least a hash
  table and probably just a singly linked list)
- Doing one more algorithm, probably LZW
- Updating UI so that user has access to all algorithms (if time permits)

#### Issues and questions

- This week was smoother sailing because I could handle the bits and bytes with
  the I/O tool from last week, and focus only on the algorithm
- I probably need to reorganize code in the [core](https://github.com/jrnn/wackpackr/tree/master/src/main/java/wackpackr/core)
  package. I wanted to "keep things neat" by splitting code up to several
  classes, but apparently ended up totally trivializing the compressor classes
  that act as entry points

## Week #4

#### Main activities

- Improving LZSS pattern search technique
- Related to the above, implementing a couple of basic data structures (linked
  list, hash table)
- Getting started on LZW
- Compulsive spring cleaning and code reorganization

#### Next in line

- Finalizing LZW and replacing java.util.TreeMap with something DYI
- Doing performance testing(?)
- Making sure there are no blind spots in unit testing

#### Issues and questions

- Nothing comes to mind, I'm quite happy with how things are coming along.
- Is it OK if some classes are not tested directly, if they are "covered"
  indirectly as part of some other tests?
  - Namely, there are a couple of helper classes that only serve the compressor
    classes, and I don't think testing them in isolation adds much value.
- P.S. Got rid of `Arrays.copy(...)` by using `System.arraycopy(...)` instead
  :-D that's one import down

## Week #5

#### Main activities

- Improving LZW speed with a custom binary trie and "prefix indexing" trick
- Improving LZW compression rate with variable bit size encoding
- *Trying* to improve overall performance by being smarter about how the I/O
  class writes offset bytes, but disappointingly no improvement was observed

#### Next in line

- I'd like to think that core functionality and DYI data structures are done
  now? So, just wrapping up loose ends
- In particular, performance testing and analysis of the results

#### Issues and questions

- None at this time... I'd appreciate suggestions on what to focus on foremost
  with the remaining time? (Is something important missing or lacking?)
