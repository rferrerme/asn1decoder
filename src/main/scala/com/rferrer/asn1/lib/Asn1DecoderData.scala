package com.rferrer.asn1.lib

/**
  * Reference: https://en.wikipedia.org/wiki/X.690#BER_encoding
  */
object Asn1DecoderData {

  type NextOffset = Int

  sealed trait TagClass
  case object Universal extends TagClass { def name = "Universal" }
  case object Application extends TagClass { def name = "Application" }
  case object ContextSpecific extends TagClass { def name = "Context-specific" }
  case object Private extends TagClass { def name = "Private" }

  def valueToTagClass(value: Int): TagClass = value match {
    case 0 => Universal
    case 1 => Application
    case 2 => ContextSpecific
    case 3 => Private
    case _ => throw new RuntimeException(s"Invalid tag class value: $value")
  }

  val tagNumberToUniversalTypeName: Map[Int, String] = Map(
    0 -> "End-of-Content (EOC)",
    1 -> "BOOLEAN",
    2 -> "INTEGER",
    3 -> "BIT STRING",
    4 -> "OCTET STRING",
    5 -> "NULL",
    6 -> "OBJECT IDENTIFIER",
    7 -> "Object Descriptor",
    8 -> "EXTERNAL",
    9 -> "REAL (float)",
    10 -> "ENUMERATED",
    11 -> "EMBEDDED PDV",
    12 -> "UTF8String",
    13 -> "RELATIVE-OID",
    14 -> "(reserved)",
    15 -> "(reserved)",
    16 -> "SEQUENCE",
    17 -> "SET",
    18 -> "NumericString",
    19 -> "PrintableString",
    20 -> "T61String",
    21 -> "VideotexString",
    22 -> "IA5String",
    23 -> "UTCTime",
    24 -> "GeneralizedTime",
    25 -> "GraphicString",
    26 -> "VisibleString",
    27 -> "GeneralString",
    28 -> "UniversalString",
    29 -> "CHARACTER STRING",
    30 -> "BMPString"
  )
  // Reverse map
  val universalTypeNameToTagNumber = tagNumberToUniversalTypeName.map { case (number, name) => (name, number) }

  sealed trait PrimitiveOrConstructed
  case object Primitive extends PrimitiveOrConstructed { def name = "Primitive" }
  case object Constructed extends PrimitiveOrConstructed { def name = "Constructed" }

  sealed trait Form
  case object DefinitiveShort extends Form { def name = "Definite, short" }
  case object Indefinite extends Form { def name = "Indefinite" }
  case object DefinitiveLong extends Form { def name = "Definite, long" }
  case object Reserved extends Form { def name = "Reserved" }

  case class Identifier(offset: Int, tagClass: TagClass, primitiveOrConstructed: PrimitiveOrConstructed, tagNumber: Int) {
    def getUniversalTypeName = tagNumberToUniversalTypeName.get(tagNumber)
  }

  case class Length(form: Form, length: Int)

  trait Contents {
    val bytes: Array[Byte]
  }
  case class ContentsBytes(bytes: Array[Byte]) extends Contents {
    override def toString = {
      val value = Util.byteArraytoHexString(bytes, ",")
      s"ContentsBytes([$value])"
    }
  }
  case class ContentsString(bytes: Array[Byte], str: String) extends Contents {
    override def toString = {
      val value = Util.byteArraytoHexString(bytes, ",")
      s"""ContentsString("$str",[$value])"""
    }
  }

  case class Data(identifier: Identifier, length: Length, contents: Option[Contents]) {
    def isConstructed = this.identifier.primitiveOrConstructed == Asn1DecoderData.Constructed
    def isPrimitive = this.identifier.primitiveOrConstructed == Asn1DecoderData.Primitive
    def isUniversal = this.identifier.tagClass == Asn1DecoderData.Universal
    def isEnd = this.isPrimitive && this.isUniversal && (this.identifier.tagNumber == 0)
    def getUniversalTypeName = this.identifier.getUniversalTypeName
  }

}
