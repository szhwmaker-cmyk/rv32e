name := "rv32e-soc"
version := "1.0"
scalaVersion := "2.13.10"

scalacOptions ++= Seq(
  "-language:reflectiveCalls",
  "-deprecation",
  "-feature",
  "-Xcheckinit"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.6.0"
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.6.0" % "test"
