lazy val commonSettings = Seq(
  Compile / compile / javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  libraryDependencies ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Seq(compilerPlugin(Dependencies.kindProjector), compilerPlugin(Dependencies.betterMonadicFor))
      case _ => Seq.empty
    }
  },
  scalacOptions += {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => "-Wconf:any:wv"
      case _ => "-Wconf:any:v"
    }
  },
  Test / fork := true,
  resolvers ++= Resolver.sonatypeOssRepos("releases"),
)

lazy val noPublishSettings =
  commonSettings ++ Seq(publish := {}, publishArtifact := false, publishTo := None, publish / skip := true)

lazy val publishSettings = commonSettings ++ Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { _ =>
    false
  },
  Test / publishArtifact := false
)

lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .settings(name := "Trace4Cats PubSub")
  .aggregate(`pubsub-client`)

lazy val `pubsub-client` =
  (project in file("modules/pubsub-client"))
    .settings(publishSettings)
    .settings(
      name := "trace4cats-pubsub-client",
      libraryDependencies ++= Seq(Dependencies.trace4catsFs2, Dependencies.fs2PubSub),
      libraryDependencies ++= Seq(Dependencies.trace4catsTestkit).map(_ % Test)
    )
