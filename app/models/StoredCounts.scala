package models

import org.joda.time.DateTime

case class StoredCounts(when: DateTime, userName: String, followersCount: Long, friendsCount: Long)

object StoredCounts {

  import play.api.libs.json._
  implicit val countsFormat = Json.format[StoredCounts]

}
