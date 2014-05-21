import AssemblyKeys._ // put this at the top of the file

assemblySettings

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-core" % "7.0.4",
      "org.scalaz" %% "scalaz-effect" % "7.0.4",
      "org.scalaz" %% "scalaz-typelevel" % "7.0.4"
      )
