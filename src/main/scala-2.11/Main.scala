import java.io.File

import actors.{AlwaysFailingMapActor, MapActor, ReduceActor}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import audio.LowPassAudioSampleFilters
import messages.ProcessFileRequestMessage
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._

/**
  * Created by tkuczma on 27.11.15.
  *
  * 8. Map-reduce sound editor
  *
  * System zawiera jednego klienta i n serwerów. Klient przetwarza m plików muzycznych f1, f2, ... fm.
  * Przetwarzanie pliku przez klienta oznacza, że dla każdego pliku fi klient wyznacza listę filtrów (F(fi))
  * które mają zostac zaaplikowane do fi. Nastepnie klient dzieli każdy plik fi na k podsekwencji.
  * Każda podsekwencja jest przydzielona do konkretnego serwera, który nakłada filtry F(fi) na podsekwencje.
  * Ostatecznie klient składa wszystkie podsekwencje z zaaplikowanymi filtrami w plik fi’.
  * System minimalizuje czas przetwarzania wszystkich plików (throughput).
  *
  * Free example:
  * https://fma-files.s3.amazonaws.com/music%2FccCommunity%2FKai_Engel%2FChapter_One__Cold%2FKai_Engel_-_01_-_Snowfall_Intro.mp3?AWSAccessKeyId=13EXTDHE1ETARJ2812R2&Expires=1448729342&Signature=1SBFmxrqftDeX4wBXm8Gg5NrN44%3D
  *
  */
object Main extends App {
  def main() = {
    implicit val timeout = Timeout(5 seconds)

    val system = ActorSystem("MapReduceSoundEditorSystem")
    val mappers = 1 until 10 map (i => factorMapActor(system, i))
    val reducer = system.actorOf(Props(new ReduceActor(mappers)), name = "reducer")

    val filesMessages = List(ProcessFileRequestMessage(new File("samples/Kai_Engel_-_01_-_Snowfall_Intro.wav"), new File("samples/Kai_Engel_-_01_-_Snowfall_Intro2.wav"), LowPassAudioSampleFilters))
    val futures = filesMessages map (m => reducer ! m)
    //val results = futures map (f => Await.result(f, timeout duration).asInstanceOf[ProcessFileResponseMessage])

    println("End main")
  }

  def factorMapActor(system: ActorSystem, index: Int): ActorRef = {
    val failingActors = Array(3, 5, 7)
    if (failingActors contains index)
      system.actorOf(Props[AlwaysFailingMapActor], name = "mapper_" + index)
    else
      system.actorOf(Props[MapActor], name = "mapper_" + index)
  }

  main()
}
