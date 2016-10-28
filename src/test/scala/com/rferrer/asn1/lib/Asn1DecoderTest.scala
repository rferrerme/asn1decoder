package com.rferrer.asn1.lib

import java.util

import org.scalatest._
import com.rferrer.asn1.lib.Asn1DecoderData._

class Asn1DecoderTest extends FlatSpec {

  "Decoder" should "decode indefinite sequence #1" in {
    val bytes = Array(0x30.toByte, 0x80.toByte)
    val (identifier, next) = Asn1Decoder.getIdentifier(bytes, 0)
    assert(identifier == Identifier(0, Universal, Constructed, universalTypeNameToTagNumber("SEQUENCE")))
    assert(identifier.getUniversalTypeName.get == "SEQUENCE")
    assert(next == 1)
    val (length, next2) = Asn1Decoder.getLength(bytes, next)
    assert(length == Length(Indefinite, 0))
    assert(next2 == 2)
    val (data, _) = Asn1Decoder.decodeData(bytes, 0)
    assert(data.isConstructed)
    assert(data.isUniversal)
    assert(data.getUniversalTypeName.get == "SEQUENCE")
  }

  it should "decode indefinite sequence #2" in {
    val bytes = Array(0x30.toByte, 0x80.toByte)
    val (result, next) = Asn1Decoder.decodeData(bytes, 0)
    assert(result.identifier == Identifier(0, Universal, Constructed, universalTypeNameToTagNumber("SEQUENCE")))
    assert(result.length == Length(Indefinite, 0))
    assert(next == 2)
  }

  it should "decode indefinite constructed" in {
    val bytes = Array(0xa0.toByte, 0x80.toByte)
    val (identifier, next) = Asn1Decoder.getIdentifier(bytes, 0)
    assert(identifier == Identifier(0, ContextSpecific, Constructed, 0))
    assert(next == 1)
    val (length, next2) = Asn1Decoder.getLength(bytes, next)
    assert(length == Length(Indefinite, 0))
    assert(next2 == 2)
  }

  it should "decode primitive length 1" in {
    val bytes = Array(0x80.toByte, 0x01.toByte, 0x01.toByte)
    val (identifier, next) = Asn1Decoder.getIdentifier(bytes, 0)
    assert(identifier == Identifier(0, ContextSpecific, Primitive, 0))
    assert(next == 1)
    val (length, next2) = Asn1Decoder.getLength(bytes, next)
    assert(length == Length(DefinitiveShort, 1))
    assert(next2 == 2)
    val (contents, next3) = Asn1Decoder.getContents(bytes, next2, length.length)
    assert(util.Arrays.equals(contents.bytes, Array(0x01.toByte)))
    assert(next3 == 3)
  }

  it should "decode primitive length 1 (using data stream)" in {
    val bytes = Array(0x80.toByte, 0x01.toByte, 0x01.toByte)
    Asn1Decoder.getDataStream(bytes, 0).foreach { case (data, next) =>
      assert(data.identifier == Identifier(0, ContextSpecific, Primitive, 0))
      assert(data.length == Length(DefinitiveShort, 1))
      assert(util.Arrays.equals(data.contents.get.bytes, Array(0x01.toByte)))
      assert(data.isPrimitive)
    }
  }

  it should "decode tag number with more" in {
    val bytes = Array(0x9f.toByte, 0x21.toByte, 0x01.toByte, 0x03.toByte)
    val (identifier, next) = Asn1Decoder.getIdentifier(bytes, 0)
    assert(identifier == Identifier(0, ContextSpecific, Primitive, 33))
    assert(next == 2)
    val (length, next2) = Asn1Decoder.getLength(bytes, next)
    assert(length == Length(DefinitiveShort, 1))
    assert(next2 == 3)
    val (contents, next3) = Asn1Decoder.getContents(bytes, next2, length.length)
    assert(util.Arrays.equals(contents.bytes, Array(0x03.toByte)))
    assert(contents.toString == "ContentsBytes([03])")
    assert(next3 == 4)
  }

  it should "decode string #1" in {
    val bytes = Array(0x83.toByte, 0x02.toByte, 0x41.toByte, 0x42.toByte)
    val (identifier, next) = Asn1Decoder.getIdentifier(bytes, 0)
    assert(identifier == Identifier(0, ContextSpecific, Primitive, 3))
    assert(next == 1)
    val (length, next2) = Asn1Decoder.getLength(bytes, next)
    assert(length == Length(DefinitiveShort, 2))
    assert(next2 == 2)
    val (contents, next3) = Asn1Decoder.getContents(bytes, next2, length.length)
    assert(util.Arrays.equals(contents.bytes, Array(0x41.toByte, 0x42.toByte)))
    contents match {
      case ContentsString(_, str) => assert(str == "AB")
      case _ => throw new RuntimeException("ContentsString expected")
    }
    assert(contents.toString == """ContentsString("AB",[41,42])""")
    assert(next3 == 4)
  }

  it should "decode string #2" in {
    val bytes = Array(0x83.toByte, 0x02.toByte, 0x41.toByte, 0x42.toByte)
    val (result, next) = Asn1Decoder.decodeData(bytes, 0)
    assert(result.identifier == Identifier(0, ContextSpecific, Primitive, 3))
    assert(result.length == Length(DefinitiveShort, 2))
    assert(util.Arrays.equals(result.contents.get.bytes, Array(0x41.toByte, 0x42.toByte)))
    result.contents.get match {
      case ContentsString(_, str) => assert(str == "AB")
      case _ => throw new RuntimeException("ContentsString expected")
    }
    assert(result.contents.get.toString == """ContentsString("AB",[41,42])""")
    assert(next == 4)
  }

  it should "decode identifier with more that 1 octet" in {
    val bytes = Array(0x9f.toByte, 0x81.toByte, 0x0b.toByte, 0x01.toByte, 0x3e.toByte)
    val (result, next) = Asn1Decoder.decodeData(bytes, 0)
    assert(result.identifier == Identifier(0, ContextSpecific, Primitive, 139))
    assert(result.length == Length(DefinitiveShort, 1))
    assert(util.Arrays.equals(result.contents.get.bytes, Array(0x3e.toByte)))
    assert(next == 5)
  }

  it should "decode length definite long" in {
    val bytes = Array(0x81.toByte, 0xab.toByte)
    val (length, next) = Asn1Decoder.getLength(bytes, 0)
    assert(length == Length(DefinitiveLong, 171))
    assert(next == 2)
  }

  it should "decode end" in {
    val bytes = Array(0x00.toByte, 0x00.toByte)
    val (data, _) = Asn1Decoder.decodeData(bytes, 0)
    assert(data.isEnd)
  }

  "byteToUnsignedInt" should "work return unsigned values" in {
    assert(Util.byteToUnsignedInt(0x01.toByte) == 1)
    assert(Util.byteToUnsignedInt(0xFF.toByte) == 255)
  }

  "bytesToInt16" should "convert to 16-bit integer" in {
    assert(Util.bytesToNumber(Array(0x01.toByte, 0x02.toByte)) == 258)
  }

  "byteArraytoHexString" should "use separator" in {
    assert(Util.byteArraytoHexString(Array(0x01.toByte, 0x02.toByte), ",") == "01,02")
  }
}
