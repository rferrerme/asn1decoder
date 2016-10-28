package com.rferrer.asn1.examples

import com.rferrer.asn1.lib.Asn1DecoderData.{ContentsString, ContentsBytes}
import com.rferrer.asn1.lib.{Util, Asn1Decoder}

import scala.collection.mutable

/**
  * This shows how to generate an output similar to "dumpasn1.c".
  */
object Example2 {

  def main(args: Array[String]) {

    if (args.length != 1) {
      println("Usage: java -cp target/scala-2.11/asn1decoder-assembly-1.0.jar com.rferrer.asn1.examples.Example2 <filename>")
      System.exit(0)
    }

    import java.nio.file.{Files, Paths}

    // Note: Whole file is loaded into memory
    val bytes: Array[Byte] = Files.readAllBytes(Paths.get(args(0)))

    // Dump

    var indent = 0
    val endingsOfConstructed: mutable.Stack[Int] = mutable.Stack()

    Asn1Decoder.getDataStream(bytes, 0).foreach { case (data, next) =>

      // Generate different parts
      val offsetStr = f"${data.identifier.offset}%3d"
      val lengthStr = f"${data.length.length}%3d"
      val padding = " " * indent
      val tagNumberStr = s"[${data.identifier.tagNumber}]"
      // If universal, and type name available, use universal type name. Otherwise use tag number.
      val tagNumberOrName = data.getUniversalTypeName.getOrElse(tagNumberStr)
      val brace = if (data.isConstructed) "{" else ""
      val contents = data.contents match {
        case Some(ContentsBytes(theBytes)) => Util.byteArraytoHexString(theBytes, " ")
        case Some(ContentsString(_, theString)) => s"'$theString'"
        case _ => ""
      }

      if (!data.isEnd) {
        // Print line
        println(s"$offsetStr $lengthStr: $padding$tagNumberOrName $brace$contents")
      }

      // Adjust indent
      if (data.isConstructed) {
        indent += 2
        val length = data.length.length
        if (length != 0) {
          endingsOfConstructed.push(next + data.length.length)
        }
      }
      else if (data.isEnd) {
        // End of constructed found in the decoded data
        showEndOfConstructed(indent, offsetStr)
        indent -= 2
      }
      while (endingsOfConstructed.nonEmpty && next == endingsOfConstructed.head) {
        // Next will be end of constructed and it appears in stack
        showEndOfConstructed(indent, offsetStr)
        indent -= 2
        endingsOfConstructed.pop()
      }
    }
  }

  // Show "}" in the right position
  def showEndOfConstructed(indent: Int, offsetStr: String): Unit = {
    val prePadding = " " * (offsetStr.length + 1 + 3)
    val newPadding = " " * indent
    println(s"$prePadding: $newPadding}")
  }

}
