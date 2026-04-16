val scala3Version = "3.8.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Projekt",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.20",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % "test",
    Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat
  )
