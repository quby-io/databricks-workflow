name := "databricks-demo"
organization := "com.quby"
scalaVersion := "2.11.11"

retrieveManaged := true
val sparkVersion = "2.4.3"
version := "0.1.0"

libraryDependencies ++= Seq(
  "org.apache.spark"     %% "spark-core"                  % sparkVersion % Provided,
  "org.apache.spark"     %% "spark-sql"                   % sparkVersion % Provided,
  "org.apache.spark"     %% "spark-hive"                  % sparkVersion % Provided,
  "org.apache.spark"     %% "spark-mllib"                 % sparkVersion % Provided,
  "org.scalatest"        %% "scalatest"                   % "2.2.6"      % "test",
  "commons-io"           % "commons-io"                   % "2.5"        % "test",
  "org.scalamock"        %% "scalamock-scalatest-support" % "3.6.0"      % "test",
  "com.databricks"       % "dbutils-api_2.11"             % "0.0.3"      % Provided
)

// If running locally, ensure that "provided" dependencies are on the classpath.
run in Compile := Defaults
  .runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))
  .evaluated

// Parallel execution speeds up unit tests
parallelExecution in Test := true

assemblyOption in assembly := (assemblyOption in assembly).value
  .copy(includeScala = false)
test in assembly := {}

assemblyMergeStrategy in assembly := {
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

// Measure time for each test
testOptions in Test += Tests.Argument("-oD")
