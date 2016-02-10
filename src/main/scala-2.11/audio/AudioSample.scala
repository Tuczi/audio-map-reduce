package audio

import javax.sound.sampled.AudioFormat

/**
  * Created by tkuczma on 29.11.15.
  */
case class Frame(data: Array[Byte])

case class AudioSample(data: Array[Frame], format: AudioFormat) {
  /**
    * @param bytesArray array of data(bytes). Clone this array - it is reused by caller
    */
  def this(bytesArray: Array[Byte], format: AudioFormat) = this(bytesArray grouped format.getFrameSize map Frame toArray, format)

  def this(intSeq: Array[Int], format: AudioFormat) = this(intSeq.flatMap(it => intToByteArray(it, format)), format)

  def flatten = data flatMap (_ data)

  def groupedToInt = data flatMap (_.data grouped (format.getFrameSize / format.getChannels) map byteArrayToInt)

  def byteArrayToInt(v: Array[Byte]) = v.zipWithIndex.map(it => (0xff & it._1) << (8 * it._2)).reduce((it, acc) => it | acc)
}

object intToByteArray extends ((Int, AudioFormat) => Array[Byte]) {
  def apply(v: Int, format: AudioFormat) = 0.until(format.getFrameSize / format.getChannels).map(it => (v >>> (8 * it)).toByte).toArray
}
