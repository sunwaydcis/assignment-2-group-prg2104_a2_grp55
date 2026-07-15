ThisBuild / scalaVersion := "3.8.4"
ThisBuild / organization := "edu.sunway.prg2104"
ThisBuild / version := "1.12.12"

lazy val root = (project in file("."))
  .settings(
    name := "amazon-sales-analytics",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Wunused:all"
    ),
    libraryDependencies ++= Seq(
      "com.github.tototoshi" %% "scala-csv" % "2.0.0",
      "org.scalameta" %% "munit" % "1.0.2" % Test
    )
  )
