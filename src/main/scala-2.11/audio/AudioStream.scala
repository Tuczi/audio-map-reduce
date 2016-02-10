package audio

import java.io.{ByteArrayInputStream, File}
import javax.sound.sampled.{AudioFileFormat, AudioFormat, AudioInputStream, AudioSystem}

/**
  * Created by tkuczma on 29.11.15.
  */
object AudioStream {
  def read(inFile: File) = {
    val inputStream = AudioSystem getAudioInputStream inFile
    val bytesPerFrame = inputStream.getFormat.getFrameSize
    val channels = inputStream.getFormat.getChannels
    val bytes = 1024

    var array: Array[Byte] = (0 until bytes) map (x => 0 toByte) toArray

    val audioStream = Stream continually {
      val b = inputStream read array

      /** @important array is cloned inside! **/
      (new AudioSample(array, inputStream.getFormat), b)
    } takeWhile (_._2 != -1) map (_ _1)

    new WaveStream(audioStream)
  }

  def store(outFile: File, samples: Seq[AudioSample], format: AudioFormat) = {
    val data = samples flatMap (_ flatten) toArray
    val ais = new AudioInputStream(new ByteArrayInputStream(data), format, data.length / format.getFrameSize)

    AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outFile)
  }
}

case class WaveStream(stream: Stream[AudioSample])
