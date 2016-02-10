package actors

import akka.actor.{Actor, Kill}
import messages._

/**
  * Created by tkuczma on 27.11.15.
  */
class MapActor extends Actor {
  override def receive = {
    case MapRequestMessage(data, filterFunction) => {
      //println("Mapping on " + self.path.name)
      sender ! MapResponseMessage(filterFunction(data))
    }
  }
}

/**
  * Only for tests!
  */
class AlwaysFailingMapActor extends MapActor {
  override def receive = {
    case MapRequestMessage(data, filterFunction) => {
      //println("Fail on " + self.path.name)
      self ! Kill
    }
  }
}
