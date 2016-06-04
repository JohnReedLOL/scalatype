name := """scalatype"""

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.0.1" % "test"

libraryDependencies += "org.specs2" %% "specs2-scalacheck" % "3.0.1" % "test"

libraryDependencies += "org.scalacheck" % "scalacheck_2.11" % "1.12.2"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

// scalacOptions ++= Seq("-Xprint:parser")

resolvers += "johnreed2 bintray" at "http://dl.bintray.com/content/johnreed2/maven"

libraryDependencies += "scala.trace" %% "scala-trace-debug" % "2.2.14"