lazy val commonSettings = Seq(
  organization := "com.acervera.osmfacts",
  organizationHomepage := Some(url("http://www.acervera.com")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  version := "0.1-SNAPSHOT",

  publishArtifact := false, // Enable publish
  publishMavenStyle := true,
  publishArtifact in Test := false, // No publish test stuff
  pomExtra :=
    <url>https://github.com/angelcervera/osm4scala</url>
      <scm>
        <connection>scm:git:git://github.com/angelcervera/osm4scala.git</connection>
        <developerConnection>scm:git:ssh://git@github.com/angelcervera/osm4scala.git</developerConnection>
        <url>https://github.com/angelcervera/osm4scala</url>
      </scm>
      <developers>
        <developer>
          <id>angelcervera</id>
          <name>Angel Cervera Claudio</name>
          <email>angelcervera@silyan.com</email>
        </developer>
      </developers>
  ,

  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
  )

)

lazy val fact1 = Project(id = "fact1", base = file("fact1")).
  settings(
    commonSettings,
    name := "fact1",
    description := "Check blobs overlaps",
    scalaVersion := "2.11.11",
    mainClass in assembly := Some("com.acervera.osmfacts.fact1.Fact1Driver"),
    test in assembly := {},
    libraryDependencies ++= Seq(
      "com.acervera.osm4scala" %% "osm4scala-core" % "1.0.1",
      "com.iheart" %% "ficus" % "1.4.2",
      "com.github.pathikrit" %% "better-files" % "2.17.1",
      "org.apache.spark" %% "spark-core" % "2.2.0" % "provided"
    )
  )

lazy val fact2 = Project(id = "fact2", base = file("fact2")).
  settings(
    commonSettings,
    name := "fact2",
    description := "Unique entities",
    scalaVersion := "2.11.11",
    mainClass in assembly := Some("com.acervera.osmfacts.fact2.Fact2Driver"),
    test in assembly := {},
    libraryDependencies ++= Seq(
      "com.acervera.osm4scala" %% "osm4scala-core" % "1.0.1",
      "org.apache.spark" %% "spark-core" % "2.2.0" % "provided"
    )
  )

lazy val fact3 = Project(id = "fact3", base = file("fact3")).
  settings(
    commonSettings,
    name := "fact3",
    description := "Connections always in the extrem of the way",
    scalaVersion := "2.11.11",
    mainClass in assembly := Some("com.acervera.osmfacts.fact2.Fact2Driver"),
    test in assembly := {},
    libraryDependencies ++= Seq(
      "com.acervera.osm4scala" %% "osm4scala-core" % "1.0.1",
      "org.apache.spark" %% "spark-core" % "2.2.0" % "provided"
    )
  )