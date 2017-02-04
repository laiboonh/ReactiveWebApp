name := "PlayTest"

version := "1.0"

scalaVersion := "2.11.8"

lazy val `PlayTest` = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  filters,ws,specs2,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.12.1",
  "org.scaldi" %% "scaldi-play" % "0.5.15",
  "org.scaldi" %% "scaldi-akka" % "0.5.8"
)

routesImport += "binders.PathBinders._"

routesImport += "binders.QueryStringBinders._"

