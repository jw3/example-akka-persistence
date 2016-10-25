organization := "com.github.jw3"
name := "example-akka-persistence"
version := "0.1"
scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",

  "-feature",
  "-unchecked",
  "-deprecation",

  "-language:postfixOps",
  "-language:implicitConversions",

  "-Ywarn-unused-import",
  "-Xfatal-warnings",
  "-Xlint:_"
)

libraryDependencies ++= {
  val akkaVersion = "2.4.11"
  val scalatestVersion = "3.0.0"

  Seq(
    "com.github.dnvriend" %% "akka-persistence-jdbc" % "2.6.7",
    "mysql" % "mysql-connector-java" % "6.0.5",

    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,

    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",

    "org.scalactic" %% "scalactic" % scalatestVersion % Test,
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
  )
}

enablePlugins(JavaAppPackaging)
dockerRepository := Some(organization.value)
dockerBaseImage := "davidcaste/debian-oracle-java:jdk8"
dockerExposedPorts := Seq(8080)
