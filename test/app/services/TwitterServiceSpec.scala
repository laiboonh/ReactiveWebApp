package app.services

import modules.TestModule
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.{JsNumber, JsObject}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import scaldi.Injectable
import scaldi.play.ScaldiApplicationBuilder
import services.{DefaultTwitterService, TwitterCounts}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class TwitterServiceSpec extends Specification with Injectable with Mockito {
  "fetchRelationshipCounts" should {
    "return TwitterCount" in {

      implicit val injector =
        new ScaldiApplicationBuilder().prependModule(new TestModule).buildInj()

      val mockWSClient = inject[WSClient]

      val mockWSResponse = mock[WSResponse]
      val mockWSRequest = mock[WSRequest]
      mockWSClient.url(any) returns mockWSRequest
      mockWSRequest.sign(any) returns mockWSRequest
      mockWSRequest.withQueryString(any) returns mockWSRequest
      mockWSRequest.get() returns Future.successful(mockWSResponse)
      mockWSResponse.status returns 200
      mockWSResponse.json returns JsObject(Seq("followers_count" -> JsNumber(1), "friends_count" -> JsNumber(2)))

      implicit ec: ExecutionContext =>
        val result = new DefaultTwitterService().fetchRelationshipCounts("foo")
        Await.result(result, Duration.Inf) must_== TwitterCounts(1, 2)

    }
  }
}
