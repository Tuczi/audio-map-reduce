package messages

import java.io.File

import akka.actor.ActorRef
import audio.AudioSample

import scala.concurrent.Future

/**
  * Created by tkuczma on 27.11.15.
  */
sealed trait Message

case class MapRequestMessage(data: AudioSample, filterFunction: AudioSample => AudioSample) extends Message

case class MapResponseMessage(data: AudioSample) extends Message

case class ProcessFileRequestMessage(from: File, to: File, filterFunction: AudioSample => AudioSample) extends Message

case class ProcessFileResponseMessage(status: Int) extends Message

case class RetryData(mapper: ActorRef, message: MapRequestMessage, future: Future[Any])

case class Retry(retryDataStream: Stream[RetryData], inFile: File, outFile: File) extends Message

case class Store(results: Seq[AudioSample], inFile: File, outFile: File)
