package actors

import akka.actor.{Actor, ActorLogging, Props}
import messages.StoreReach
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.core.errors.ConnectionException
import org.joda.time.DateTime

/**
  * Created by david on 17/02/20.
  */
class Storage extends Actor with ActorLogging {

  val Database = "twitterService"
  val ReachCollection = "ComputedReach"

  implicit val executionContext = context.dispatcher

  val driver: MongoDriver = new MongoDriver
  var connection: MongoConnection = _
  var db: DefaultDB = _
  var collection: BSONCollection = _
  obtainConnection()

  override def postRestart(reason: Throwable): Unit = {
    reason match {
      case ce: ConnectionException =>
        // try to obtain a brand new connection
        obtainConnection()
    }
    super.postRestart(reason)
  }

  override def postStop(): Unit = {
    connection.close()
    driver.close()
  }

  def receive = {
    case StoreReach(tweetId, score) => // TODO
  }

  private def obtainConnection(): Unit = {
    connection = driver.connection(List("localhost"))
    db = connection.db(Database)
    collection = db.collection[BSONCollection](ReachCollection)
  }
}

case class StoredReach(when: DateTime, tweetId: BigInt, score: Int)

object Storage {
  def props = Props[Storage]
}