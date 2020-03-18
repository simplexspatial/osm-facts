import sbt.Keys.{licenses, name, publishArtifact}

lazy val commonSettings = Seq(
  organization := "com.acervera.osmfacts",
  name := "facts",
  description := "Connections always in the extrem of the way",
  version := "0.1-SNAPSHOT",
  organizationHomepage := Some(url("http://www.acervera.com")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  publishArtifact := false, // Enable publish
  publishMavenStyle := true,
  publishArtifact in Test := false, // No publish test stuff
  pomExtra :=
    <url>https://github.com/angelcervera/osm-facts</url>
    <scm>
      <connection>scm:git:git://github.com/angelcervera/osm-facts.git</connection>
      <developerConnection>scm:git:ssh://git@github.com/angelcervera/osm-facts.git</developerConnection>
      <url>https://github.com/angelcervera/osm-facts</url>
    </scm>
    <developers>
      <developer>
        <id>angelcervera</id>
        <name>Angel Cervera Claudio</name>
        <email>angelcervera@silyan.com</email>
      </developer>
    </developers>,
  resolvers += "osm4scala repo" at "http://dl.bintray.com/angelcervera/maven",
  test in assembly := {}
)

lazy val sparkFact = Project(id = "spark-facts", base = file("spark-facts"))
  .settings(commonSettings)
  .settings(
    name := "spark-facts",
    scalaVersion := "2.11.12",
    libraryDependencies ++= Seq(
      "com.acervera.osm4scala" %% "osm4scala-core" % "1.0.1",
      "com.iheart" %% "ficus" % "1.4.2",
      "com.github.pathikrit" %% "better-files" % "2.17.1",
      "org.apache.spark" %% "spark-core" % "2.4.5" % "provided",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
    )
  )

lazy val scalaFact = Project(id = "scala-facts", base = file("scala-facts"))
  .settings(commonSettings)
  .settings(
    name := "scala-facts",
    scalaVersion := "2.12.11",
    libraryDependencies ++= Seq(
      "com.acervera.osm4scala" %% "osm4scala-core" % "1.0.1",
      "com.github.pathikrit" %% "better-files" % "2.17.1",
      "org.roaringbitmap" % "RoaringBitmap" % "0.8.13",
      "io.tmos" %% "arm4s" % "1.1.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
    )
  )
