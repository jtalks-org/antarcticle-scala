logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.2")

addSbtPlugin("com.github.play2war" % "play2-war-plugin" % "1.2-beta4")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.2")
