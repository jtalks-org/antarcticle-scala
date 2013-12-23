import play.Project._
import com.github.play2war.plugin._

name := "antarcticle-scala"

version := "0.1"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "1.0.1",
  // Joda time wrapper for scala
  "com.github.nscala-time" %% "nscala-time" % "0.6.0",
  "com.h2database" % "h2" % "1.3.174",
  "org.mockito" % "mockito-all" % "1.9.0",
  "postgresql" % "postgresql" % "9.1-901.jdbc4"
)

playScalaSettings

Play2WarPlugin.play2WarSettings

Play2WarKeys.servletVersion := "3.0"
