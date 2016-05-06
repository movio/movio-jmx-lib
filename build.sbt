organization := "movio.cinema"
name := "mc-jmx-lib"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % Test,
  "org.mockito" % "mockito-core" % "1.9.5" % Test
)

releaseVersionBump := sbtrelease.Version.Bump.Minor
releaseTagName := s"${version.value}"
