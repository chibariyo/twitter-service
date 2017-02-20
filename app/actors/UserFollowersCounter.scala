package actors

import akka.actor.{Actor, ActorLogging, Props}

/**
  * Created by david on 17/02/20.
  */
class UserFollowersCounter extends Actor with ActorLogging {

  def receive = {
    case message => // do nothing
  }
}

object UserFollowersCounter {
  def props = Props[UserFollowersCounter]
}
