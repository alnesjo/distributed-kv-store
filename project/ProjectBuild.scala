import sbt.{Resolver, _}
import sbt.Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object ProjectBuild extends Build {

  lazy val root = Project(id = "distributed-kv-store", base = file("."), settings=assemblySettings).
    settings(
      version in ThisBuild := "1.0",
      scalaVersion in ThisBuild := "2.11.8"
      ).
    aggregate(client, common, server)
  lazy val common = Project(id = "common", base = file("common"))
  lazy val client = Project(id = "client", base = file("client")).
    dependsOn(common).
    settings(
      mainClass in Compile := Some("se.kth.id2203.Client"),
      mainClass in assembly := Some("se.kth.id2203.Client")
    )
  lazy val server = Project(id = "server", base = file("server")).
    dependsOn(common).
    settings(
      mainClass in Compile := Some("se.kth.id2203.Server"),
      mainClass in assembly := Some("se.kth.id2203.Server")
    )

}