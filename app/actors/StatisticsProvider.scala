package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

/**
  * Created by david on 17/02/17.
  */
class StatisticsProvider extends Actor with ActorLogging {

  var reachComputer: ActorRef = _
  var storage: ActorRef = _
  var followersCounter: ActorRef = _

  override def preStart(): Unit = {
    log.info("Starting StatisticsProvider")
    followersCounter = context.actorOf(
      UserFollowersCounter.props,
      name = "userFollowersCounter"
    )
    storage = context.actorOf(
      Storage.props,
      name = "storage"
    )
    reachComputer = context.actorOf(
      TweetReachComputer.props(followersCounter, storage),
      name = "tweetReachComputer"
    )
  }

  def receive = {
    case message => // do nothing
  }

  override def unhandled(message: Any): Unit = {
    log.warning(
      "Unhandled message {} message from {}", message, sender()
    )
    super.unhandled(message)
  }
}

object StatisticsProvider {
  def props = Props[StatisticsProvider]
}
