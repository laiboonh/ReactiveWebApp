package controllers

import models.Person
import play.api.mvc.{Action, AnyContent, Controller}
import scaldi.{Injectable, Injector}
import services.StatisticsService

class StatsController(implicit inj:Injector) extends Controller with Injectable {
  private val statsService = inject[StatisticsService]
  def stats(person:Person): Action[AnyContent] = Action.async {
    import scala.concurrent.ExecutionContext.Implicits.global
    statsService.createUserStatistics(person.name).map(Ok(_))
  }
}