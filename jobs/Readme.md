# Jobs

This folder contains the definition of Databricks jobs and the respective configuration for each environment.

## Jobs configuration

Each job configuration is composed by the merge of the JSON configurations in the following order
  1) job_configuration/_base_.json
  2) job_configuration/${JOB_NAME}.json
  2) environments/${ENV}.json (`.config` section)
  
The `job_configuration` folder contains a `*.json` file for each job definition.
That file defines a job with Databricks syntax [https://docs.databricks.com/dev-tools/api/latest/jobs.html]. It also contains some special placeholders `{{*}}` that are going to be filled in by the deployment job.

## Notebooks

The `notebooks` folder contains the notebooks that are going to be triggered by the jobs. By convention, the notebook name must match the name of it's configuration file from `job_configuration` folder.

## Environment

There is a JSON file for each environment. 
The file specifies what are the active jobs in that environment, and some overrides for the job configurations.