name := """QSenti"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "commons-lang" % "commons-lang" % "2.3",
	"org.apache.hadoop" % "hadoop-client" % "2.6.0",
	"org.apache.hadoop" % "hadoop-hdfs" % "2.6.0",
	"org.apache.hadoop" % "hadoop-common" % "2.6.0",																																																																												
  "org.apache.hbase" % "hbase" % "1.0.0",
  "org.apache.hbase" % "hbase-client" % "1.0.0",
  "org.apache.hbase" % "hbase-common" % "1.0.0",
  "org.apache.hbase" % "hbase-server" % "1.0.0",
  "org.quartz-scheduler" % "quartz" % "2.2.1",
  "com.google.code.gson" % "gson" % "2.2",
  "io.github.cloudify" % "spdf_2.11" % "1.3.1",
  "com.itextpdf" % "itextpdf" % "5.0.6",
  "com.itextpdf.tool" % "xmlworker" % "5.5.6",
  "javax.mail" % "mail" % "1.4.7",
	"com.typesafe.play" %% "play-mailer" % "2.4.1"
)