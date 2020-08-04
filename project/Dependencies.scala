import sbt._

object Dependencies {
  val akkaHttpVersion = "10.1.12"
  val akkaVersion = "2.6.8"

  val akkaStack = Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion
    //    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    //    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
  )

  val pdfBox = "org.apache.pdfbox" % "pdfbox" % "2.0.20"
  val pdfStack = Seq(pdfBox)

  val rootDependencies = akkaStack ++ pdfStack
}
