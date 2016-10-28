package com.rferrer.asn1.lib

import scala.Stream._
import com.rferrer.asn1.lib.Asn1DecoderData._

/**
  * Reference: https://en.wikipedia.org/wiki/X.690#BER_encoding
  */
object Asn1Decoder {

  // Get a stream of decoded Data objects
  def getDataStream(bytes: Array[Byte], offset: Int): Stream[(Data, NextOffset)] = {
    if (offset >= bytes.length) {
      empty /* terminate stream */
    } else {
      val (data, newOffset) = decodeData(bytes, offset)
      // Do recursion to build stream
      (data, newOffset) #:: getDataStream(bytes, newOffset)
    }
  }

  // Decode "identifier", "length" and "contents" (if available)
  def decodeData(bytes: Array[Byte], index: Int): (Data, NextOffset) = {
    val (identifier, next1) = getIdentifier(bytes, index)
    val (length, next2) = getLength(bytes, next1)
    val hasContents = (identifier.primitiveOrConstructed == Primitive) && length.length > 0
    val (contents, next3) = hasContents match {
      case true =>
        val (theContents, theNext) = getContents(bytes, next2, length.length)
        (Some(theContents), theNext)
      case false => (None, next2) // No contents, offset doesn't change
    }
    (Data(identifier, length, contents), next3)
  }

  def getIdentifier(bytes: Array[Byte], index: Int): (Identifier, NextOffset) = {
    val octet1 = Util.byteToUnsignedInt(bytes(index))
    val offset = index
    val tagClassValue = (octet1 & 0xc0) >> 6
    val tagClass = valueToTagClass(tagClassValue)
    val primitiveOrConstructed = {
      val pc = (octet1 & 0x20) >> 5
      if (pc == 1) Constructed else Primitive
    }
    val (tagNumber, length) = {
      octet1 & 0x1f match {
        case 31 =>
          val octet2 = Util.byteToUnsignedInt(bytes(index+1))
          octet2 match {
            case i if i > 127 =>
              val octet3 = Util.byteToUnsignedInt(bytes(index+2))
              val tagNumber = ((octet2 & 0x7F) << 7) + octet3
              // TODO: Not implemented for additional octets
              require(octet3 <= 127, "Octet 3 has more: " + Util.byteArraytoHexString(bytes.slice(index+1, index+4), " "))
              (tagNumber, 3)
            case i => (i, 2)
          }
        case i => (i, 1)
      }
    }
    (Identifier(offset, tagClass, primitiveOrConstructed, tagNumber), index + length)
  }

  def getLength(bytes: Array[Byte], index: Int): (Length, NextOffset) = {
    val octet = Util.byteToUnsignedInt(bytes(index))
    octet match {
      case _ if (octet & 0x80) == 0 => (Length(DefinitiveShort, octet), index + 1)
      case _ if octet == 0x80 => (Length(Indefinite, 0), index + 1)
      case _ if octet == 0xFF => (Length(Reserved, 0), index + 1)
      case _ =>
        val numberOfFollowingOctects = octet & 0x7f
        val lengthValue = Util.bytesToNumber(bytes.slice(index+1, index+1+numberOfFollowingOctects))
        require(numberOfFollowingOctects >= 1 && numberOfFollowingOctects <= 126)
        (Length(DefinitiveLong, lengthValue), index + 1 + numberOfFollowingOctects)
    }
  }

  def getContents(bytes: Array[Byte], index: Int, length: Int): (Contents, NextOffset) = {
    val chunk = bytes.slice(index, index + length)
    Util.getStringIfReadable(chunk) match {
      case Some(str) => (ContentsString(chunk, str), index + length)
      case None => (ContentsBytes(chunk), index + length)
    }
  }

}
