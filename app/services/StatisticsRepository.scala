package services

import models.StoredCounts
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import scaldi.{Injectable, Injector}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import play.modules.reactivemongo.json._

trait StatisticsRepository {
  def storeCounts(counts: StoredCounts)(implicit ec: ExecutionContext): Future[Unit]

  def retrieveLatestCounts(userName: String)(implicit ec: ExecutionContext): Future[StoredCounts]
}

class MongoStatisticsRepository(implicit inj: Injector) extends StatisticsRepository with Injectable {

  private val reactiveMongoApi = inject[ReactiveMongoApi]

  import scala.concurrent.ExecutionContext.Implicits._

  private lazy val collection: Future[JSONCollection] = reactiveMongoApi.database.
    map(_.collection[JSONCollection]("userStatistics"))

  override def storeCounts(counts: StoredCounts)(implicit ec: ExecutionContext): Future[Unit] =
    collection.map {
      _.insert(counts).recover {
        case NonFatal(_) => CountStorageException(counts)
      }
    }

  override def retrieveLatestCounts(userName: String)(implicit ec: ExecutionContext): Future[StoredCounts] =
    collection.flatMap {
      _.find(Json.obj("userName" -> userName)).sort(Json.obj("_id" -> -1)).one[StoredCounts].map {
        _.getOrElse(StoredCounts(DateTime.now, userName, 0, 0))
      }
    }.recover {
      case NonFatal(_) => throw CountRetrievalException(userName)
    }
}


case class CountRetrievalException(userName: String) extends RuntimeException(s"Could not read counts for $userName")

case class CountStorageException(counts: StoredCounts) extends RuntimeException