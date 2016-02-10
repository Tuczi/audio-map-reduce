package actors

import javax.sound.sampled._

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import audio.AudioStream
import messages._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Failure


/**
  * Created by tkuczma on 27.11.15.
  */
class ReduceActor(mappers: Seq[ActorRef])(implicit timeout: Timeout, exec: ExecutionContext) extends Actor {
  override def receive: Receive = {
    case ProcessFileRequestMessage(inFile, outFile, filterFunction) =>
      //println("Start")
      val inStream = AudioStream.read(inFile)

      //println("Preparing messages")
      val messages = inStream.stream map (f => new MapRequestMessage(f, filterFunction))
      //println("Sending requests")
      val futures = (Stream continually mappers).flatten zip messages map { case (mapper, request) => mapper ? request }

      val retryDataStream = ((Stream continually mappers).flatten, messages, futures).zipped.toStream.map(it => new RetryData(it._1, it._2, it._3))
      self ! Retry(retryDataStream, inFile, outFile)

    case Store(results, inFile, outFile) =>
      AudioStream.store(outFile, results, AudioSystem.getAudioFileFormat(inFile).getFormat)
      sender ! ProcessFileResponseMessage(0)
    //println("End")

    /***
      * Retry after timeout on different mapper
      */
    case Retry(retryDataStream, inFile, outFile) =>
      //println("Retry")
      def isBadFuture(future: Future[Any]) = future.value.isDefined && future.value.get.isInstanceOf[Failure[Any]]
      def retryDataWithNewMapper(mapper: ActorRef, retryData: RetryData, goodMappers: Stream[ActorRef]): RetryData = {
        if (isBadFuture(retryData.future) || !goodMappers.contains(retryData.mapper))
          new RetryData(mapper, retryData.message, mapper ? retryData.message)
        else
          retryData
      }

      retryDataStream foreach (it => Await.ready(it.future, timeout duration))
      val badMappers = retryDataStream filter (it => isBadFuture(it.future)) map (_.mapper) distinct

      if (badMappers.isEmpty) {
        val results = retryDataStream map (_.future.value.get.get.asInstanceOf[MapResponseMessage].data)
        self ! Store(results, inFile, outFile)
      } else {
        val lastMappers = retryDataStream map (_.mapper) distinct
        val goodMappers = lastMappers diff badMappers

        val newRetryDataStream = (Stream continually goodMappers).flatten zip retryDataStream map (it => retryDataWithNewMapper(it._1, it._2, goodMappers))
        self ! Retry(newRetryDataStream, inFile, outFile)
      }
  }
}
