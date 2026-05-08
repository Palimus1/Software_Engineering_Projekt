val scala3Version = "3.8.2"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Projekt",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.20",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % "test",
    Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,

    sonarProperties := Map(
      "sonar.projectKey" -> "Software_Engineering_Projekt",
      "sonar.host.url" -> "http://localhost:9000",
      "sonar.token" -> "sqp_5e7fe4b548a99d932b47e78dc9f0d1c5d8732107",
      "sonar.scala.scoverage.reportPath" -> "target/scala-3.8.2/scoverage-report/scoverage.xml"
    )
  )