name := """scalatype"""

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.0.1" % "test"

libraryDependencies += "org.specs2" %% "specs2-scalacheck" % "3.0.1" % "test"

libraryDependencies += "org.scalacheck" % "scalacheck_2.11" % "1.12.2"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

// scalacOptions ++= Seq("-Xprint:parser")

resolvers += "johnreed2 bintray" at "http://dl.bintray.com/content/johnreed2/maven"

libraryDependencies += "scala.trace" %% "scala-trace-debug" % "2.2.14"

scalacOptions ++= Seq("-Xfatal-warnings", "-unchecked", "-feature", "-Xlint", "-Yinline-warnings", "-Ywarn-inaccessible", "-Ywarn-nullary-override", "-Ywarn-nullary-unit")

libraryDependencies ++= Seq(
  "eu.timepit" %% "refined"            % "0.4.0",
  "eu.timepit" %% "refined-scalaz"     % "0.4.0",         // optional
  "eu.timepit" %% "refined-scodec"     % "0.4.0",         // optional
  "eu.timepit" %% "refined-scalacheck" % "0.4.0" % "test" // optional
)