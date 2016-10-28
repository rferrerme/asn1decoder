package com.rferrer.asn1.lib

object Util {

  def byteToUnsignedInt(b: Byte) = {
    val i = b.toInt
    if (i < 0) i+256 else i
  }

  // Example: Array(0x01.toByte, 0x02.toByte)) -> 258
  def bytesToNumber(bytes: Array[Byte]) = {
    bytes.foldLeft(0) { case (acum, b) => acum*256+byteToUnsignedInt(b) }
  }

  // Example: Array(0x01.toByte, 0x02.toByte) with "," as separator -> "01,02"
  def byteArraytoHexString(bytes: Array[Byte], separator: String) = {
    bytes.map(byteToUnsignedInt).map(i => f"$i%02X").mkString(separator)
  }

  // If string characters are readable then return Some(bytesAsString), otherwise return None
  def getStringIfReadable(bytes: Array[Byte]): Option[String] = {
    val ascii = !bytes.map(Util.byteToUnsignedInt).exists(i => i < 32 || i >= 127)
    if (ascii) Some(new String(bytes.map(_.toChar))) else None
  }

}
