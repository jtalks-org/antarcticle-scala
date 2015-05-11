import com.github.play2war.plugin._

name := "antarcticle-scala"

version := "2.7"

scalaVersion := "2.11.6"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.scala-lang.modules" %% "scala-async" % "0.9.3",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.slick" %% "slick" % "2.1.0",
//  "com.typesafe.slick" %% "slick-codegen" % "2.1.0",
//  "com.typesafe.slick" %% "slick-extensions" % "2.1.0",
  // Joda time wrapper for scala
  "com.github.nscala-time" %% "nscala-time" % "2.0.0",
  "com.h2database" % "h2" % "1.4.187",
  "org.mockito" % "mockito-all" % "1.9.5" % "test",
  // markdown support
  "org.pegdown" % "pegdown" % "1.5.0",
  // scalaz magic
  "org.scalaz" %% "scalaz-core" % "7.1.0",
  "org.typelevel" %% "scalaz-specs2" % "0.4.0" % "test",
  // production database
  "mysql" % "mysql-connector-java" % "5.1.28",
  "javax.mail" % "mail" % "1.5.0-b01",
  "com.netaporter" %% "pre-canned" % "0.0.6" % "test",
  "org.specs2" %% "specs2-core" % "3.6" % "test",
  "org.specs2" %% "specs2-mock" % "3.6" % "test",
  "org.specs2" %% "specs2-junit" % "3.6" % "test",
  "org.specs2" %% "specs2-matcher-extra" % "3.6" % "test",
  "org.specs2" %% "specs2-analysis" % "3.6" % "test"
)

// global imports for templates
TwirlKeys.templateImports ++= Seq(
  "security.Entities._",
  "security.Permissions._",
  "security.Authorities._",
  "security.Principal"
)

// publish some SBT variables as scala object for application code
buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](version)

buildInfoKeys ++= Seq[BuildInfoKey](
  BuildInfoKey.action("fullVersion") {
    sys.props.getOrElse("app.version", (version in version.scope).value + "-dev")
  }
)

buildInfoPackage := "build"

// Coffee Script compilation options
CoffeeScriptKeys.bare := true

includeFilter in (Assets, LessKeys.less) := "*.less"

scalacOptions ++= Seq("-feature")

scalacOptions in Test ++= Seq("-Yrangepos")

parallelExecution in Test := false

testOptions in Test += Tests.Argument("sequential")

// WAR packaging

Play2WarPlugin.play2WarSettings

Play2WarKeys.servletVersion := "3.0"

// disable publishing the main API jar
publishArtifact in (Compile, packageDoc) := false

// disable publishing the main sources jar
publishArtifact in (Compile, packageSrc) := false

net.virtualvoid.sbt.graph.Plugin.graphSettings

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "<empty>"

ScoverageSbtPlugin.ScoverageKeys.coverageHighlighting := true
