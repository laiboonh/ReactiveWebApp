package services

class GreetingService {
  println("initializing greeting service")
  def greet(name: String): String = s"Hello $name"
}
