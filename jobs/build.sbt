scalaVersion := "2.11.11"
val sparkVersion = "2.2.0"

resolvers += "MVNRepository" at "https://dl.bintray.com/spark-packages/maven/"

libraryDependencies ++= Seq("org.apache.spark" %% "spark-mllib" % sparkVersion % Provided,
                            "saurfang"         % "spark-knn"    % "0.2.0"      % Provided)
