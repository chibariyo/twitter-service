package actors

import akka.actor.{Actor, ActorLogging, Props}

/**
  * Created by david on 17/02/20.
  */
class TweetReachComputer extends Actor with ActorLogging {

  def receive = {
    case message => // do nothing
  }
}

object TweetReachComputer {
  def props = Props[TweetReachComputer]
}