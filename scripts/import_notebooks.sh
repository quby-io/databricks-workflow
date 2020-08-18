#!/bin/bash
set -e

if [ $# -lt 1 ]
  then
    echo "Please specify the notebook path."
    echo "Usage: ./import_notebooks.sh /pipeline/staging/0.0.1-df88b42"
    exit 1
fi

NOTEBOOK_PATH=$1

databricks workspace export_dir -o "$NOTEBOOK_PATH" jobs/notebooks/
echo "Imported."