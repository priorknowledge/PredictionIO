
import AssemblyKeys._

assemblySettings

name := "scala-local-movielens-evaluation"

organization := "myorg"

version := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "io.prediction"    %% "core"          % "0.9.0" % "provided",
  "io.prediction"    %% "engines"          % "0.9.0" % "provided",
  "org.apache.spark" %% "spark-core"    % "1.2.0" % "provided")
