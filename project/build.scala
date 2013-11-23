import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import com.earldouglas.xsbtwebplugin.PluginKeys._
import ScalateKeys._

object AntarcticleBuild extends Build {
  val Organization = "org.jtalks"
  val Name = "antarcticle"
  val Version = "0.1.0-SNAPSHOT"
  val ScalaVersion = "2.10.2"
  val ScalatraVersion = "2.2.1"

  lazy val project = Project (
    "antarcticle",
    file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ scalateSettings ++ addArtifact(artifact in (Compile,
      packageWar), (packageWar in Compile)) ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      resolvers += "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test",
        "com.typesafe.slick" %% "slick" % "1.0.1",
        "com.h2database" % "h2" % "1.3.166",
        "c3p0" % "c3p0" % "0.9.1.2",
        "ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
        "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106" % "container",
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq(
              Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
            ),  /* add extra bindings here */
            Some("templates")
          )
        )
      },
      // disable using the Scala version in output paths and artifacts
      crossPaths := false,
      // repositories
      publishTo <<= version { (v: String) =>
        val nexus = "http://repo.jtalks.org/content/repositories/"
        if (v.trim.endsWith("SNAPSHOT"))
          Some("JTalks Nexus Snapshots" at nexus + "snapshots")
        else
          Some("JTalks Nexus Releases"  at nexus + "releases")
      },
      // file with repository credentialfile with repository credentialss
      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
      // disable .jar publishing
      publishArtifact in (Compile, packageBin) := false,
      // disable publishing the main API jar
      publishArtifact in (Compile, packageDoc) := false,
      // disable publishing the main sources jar
      publishArtifact in (Compile, packageSrc) := false,
      // create an Artifact for publishing the .war file
      artifact in (Compile, packageWar) ~= { (art: Artifact) =>
        art.copy(`type` = "war", extension = "war")
      }
    )
  )
}
