#!/bin/bash
set -e

if [ $# -lt 1 ]
  then
    echo "Please specify the environment (integration_test/staging/production)"
    echo "Usage: ./run_jobs.sh integration_test"
    exit 1
fi

ENV=$1

declare -r JOB_CONFIG_PATH=jobs/environments/"${ENV}".json

declare JOB_SETTINGS=$(cat "$JOB_CONFIG_PATH")

ACTIVE_JOBS=($(echo $JOB_SETTINGS | jq -r ".active_jobs[]" | xargs echo -n))

for JOB_NAME in "${ACTIVE_JOBS[@]}"
do
  echo "Retrieving existing ${JOB_NAME} job"
  declare JOB_ID=$(databricks jobs list --output json | jq ".jobs[] | select(.settings.name == \"${ENV} - ${JOB_NAME}\") | .job_id")

  if [ -z "$JOB_ID" ]
  then
      echo "Job not found. Integration test failed"
      exit 1
  else
      echo "Executing job ${ENV} - ${JOB_NAME} ($JOB_ID)"
      databricks jobs run-now --job-id "$JOB_ID"

      sleep 1
      # wait until life_cycle_state == TERMINATED
      while [ $(databricks runs list --job-id "$JOB_ID" --limit 1 --output json | jq -r ".runs[].state.life_cycle_state") != "TERMINATED" ];do

         echo "Job ${ENV} - ${JOB_NAME} ($JOB_ID) is running."
         sleep 3 #3s

      done

      echo "Job ${ENV} - ${JOB_NAME} ($JOB_ID) has terminated."

      # Check it the result state is SUCCESS
      if [ $(databricks runs list --job-id "$JOB_ID" --limit 1 --output json | jq -r ".runs[].state.result_state") == "SUCCESS" ]
      then
        echo "Success."
      else
        echo "The job ${ENV} - ${JOB_NAME} ($JOB_ID) did not run successfully"
        exit 1
      fi
  fi
done

