resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.2")

logLevel := Level.Warn