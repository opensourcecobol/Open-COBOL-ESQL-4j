name := "OCESQL4j"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "org.postgresql" % "postgresql" % "42.2.5"
)

assemblyJarName := "ocesql4j.jar"

scalacOptions := Seq("-unchecked", "-deprecation")

ThisBuild / assemblyCacheUnzip := false
