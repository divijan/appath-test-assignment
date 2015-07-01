scalaVersion := "2.11.7"

libraryDependencies ++= {
  val akkaVersion = "2.3.11"
  val akkaExperimentalVersion = "1.0-RC3"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    //"com.typesafe.akka" %% "akka-agent" % akkaVersion,
    //"com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    //"com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    //"com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,
    //"com.github.dnvriend" % "akka-persistence-inmemory_2.11" % "1.0.0" % "test",
    "com.typesafe.akka" %% "akka-http-experimental" % akkaExperimentalVersion,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaExperimentalVersion,
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaExperimentalVersion
    //"com.typesafe.akka" %% "akka-http-testkit-experimental" % akkaExperimentalVersion % "test",
    //"com.typesafe.akka" %% "akka-http-xml-experimental" % akkaExperimentalVersion,
    //"org.scala-lang.modules" %% "scala-xml" % "1.0.2",
    //"org.specs2" %% "specs2" % "2.4.15" % "test" ,
    //"com.optrak" %% "scalautil" % "latest.integration",
    //"com.optrak" %% "akkatestutil" % "latest.integration" % "test"
  )
}
