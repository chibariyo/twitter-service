package actors

import javax.naming.ServiceUnavailableException

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props, SupervisorStrategy, Terminated}
import reactivemongo.core.errors.ConnectionException

import scala.concurrent.duration._
import messages._
import StatisticsProvider._

/**
  * Created by david on 17/02/17.
  */
class StatisticsProvider extends Actor with ActorLogging {

  var reachComputer: ActorRef = _
  var storage: ActorRef = _
  var followersCounter: ActorRef = _

  override def preStart(): Unit = {
    log.info("Starting StatisticsProvider")
    followersCounter = context.actorOf(UserFollowersCounter.props, name = "userFollowersCounter")
    storage = context.actorOf(Storage.props, name = "storage")
    reachComputer = context.actorOf(TweetReachComputer.props(followersCounter, storage), name = "tweetReachComputer")

    context.watch(storage)
  }

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 2.minutes) {
      case _: ConnectionException =>
        Restart
      case t: Throwable =>
        super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

  def receive = {
    case reach: ComputeReach =>
      reachComputer forward reach
    case Terminated(terminatedStorageRef) =>
      context.system.scheduler.scheduleOnce(1.minute, self, ReviveStorage)
      context.become(storageUnavailable)
  }

  def storageUnavailable: Receive = {
    case ComputeReach(_) =>
      sender() ! ServiceUnavailable
    case ReviveStorage =>
      storage = context.actorOf(Storage.props, name = "storage")
      context.unbecome()
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

  case object ServiceUnavailable
  case object ReviveStorage
}
