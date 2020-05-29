#!/usr/bin/env bash
set -e

if [ $# -lt 1 ]
  then
    echo "Please specify the cluster name [and job notebook path]"
    echo "Usage: ./install_library_on_cluster.sh my_cluster_name"
    exit 1
fi

declare CLUSTER_NAME=$1

declare CLUSTER_ID=$(databricks clusters list --output json | jq --raw-output ".clusters[] | select(.cluster_name == \"${CLUSTER_NAME}\") | .cluster_id")
declare CLUSTER_STATE=$(databricks clusters list --output json | jq --raw-output ".clusters[] | select(.cluster_name == \"${CLUSTER_NAME}\") | .state")

if [ -z "$CLUSTER_ID" ]
  then
    echo "Cluster ID not found."
    exit 1
fi
echo "Cluster $CLUSTER_NAME has ID: $CLUSTER_ID"

JAR_VERSION=$(cat scala/build.sbt | grep "version := " | sed -e 's/.*\(version := \)//' | tr -d "\"")
JAR_NAME="databricks-demo-assembly-$JAR_VERSION.jar"
JAR_PATH="scala/target/scala-2.11/$JAR_NAME"
JAR_DESTINATION="dbfs:/libs-dev/$CLUSTER_NAME/jars/$JAR_NAME"

echo "Current project version is $JAR_VERSION"
echo "Jar name is $JAR_NAME"

echo "Copying $JAR_PATH to $JAR_DESTINATION"
dbfs cp --overwrite $JAR_PATH $JAR_DESTINATION
echo "Copied."

if [ "$CLUSTER_STATE" ==  "TERMINATED" ]; then
    echo "Cluster is terminated"
    echo "Starting cluster"
    databricks clusters start --cluster-id $CLUSTER_ID
    echo "done"
fi

echo "Installing libraries"
databricks libraries install --cluster-id $CLUSTER_ID --jar $JAR_DESTINATION

echo "Done."
echo "Library status on cluster ${CLUSTER_NAME} (${CLUSTER_ID}): "
echo $(databricks libraries list --cluster-id $CLUSTER_ID)

if [ "$CLUSTER_STATE" ==  "RUNNING" ]; then
    echo "Cluster is running"
    echo "restarting the cluster Id : $CLUSTER_ID"
    databricks clusters restart --cluster-id $CLUSTER_ID
    echo "done"
fi

