name := "Idus"

version := "0.1"

scalaVersion := "2.13.14"

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.1"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.5.1"
libraryDependencies += "org.apache.spark" %% "spark-hive" % "3.5.1"
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "3.3.1"
libraryDependencies += "com.amazonaws" % "aws-java-sdk-bundle" % "1.11.199"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

mainClass in assembly := Some("Main")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
  case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
  case PathList("META-INF", "org", "apache", "logging", "log4j", "core", "config", "plugins", "Log4j2Plugins.dat") => MergeStrategy.concat
  case PathList("META-INF", "versions", "11", "module-info.class") => MergeStrategy.first
  case PathList("META-INF", "versions", "9", "module-info.class") => MergeStrategy.first
  case PathList("google", "protobuf", xs @ _*) => MergeStrategy.first
  case "reference.conf" => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

parallelExecution in Global := true
concurrentRestrictions in Global := Seq(
  Tags.limitAll(8)
)
