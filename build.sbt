lazy val commonSettings = Seq(
  organization := "com.acervera.osmfacts",
  organizationHomepage := Some(url("http://www.acervera.com")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  version := "0.1-SNAPSHOT",

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
      </developers>
  ,

  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
  )

)

lazy val fact = Project(id = "facts", base = file("facts")).
  settings(
    commonSettings,
    name := "facts",
    description := "Connections always in the extrem of the way",
    scalaVersion := "2.11.11",
    mainClass in assembly := Some("com.acervera.osmfacts.fact2.Fact2Driver"),
    test in assembly := {},
    libraryDependencies ++= Seq(
      "com.acervera.osm4scala" %% "osm4scala-core" % "1.0.1",
      "com.iheart" %% "ficus" % "1.4.2",
      "com.github.pathikrit" %% "better-files" % "2.17.1",
      "org.apache.spark" %% "spark-core" % "2.2.0" % "provided"
    )
  )