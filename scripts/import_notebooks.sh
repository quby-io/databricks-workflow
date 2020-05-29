#!/bin/bash
set -e

if [ $# -lt 2 ]
  then
    echo "Please specify the environment (dev/staging/production) and artifact version."
    echo "Usage: ./import_notebooks.sh test"
    exit 1
fi

ENV=$1
ARTIFACT_ID=$2

NOTEBOOK_ROOT_PATH="/pipeline/$ENV/$ARTIFACT_ID"

databricks workspace export_dir -o "$NOTEBOOK_ROOT_PATH" jobs/notebooks/
echo "Imported."