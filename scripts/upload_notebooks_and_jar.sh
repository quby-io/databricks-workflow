#!/bin/bash
set -e
. scripts/download_artifacts.sh

for i in "$@"
do
    case $i in
        --environment=*)
        ENV="${i#*=}"
        shift
        ;;
        --artifact_id=*)
        ARTIFACT_ID="${i#*=}"
        shift
        ;;
        *)
        echo "Unknown option supplied"
        ;;
    esac
done

download_artifacts

declare VERSIONS=$(cat "$TEMP_PATH/versions.json")

JAR_NAME=$(echo "$VERSIONS" | jq -r .toon_datascience_jar_name)
JAR_PATH="$TEMP_PATH/$JAR_NAME"
JAR_DESTINATION="$DBFS_PATH/jars/$JAR_NAME"
echo "Copying $JAR_PATH to $JAR_DESTINATION"
dbfs cp --overwrite "$JAR_PATH" "$JAR_DESTINATION"

declare -r JOB_CONFIG_PATH=$TEMP_PATH/environments/"${ENV}".json

declare JOB_SETTINGS=$(cat "$JOB_CONFIG_PATH")

echo "Copying jobs notebooks to Databricks"
databricks workspace mkdirs $NOTEBOOK_ROOT_PATH
databricks workspace rm -r $NOTEBOOK_ROOT_PATH
databricks workspace import_dir $TEMP_PATH/notebooks $NOTEBOOK_ROOT_PATH
echo "Notebooks copied."