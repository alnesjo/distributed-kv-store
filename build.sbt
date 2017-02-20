resolvers in ThisBuild ++= List(
  "Kompics Releases" at "http://kompics.sics.se/maven/repository/",
  "Kompics Snapshots" at "http://kompics.sics.se/maven/snapshotrepository/",
  "Artima Maven Repository" at "http://repo.artima.com/releases",
  "artifactory" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"
)
libraryDependencies in ThisBuild ++= List(
  "se.sics.kompics" %% "kompics-scala" % "0.9.2-SNAPSHOT",
  "se.sics.kompics.simulator" % "core" % "0.9.2-SNAPSHOT",
  "se.sics.kompics.basic" % "kompics-component-netty-network" % "0.9.2-SNAPSHOT",
  "se.sics.kompics.basic" % "kompics-component-java-timer" % "0.9.2-SNAPSHOT",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "commons-cli" % "commons-cli" % "1.3.1",
  "log4j" % "log4j" % "1.2.17",
  "org.jline" % "jline" % "3.1.2"
)