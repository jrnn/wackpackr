## Week #1

#### Main activities

- Picking a topic and trying to understand what it is about
- Basic project setup
- Baby steps with Huffman

#### Next in line

- Extending and refining Huffman implementation
- Replacing PriorityQueue with a DYI data structure
- Unit testing

#### Issues and questions

- None so far

## Week #2

#### Main activities

- Extending and restructuring Huffman code, should be more or less done now(?)
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
  - Was surprised to learn that the standard Input- and OutputStreams do not
    operate below the byte level: putting together an "add-on" tool that can do
    I/O on binary level was a very instructive experience
- Would like to check if I'm at this point using something from standard library
  that should be replaced with DYI implementations. Are the following legal?
  - java.io.ByteArrayInputStream
  - java.io.ByteArrayOutputStream
  - java.util.Arrays
- Also, is it OK to resort to standard library in test classes?
