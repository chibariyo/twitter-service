package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

/**
  * Created by david on 17/02/20.
  */
class UserFollowersCounter(userFollowersCounter: ActorRef, storage: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case message => // do nothing
  }
}

object UserFollowersCounter {
  def props(userFollowersCounter: ActorRef, storage: ActorRef) =
    Props(classOf[UserFollowersCounter], userFollowersCounter, storage)
}
