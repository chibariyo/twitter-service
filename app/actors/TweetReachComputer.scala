package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import messages._
import play.api.libs.json.JsArray
import play.api.libs.oauth.OAuthCalculator
import play.api.libs.ws.WS

import scala.concurrent.Future
import akka.pattern.pipe

import scala.util.control.NonFatal


/**
  * Created by david on 17/02/20.
  */
class TweetReachComputer(userFollowersCounter: ActorRef, storage: ActorRef) extends Actor with ActorLogging
  with TwitterCredentials {
  implicit val executionContext = context.dispatcher

  var followerCountsByRetweet = Map.empty[FetchedRetweet, List[FollowerCount]]

  def receive = {

    case ComputeReach(tweetId) =>
      val originalSender = sender()
      fetchRetweets(tweetId, sender()).recover {
        case NonFatal(t) =>
          RetweetFetchingFailed(tweetId, t, originalSender)
      } pipeTo self
    case fetchedRetweets: FetchedRetweet =>
        followerCountsByRetweet =
          followerCountsByRetweet + (fetchedRetweets -> List.empty)
        fetchedRetweets.retweeters.foreach { rt =>
          userFollowersCounter ! FetchFollowerCount(fetchedRetweets.tweetId, rt)
        }
    case count @ FollowerCount(tweetId, _, _) =>
      log.info("Received followers count for tweet {}", tweetId)
      fetchedRetweetsFor(tweetId).foreach { fetchedRetweets =>
        updateFollowersCount(tweetId, fetchedRetweets, count)
      }
    case ReachStored(tweetId) =>
      fetchedRetweetsFor(tweetId).foreach { key =>
          followerCountsByRetweet = followerCountsByRetweet.filterNot(_._1 == key)
      }
  }

  case class FetchedRetweet(tweetId: BigInt, retweeters: List[BigInt], client: ActorRef)
  case class RetweetFetchingFailed(tweetId: BigInt, cause: Throwable, client: ActorRef)

  def fetchedRetweetsFor(tweetId: BigInt) = followerCountsByRetweet.keys.find(_.tweetId == tweetId)

  def updateFollowersCount(tweetId: BigInt, fetchedRetweets: FetchedRetweet, count: FollowerCount) = {
    val existingCounts = followerCountsByRetweet(fetchedRetweets)
    followerCountsByRetweet = followerCountsByRetweet.updated(fetchedRetweets, count :: existingCounts)
    val newCounts = followerCountsByRetweet(fetchedRetweets)
    if (newCounts.length == fetchedRetweets.retweeters.length) {
      log.info("Received all retweeters followers count for tweet {}, computing sum", tweetId)
      val score = newCounts.map(_.followersCount).sum
      fetchedRetweets.client ! TweetReach(tweetId, score)
      storage ! StoreReach(tweetId, score)
    }
  }

  def fetchRetweets(tweetId: BigInt, client: ActorRef): Future[FetchedRetweet] = {
    credentials.map {
      case (consumerKey, requestToken) =>
        WS.url("https://api.twitter.com/1.1/statuses/retweeters/ids.json")
          .sign(OAuthCalculator(consumerKey, requestToken))
          .withQueryString("id" -> tweetId.toString)
          .withQueryString("stringify_ids" -> "true")
          .get().map { response =>
          if (response.status == 200) {
            val ids = (response.json \ "ids").as[JsArray].value.map(v => BigInt(v.as[String])).toList
            FetchedRetweet(tweetId, ids, client)
          } else {
            throw new RuntimeException(s"Could not retrieve details for Tweet $tweetId")
          }
        }
    }.getOrElse {
      Future.failed(new RuntimeException("You did not correctly configure the Twitter credentials"))
    }
  }
}

object TweetReachComputer {
  def props(userFollowersCounter: ActorRef, storage: ActorRef) =
    Props(classOf[UserFollowersCounter], userFollowersCounter, storage)
}