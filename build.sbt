name := "pdfSelector"

version := "0.1"

scalaVersion := "2.13.1"

import Dependencies._

ThisBuild / organization := "org.gnuger"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.1"

lazy val root = (project in file("."))
  .settings(
    name := "pdf-selector",
    libraryDependencies ++= rootDependencies
  )