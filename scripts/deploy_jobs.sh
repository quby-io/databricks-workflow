#!/bin/bash
set -e

for i in "$@"
do
    case $i in
        --environment=*)
        ENV="${i#*=}"
        shift
        ;;
        --build_version=*)
        ARTIFACT_ID="${i#*=}"
        shift
        ;;
        --skip-jobs)
        SKIP_JOBS=true
        shift
        ;;
        *)
        echo "Unknown option supplied"
        ;;
    esac
done

if [[ -z $ARTIFACT_ID || -z $ENV ]];
  then
    echo "Please specify the environment (dev/staging/production), and the artifact_id as arguments."
    echo "Usage: ./deploy_jobs.sh --environment=staging --build_version==123 [--skip-jobs]"
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

declare VERSIONS=$(cat "$TEMP_PATH/versions.json")

JAR_NAME=$(echo "$VERSIONS" | jq -r .toon_datascience_jar_name)
JAR_PATH="$TEMP_PATH/$JAR_NAME"
JAR_DESTINATION="$DBFS_PATH/jars/$JAR_NAME"
echo "Copying $JAR_PATH to $JAR_DESTINATION"
dbfs cp --overwrite "$JAR_PATH" "$JAR_DESTINATION"


declare -r JOB_CONFIG_PATH=$TEMP_PATH/environments/"${ENV}".json

declare JOB_SETTINGS=$(cat "$JOB_CONFIG_PATH")


echo "Replacing jobs notebooks into Databricks"
databricks workspace mkdirs $NOTEBOOK_ROOT_PATH
databricks workspace rm -r $NOTEBOOK_ROOT_PATH
databricks workspace import_dir $TEMP_PATH/notebooks $NOTEBOOK_ROOT_PATH
echo "Imported."

ACTIVE_JOBS=($(echo $JOB_SETTINGS | jq -r ".active_jobs[]" | xargs echo -n))

for JOB_NAME in "${ACTIVE_JOBS[@]}"
do
  echo "Retrieving existing $JOB_NAME job"
  declare JOB_ID=$(databricks jobs list --output json | jq ".jobs[] | select(.settings.name == \"${ENV} - ${JOB_NAME}\") | .job_id")

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

  if [ -z "$JOB_ID" ]
    then
      echo "Job not found. Creating it"
      echo "$JOB_CONFIG"
      databricks jobs create --json "$JOB_CONFIG"
    else
      # The job already exists
      echo "Updating job $JOB_ID"
      databricks jobs reset --job-id "$JOB_ID" --json "$JOB_CONFIG"

      RUNNING_JOB_ID=$(databricks runs list --active-only --limit 1000 --output json | jq -r ".runs[]? | select(.job_id == $JOB_ID and .state.life_cycle_state == \"RUNNING\")? | .run_id?")

      # Restart the job if it is running
      if [ -n "$RUNNING_JOB_ID" ]
        then
            echo "The job $RUNNING_JOB_ID (${ENV} - ${JOB_NAME}) is running. Stopping and restarting it"

            # wait until life_cycle_state == TERMINATED
            while [ $(databricks runs list --active-only --limit 1000 --output json | jq -r ".runs[] | select(.job_id == $JOB_ID and .run_id == $RUNNING_JOB_ID) | .state.life_cycle_state ") != "TERMINATED" ];do
              sleep 0.1 #100ms
            done

            echo "${JOB_NAME} has been terminated."

            # in the end... run the stopped job
            databricks jobs run-now --job-id "$JOB_ID"
        fi
  fi
done

