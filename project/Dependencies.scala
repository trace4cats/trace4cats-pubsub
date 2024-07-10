import sbt._

object Dependencies {
  object Versions {
    val scala212 = "2.12.16"
    val scala213 = "2.13.14"
    val scala3 = "3.2.2"

    val trace4cats = "0.14.7"

    val fs2PubSub = "1.0.0"

    val kindProjector = "0.13.3"
    val betterMonadicFor = "0.3.1"
  }

  lazy val trace4catsFs2 = "io.janstenpickle"     %% "trace4cats-fs2"     % Versions.trace4cats
  lazy val trace4catsTestkit = "io.janstenpickle" %% "trace4cats-testkit" % Versions.trace4cats

  lazy val fs2PubSub = "com.permutive" %% "fs2-pubsub" % Versions.fs2PubSub

  lazy val kindProjector = ("org.typelevel" % "kind-projector"     % Versions.kindProjector).cross(CrossVersion.full)
  lazy val betterMonadicFor = "com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor
}
