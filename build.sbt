  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux") => "linux"
    case n if n.startsWith("Mac") => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ => throw new Exception("Unknown platform!")
  }

  val scala3Version = "3.8.2"

  lazy val root = project
    .in(file("."))
    .enablePlugins(SonarPlugin)
    .settings(
      name := "Projekt",
      version := "0.1.0-SNAPSHOT",

      scalaVersion := scala3Version,
      strykerIsSupported := true,
      libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.20",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.20" % "test",
      
      scalacOptions ++= Seq(
        "-deprecation",
        "-Wconf:msg=Implicit parameters should be provided with a `using` clause:s"
      ),

      libraryDependencies += "org.scalafx" %% "scalafx" % "20.0.0-R31",
      libraryDependencies ++= Seq("base", "controls", "fxml", "graphics", "media", "swing", "web").map(m =>
        "org.openjfx" % s"javafx-$m" % "20.0.2" classifier osName
      ),
      coverageExcludedFiles := ".*Gui.*",

      Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat,

      sonarProperties ++= Map(
        "sonar.projectKey" -> "Software_Engineering_Projekt",
        "sonar.host.url" -> "http://localhost:9000",
        "sonar.token" -> "squ_cbb0a4be896f58d30f0a75e7af65d62343728d45",
        "sonar.scala.scoverage.reportPath" -> "target/scala-3.8.2/scoverage-report/scoverage.xml",
        "sonar.sources" -> "src/main/scala",
        "sonar.tests" -> "src/test/scala"
      )
    )