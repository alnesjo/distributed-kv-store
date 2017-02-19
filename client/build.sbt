name := "client"

version := "1.0"

scalaVersion := "2.11.8"

mainClass in Compile := Some("se.kth.id2203.Client")

resolvers += "Kompics Releases" at "http://kompics.sics.se/maven/repository/"
resolvers += "Kompics Snapshots" at "http://kompics.sics.se/maven/snapshotrepository/"
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

libraryDependencies += "se.sics.kompics" %% "kompics-scala" % "0.9.2-SNAPSHOT"
libraryDependencies += "se.sics.kompics.simulator" % "core" % "0.9.2-SNAPSHOT"
libraryDependencies += "se.sics.kompics.basic" % "kompics-component-netty-network" % "0.9.2-SNAPSHOT"
libraryDependencies += "se.sics.kompics.basic" % "kompics-component-java-timer" % "0.9.2-SNAPSHOT"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
libraryDependencies += "commons-cli" % "commons-cli" % "1.3.1"
libraryDependencies += "log4j" % "log4j" % "1.2.17"
libraryDependencies += "org.jline" % "jline" % "3.1.2"
