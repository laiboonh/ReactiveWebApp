package services

import models.StoredCounts
import org.joda.time.{DateTime, Period}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait StatisticsService {
  def createUserStatistics(userName: String)(implicit ec: ExecutionContext): Future[String]
}

class DefaultStatisticsService(statisticsRepository: StatisticsRepository, twitterService: TwitterService) extends StatisticsService {
  override def createUserStatistics(userName: String)(implicit ec: ExecutionContext): Future[String] = {

    def storeCounts(counts: (StoredCounts, TwitterCounts)): Future[(StoredCounts, TwitterCounts)] = counts match {
      case (_, current) =>
        statisticsRepository.storeCounts(StoredCounts(
          DateTime.now, userName, current.followersCount, current.friendsCount
        )).map(_ => counts)
    }

    def publishMessage(counts: (StoredCounts, TwitterCounts)): String = counts match {
      case (previous, current) =>
        val followersDifference = current.followersCount - previous.followersCount
        val friendsDifference = current.friendsCount - previous.friendsCount
        val durationInDays = new Period(previous.when, DateTime.now).getDays

        def phrasing(difference: Long) = if (difference > 0) "gained" else "lost"

        s"$userName in the past $durationInDays days have ${phrasing(followersDifference)} $followersDifference followers" +
          s" and ${phrasing(friendsDifference)} $friendsDifference friends"
    }

    def retryStoring(counts: StoredCounts, attemptNumber: Int)(implicit ec: ExecutionContext): Future[Unit] = {
      if (attemptNumber < 3) {
        statisticsRepository.storeCounts(counts).recoverWith {
          case NonFatal(_) => retryStoring(counts, attemptNumber + 1)
        }
      } else {
        Future.failed(CountStorageException(counts))
      }
    }
    def retryRetrieval(userName: String, attemptNumber: Int)(implicit ec: ExecutionContext): Future[StoredCounts] = {
      if (attemptNumber < 3) {
        statisticsRepository.retrieveLatestCounts(userName).recoverWith {
          case NonFatal(_) => retryRetrieval(userName, attemptNumber + 1)
        }
      } else {
        Future.failed(CountRetrievalException(userName))
      }
    }

    val previousCounts: Future[StoredCounts] = statisticsRepository.retrieveLatestCounts(userName)
    val currentCounts: Future[TwitterCounts] = twitterService.fetchRelationshipCounts(userName)

    val counts: Future[(StoredCounts, TwitterCounts)] = for {
      previous <- previousCounts
      current <- currentCounts
    } yield (previous, current)

    val storedCounts = counts.flatMap(storeCounts)
    val result = storedCounts.map(publishMessage).recoverWith {
      case CountStorageException(countsToStore) =>
        retryStoring(countsToStore, 0).map(_=>"")
      case CountRetrievalException(user) =>
        retryRetrieval(user, 0).map(_=>"")
    } recover {
      case CountStorageException(countsToStore) => (s"could not save $countsToStore to database")
      case CountRetrievalException(user) => (s"could not retrieve stats for $user")
      case TwitterServiceException(message) => (s"problem contacting Twitter: $message")
      case NonFatal(t) => ("unknown problem")
    }

    result
  }

}

class StatisticsServiceFailed(cause:Throwable) extends RuntimeException(cause) {
  def this(message:String) = this(new RuntimeException(message))
  def this(message:String, cause:Throwable) = this(new RuntimeException(message,cause))
}
object StatisticsServiceFailed {
  def apply(message:String):StatisticsServiceFailed = new StatisticsServiceFailed(message)
  def apply(message:String, cause:Throwable):StatisticsServiceFailed = new StatisticsServiceFailed(message,cause)
}