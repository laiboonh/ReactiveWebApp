package binders

import models.Person
import play.api.mvc.PathBindable

object PathBinders {
  implicit object PersonPathBindable extends PathBindable[Person] {
    override def bind(key: String, value: String): Either[String, Person] =
      value match {
        case "alice" => Right(Person("Alice"))
        case "bob" => Right(Person("Bob"))
        case _ => Left("No such person")
      }
    override def unbind(key: String, value: Person): String = value.name
  }
}