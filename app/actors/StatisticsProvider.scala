package actors

import akka.actor.{Actor, ActorLogging, Props}

class StatisticsProvider extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.info("preStart StatisticsProvider")
  }

  override def receive: Receive = {
    case message:String =>  log.debug(message)
  }
  object StatisticsProvider {
    def props = Props[StatisticsProvider]
  }
}
