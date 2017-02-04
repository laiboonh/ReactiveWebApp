package services

import play.api.{Configuration, Play}
import play.api.libs.oauth.{ConsumerKey, OAuthCalculator, RequestToken}
import play.api.libs.ws.WSClient
import scaldi.{Injectable, Injector}

import scala.concurrent.{ExecutionContext, Future}

trait TwitterService {
  def fetchRelationshipCounts(userName: String)(implicit ec: ExecutionContext): Future[TwitterCounts]

  def postTweet(message: String)(implicit ec: ExecutionContext): Future[Unit]
}

case class TwitterCounts(followersCount: Long, friendsCount: Long)

class DefaultTwitterService(implicit inj: Injector) extends TwitterService with Injectable {
  private val ws: WSClient = inject[WSClient]
  private val config: Configuration = inject[Configuration]

  override def fetchRelationshipCounts(userName: String)(implicit ec: ExecutionContext): Future[TwitterCounts] = {
    val credentials: Option[(ConsumerKey, RequestToken)] = for {
      apiKey <- config.getString("twitter.apiKey")
      apiSecret <- config.getString("twitter.apiSecret")
      token <- config.getString("twitter.token")
      tokenSecret <- config.getString("twitter.tokenSecret")
    } yield (ConsumerKey(apiKey, apiSecret), RequestToken(token, tokenSecret))

    credentials.map {
      case (consumerKey, requestToken) =>
        ws.url("https://api.twitter.com/1.1/users/show.json")
          .sign(OAuthCalculator(consumerKey, requestToken))
          .withQueryString("screen_name" -> userName)
          .get().map {
          response =>
            if (response.status == 200) {
              TwitterCounts(
                (response.json \ "followers_count").as[Long],
                (response.json \ "friends_count").as[Long]
              )
            } else {
              throw new TwitterServiceException(s"Could not retrieve counts for Twitter user $userName")
            }
        }
    }.getOrElse {
      Future.failed(new TwitterServiceException("You did not correctly configure the Twitter credentials"))
    }

  }

  override def postTweet(message: String)(implicit ec: ExecutionContext): Future[Unit] = ???
}

case class TwitterServiceException(message: String) extends RuntimeException(message)
