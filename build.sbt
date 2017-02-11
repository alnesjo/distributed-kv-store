name := "distributed-kv-store"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Kompics Releases" at "http://kompics.sics.se/maven/repository/"
resolvers += "Kompics Snapshots" at "http://kompics.sics.se/maven/snapshotrepository/"
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

libraryDependencies += "se.sics.kompics" %% "kompics-scala" % "0.9.2-SNAPSHOT"
libraryDependencies += "se.sics.kompics.basic" % "kompics-component-netty-network" % "0.9.2-SNAPSHOT"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
