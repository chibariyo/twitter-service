package actors

import akka.actor.{Actor, ActorLogging, Props}

/**
  * Created by david on 17/02/17.
  */
class StatisticsProvider extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.info("Hello, world.")
  }

  def receive = {
    case message => // do nothing
  }
}

object StatisticsProvider {
  def props = Props[StatisticsProvider]
}
