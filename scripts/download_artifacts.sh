#!/bin/bash

download_artifacts() {
    if [[ -z $ARTIFACT_ID || -z $ENV ]];
      then
        echo "Please specify the environment (dev/staging/production), and the artifact_id as arguments."
        echo "Usage: ./deploy_jobs.sh --environment=staging --build_version==123"
        exit 1
    fi

    ARTIFACTS_PATH="dbfs:/artifacts/$ARTIFACT_ID/"
    DOWNLOAD_PATH="/tmp/artifacts/tmp"
    TEMP_PATH="/tmp/artifacts/$ARTIFACT_ID"
    DBFS_PATH="dbfs:/env-artifacts/$ENV/$ARTIFACT_ID"
    NOTEBOOK_ROOT_PATH="/pipeline/$ENV/$ARTIFACT_ID"

    if [[ -d "$TEMP_PATH" ]]; then
        echo "$TEMP_PATH exists. Using existing artifacts."
    else
        echo "$TEMP_PATH does not exist"
        echo "Getting artifacts from $ARTIFACTS_PATH"
        rm -rf $DOWNLOAD_PATH
        dbfs cp --overwrite --recursive $ARTIFACTS_PATH $DOWNLOAD_PATH
        mv $DOWNLOAD_PATH $TEMP_PATH
    fi
}
