import sbt.Keys._
import sbt._

object BuildDistributedKeyValueStore extends Build {
  lazy val root = Project(id = "distributed-kv-store", base = file(".")) aggregate (client, common, server)
  lazy val client = Project(id = "client", base = file("client")) dependsOn common
  lazy val common = Project(id = "common", base = file("common"))
  lazy val server = Project(id = "server", base = file("server")) dependsOn common
}