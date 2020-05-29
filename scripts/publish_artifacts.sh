#!/bin/bash
set -e

if [ -z "$1" ]
  then
    echo "Artifact ID (Jenkins BUILD_NUMBER) not found."
    exit 1
fi

ARTIFACT_ID=$1
TEMP_PATH="/tmp/artifacts/$ARTIFACT_ID"
DESTINATION_PATH="dbfs:/artifacts/toon-datasience/$ARTIFACT_ID"

if [[ -d "$TEMP_PATH" ]]; then
    echo "$TEMP_PATH already exists. Removing it's content"
    rm -r $TEMP_PATH/*
else
    mkdir -p $TEMP_PATH
fi


echo "Copying artifacts to $TEMP_PATH"

JAR_VERSION=$(cat scala/build.sbt | grep "version := " | sed -e 's/.*\(version := \)//' | tr -d "\"")
JAR_NAME="databricks-demo-assembly-$JAR_VERSION.jar"
JAR_PATH="scala/target/scala-2.11/$JAR_NAME"


cat > jobs/versions.json << EOF
{
    "toon_datascience_jar_name": "$JAR_NAME"
}
EOF

echo "Copying $JAR_PATH to $TEMP_PATH"
cp -f $JAR_PATH $TEMP_PATH/$JAR_NAME

echo "Copying jobs and notebooks into $TEMP_PATH"
cp -f -R jobs/notebooks $TEMP_PATH/notebooks
cp -f -R jobs/job_configuration $TEMP_PATH/job_configuration
cp -f -R jobs/environments $TEMP_PATH/environments
cp -f -R jobs/versions.json $TEMP_PATH/versions.json
echo "Done"


echo "Publishing artifacts to $DESTINATION_PATH"
dbfs cp --overwrite --recursive $TEMP_PATH $DESTINATION_PATH