package controllers

import actors.StatisticsProvider
import akka.actor.{ActorRef, ActorSystem}
import play.api.mvc.{Action, AnyContent, Controller}
import scaldi.Injector
import scaldi.akka.AkkaInjectable

class TestController(implicit inj:Injector) extends Controller with AkkaInjectable {

  implicit val system = inject[ActorSystem]

  private val statisticsProviderProps = injectActorProps [StatisticsProvider]
  private val statisticsProvider = system.actorOf(statisticsProviderProps, "statisticsProvider")

  def test(msg:String): Action[AnyContent] = Action {
    request =>
      statisticsProvider ! msg
      Ok("check log")
  }
}