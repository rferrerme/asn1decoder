## asn1decoder

This is an educational BER decoder based on [https://en.wikipedia.org/wiki/X.690#BER_encoding](https://en.wikipedia.org/wiki/X.690#BER_encoding).

#### Features
* It performs basic decoding, generating a stream of `Data` objects that have `identifier`, `length`, and `contents` (if present)
* That first decoding sets the foundation to write more sophisticated decoders
* It is implemented in Scala but it is simple enough to be easily ported to your preferred language
* One of the examples (`com.rferrer.asn1.examples.Example2`) produces an output similar to [dumpasn1](https://www.cs.auckland.ac.nz/~pgut001/dumpasn1.c)

_Notes_:
* This is a _work in progress_, so don't expect a complete implementation...
* Although very simple, it is able to decode files that many other ASN.1 decoders will refuse to decode

#### How to use it

To generate the JAR:
* `sbt assembly`

To run the tests:
* `sbt test`

Finally, to run the examples:
* `java -cp target/scala-2.11/asn1decoder-assembly-1.0.jar com.rferrer.asn1.examples.Example1 <filename>`
* `java -cp target/scala-2.11/asn1decoder-assembly-1.0.jar com.rferrer.asn1.examples.Example2 <filename>`

#### Examples

"Example1" simply produces a stream of `Data` objects from the information in the input file and prints those objects:
```
Data(Identifier(0,Universal,Constructed,SEQUENCE),Length(Indefinite,0),None)
Data(Identifier(2,ContextSpecific,Constructed,0),Length(Indefinite,0),None)
Data(Identifier(4,ContextSpecific,Primitive,0),Length(DefinitiveShort,1),Some(ContentsBytes([01])))
...
```

"Example2" produces an output similar to [dumpasn1](https://www.cs.auckland.ac.nz/~pgut001/dumpasn1.c):
```
  0   0: SEQUENCE {
  2   0:   [0] {
  4   1:     [0] 01
...
```

#### How to write your own decoder

Check the source code of the examples.

**"Example1"** shows how to simply read a file and decode the array of bytes to generate a stream of `Data` objects:
```
    import java.nio.file.{Files, Paths}

    // Note: Whole file is loaded into memory
    val bytes: Array[Byte] = Files.readAllBytes(Paths.get(args(0)))

    // Dump
    Asn1Decoder.getDataStream(bytes, 0).foreach { case (data, next) =>
      println(data)
    }
```

The result is very close to what they describe in [https://en.wikipedia.org/wiki/X.690#BER_encoding](https://en.wikipedia.org/wiki/X.690#BER_encoding).

**"Example2"** is a further step that uses the stream of `Data` objects to generate an output similar to [dumpasn1](https://www.cs.auckland.ac.nz/~pgut001/dumpasn1.c).

It helps to understand how to extract information from the `Data` objects and provides simple mechanisms to indent the information and to realize when constructed data without length information ends.