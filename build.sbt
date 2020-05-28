scalaVersion := "2.11.11"

val sparkVersion = "2.4.3"

lazy val root = Project("databricks-demo", file(".")).aggregate(core)

publish := {}
publishLocal := {}
publishArtifact := false
publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))

lazy val core = project in file("scala")

lazy val jobs = Project("databricks-demo-jobs", file("jobs"))
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core"      % sparkVersion % Provided,
    "org.apache.spark" %% "spark-sql"       % sparkVersion % Provided,
    "org.apache.spark" %% "spark-hive"      % sparkVersion % Provided,
    "com.databricks"   % "dbutils-api_2.11" % "0.0.3"      % Provided
  ))
