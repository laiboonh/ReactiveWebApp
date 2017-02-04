package binders

import models.Person
import play.api.mvc.QueryStringBindable

object QueryStringBinders {
  implicit object PersonQueryStringBindable extends QueryStringBindable[Person] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Person]] =
      params.get(key) flatMap {
        value =>
          value.headOption map {
            case "alice" => Right(Person("Alice"))
            case "bob" => Right(Person("Bob"))
            case _ => Left("No such person")
          }
      }
    override def unbind(key: String, value: Person): String = value.name
  }
}

