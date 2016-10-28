package com.rferrer.asn1.examples

import com.rferrer.asn1.lib.Asn1Decoder

/**
  * This shows how to use Asn1Decoder to get a stream of Data objects.
  * Data objects are just printed.
  */
object Example1 {

  def main(args: Array[String]) {

    if (args.length != 1) {
      println("Usage: java -cp target/scala-2.11/asn1decoder-assembly-1.0.jar com.rferrer.asn1.examples.Example1 <filename>")
      System.exit(0)
    }

    import java.nio.file.{Files, Paths}

    // Note: Whole file is loaded into memory
    val bytes: Array[Byte] = Files.readAllBytes(Paths.get(args(0)))

    // Dump
    Asn1Decoder.getDataStream(bytes, 0).foreach { case (data, next) =>
      println(data)
    }
  }

}
