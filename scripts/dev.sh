#!/bin/bash
set -e

if [ $# -lt 3 ]
  then
    echo "Please specify the environment (dev/staging/production), and the artifact_id as arguments."
    echo "Usage: ./dev.sh staging job_name 0.1.0-df88b42"
    exit 1
fi

ENV=$1
JOB_NAME=$2
ARTIFACT_ID=$3

DATABRICKS_PATH="/dev"

ARTIFACTS_PATH="dbfs:/artifacts/$ARTIFACT_ID/"
DOWNLOAD_PATH="/tmp/artifacts/tmp"
TEMP_PATH="/tmp/artifacts/$ARTIFACT_ID"
DBFS_PATH="dbfs:/env-artifacts/$ENV/$ARTIFACT_ID"
NOTEBOOK_ROOT_PATH="$DATABRICKS_PATH/$ENV/$JOB_NAME/$ARTIFACT_ID"

if [[ -d "$TEMP_PATH" ]]; then
    echo "$TEMP_PATH exists. Using existing artifacts."
else
    echo "$TEMP_PATH does not exist"
    echo "Getting artifacts from $ARTIFACTS_PATH"
    rm -rf $DOWNLOAD_PATH
    dbfs cp --overwrite --recursive $ARTIFACTS_PATH $DOWNLOAD_PATH
    mv $DOWNLOAD_PATH $TEMP_PATH
fi

declare VERSIONS=$(cat "$TEMP_PATH/versions.json")

JAR_NAME=$(echo "$VERSIONS" | jq -r .toon_datascience_jar_name)
JAR_PATH="$TEMP_PATH/$JAR_NAME"
JAR_DESTINATION="$DBFS_PATH/jars/$JAR_NAME"
echo "Copying $JAR_PATH to $JAR_DESTINATION"
dbfs cp --overwrite "$JAR_PATH" "$JAR_DESTINATION"


echo "Replacing jobs notebooks into Databricks"
databricks workspace mkdirs $NOTEBOOK_ROOT_PATH
databricks workspace rm -r $NOTEBOOK_ROOT_PATH
databricks workspace import_dir $TEMP_PATH/notebooks $NOTEBOOK_ROOT_PATH
echo "Imported."



# Merges the content of the JSON configurations in the following order
# 1) job_configuration/_base_.json
# 2) job_configuration/${JOB_NAME}.json
# 2) environments/${ENV}.json (.config section)
declare JOB_CONFIG_TEMPLATE=$(jq -s '.[0] * .[1] * .[2].config' $TEMP_PATH/job_configuration/_base_.json $TEMP_PATH/job_configuration/"$JOB_NAME".json $TEMP_PATH/environments/"$ENV".json)


declare JOB_CONFIG=$(echo "$JOB_CONFIG_TEMPLATE" | \
  sed -e "s:{{ENVIRONMENT}}:$ENV:g" | \
  sed -e "s:{{JOB_NAME}}:$JOB_NAME:g" | \
  sed -e "s/{{JAR_DESTINATION}}/$(echo "$JAR_DESTINATION" | sed -e 's/[\/&]/\\&/g')/g" | \
  sed -e "s/{{NOTEBOOK_ROOT_PATH}}/$(echo "$NOTEBOOK_ROOT_PATH" | sed -e 's/[\/&]/\\&/g')/g" | \
  jq .settings)

declare CLUSTER_NAME="${ENV}_${JOB_NAME}"
declare CLUSTER_CONFIG=$(echo "$JOB_CONFIG" | jq ".new_cluster + { \"cluster_name\": \"${CLUSTER_NAME}\" }")

declare CLUSTER_ID=$(databricks clusters list --output json | jq --raw-output ".clusters[] | select(.cluster_name == \"${CLUSTER_NAME}\") | .cluster_id")

if [ -z "$CLUSTER_ID" ]
  then
    echo "Cluster ID not found. Creating it"
    databricks clusters create --json "$CLUSTER_CONFIG"
fi

./scripts/install_library_on_cluster.sh $CLUSTER_NAME

echo "You can now use the notebooks on $NOTEBOOK_ROOT_PATH with cluster $CLUSTER_NAME."