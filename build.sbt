name := """idgen"""

organization := "com.lvxingpai"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.4"

//crossScalaVersions := "2.10.4" :: "2.11.4" :: Nil

// Change this to another test framework if you prefer
libraryDependencies ++= Seq(
  "com.lvxingpai" %% "etcd-store-guice" % "0.1.0-SNAPSHOT",
  "com.twitter" %% "finagle-thriftmux" % "6.30.0",
  "com.twitter" %% "scrooge-core" % "4.2.0",
  "org.mongodb" % "mongo-java-driver" % "3.1.1",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

com.twitter.scrooge.ScroogeSBT.newSettings

enablePlugins(JavaAppPackaging)

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

