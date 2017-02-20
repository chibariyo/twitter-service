package messages

/**
  * Created by david on 17/02/20.
  */
case class ComputeReach(tweetId: BigInt)
case class TweetReach(tweetId: BigInt, score: Int)

case class FetchFollowerCount(user: String)
case class FollowerCount(user: String, followersCount: Int)

case class StoreReach(tweetId: BigInt, score: Int)
case class ReachStored(tweetId: BigInt)
