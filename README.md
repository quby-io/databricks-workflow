# Databricks Workflow (Alpha)

This repository is an *example* of how to use Databricks for setting up a multi-environment data processing pipeline. 

If you are part of a Data Engineering or Data Science team, and you want to start a project in Databricks, you can use this repository as a jump start. 

This template shows some of the best practices and processes we have set up for production, research and development at [Quby](https://www.quby.com/).

This code can help you to get an answer to the questions you might have when starting from scratch, like:
* How do I set up a production pipeline in Databricks?
* How do I run unit tests on Spark transformations?
* How do I run integration tests on Notebook work flows?
* How do I rapidly prototype data transformations on real data?
* How do I refactor notebook experiments into tested code?
* How do I organize the code?
* How do I manage multiple environments and configurations?

This repository is also an open invitation to all developers out there that see improvements on our current practices. Don't hesitate to contribute and make a pull request with your suggestions!

## Getting started

In order to get started with a paid or trial version of Databricks, follow the steps below. If you want to get started
with the community edition of follow [these steps](./doc/databricks_community_edition_instructions.md).

1. Setup your [Databricks account](https://databricks.com/try-databricks) (This requires an AWS or Azure account)
2. Create an [authentication token](https://docs.databricks.com/dev-tools/api/latest/authentication.html) on your Databricks account
2. Install and configure your [Databricks CLI](https://docs.databricks.com/dev-tools/cli/index.html) (This example repository uses your default Databricks profile) 
   1. `pip install databricks-cli`
   2. `databricks configure --token`
3. Install [jq](https://stedolan.github.io/jq/), we use it to parse and combine JSON configuration files for the jobs.
   1. `brew install jq`
4. Clone this repository on your local machine 
   1. `git clone git@github.com:quby-io/databricks-workflow.git`

You are good to go :)

Try to execute `make help`, this will show the actions available.

## Project structure

The project is structured in 3 folders:
* `jobs`: Contains job definition and configuration of the scheduled Databricks jobs, along with the notebooks that execute them. More details can be found in the jobs [Readme.md](./jobs/Readme.md) 
* `scala`: Contains the Scala code and relative unit tests. The content of this directory gets compiled, packaged and deployed to each environment.
* `scripts`: Contains bash scripts that are used for managing the environment deployments and development workflow. These scripts are triggered through the `make` targets.

## Development workflow

When creating a new feature from real data you can procede like this:

1. Create a new environment
   1. Duplicate `/jobs/environments/staging.json` and rename it to reflect the feature intent `/jobs/environments/dev_my_feature.json`
   2. Adjust the configuration of the new environment to fit your needs (eg. change the `featureDb` parameter)
   3. Deploy the notebooks and cluster with the whole stack `make dev env=dev_my_feature job=create_features`.
      Now you should have a new cluster with your libraries installed and a copy of your notebook to work on.
2. Navigate to your notebook directory `/dev/dev_my_feature/create_features` and attach your notebook to the newly created cluster `dev_my_feature_create_features`
3. Explore, apply and try all the changes you need
4. Import the notebooks back to your local development environment `make import_dev_notebooks env=dev_my_feature job=create_features`
5. Refactor your code by extracting the new logic in a transformation function
6. Add unit tests to your function
7. Run an integration test `make integration_test`
8. Deploy the new job in your insulated environment `make deploy env=dev_my_feature`

## Deploy an environment

You can deploy an environment by using the `make deploy` target.

Eg. `make deploy env=staging`

By default you have 2 environments available: `staging` and `production`.

There is a third environment called `integration_test`, which will be deployed without any scheduling, and it is used for running integration tests. There is no need to deploy the `integration_test` environment explicitly, as it is taken care of by the integration test script.

## Run unit test

Execute `make test`

## Run integration tests

The integration test will run on the Databricks platform. It will deploy an independent environment called `integration_test`, and will execute sequentially all the jobs defined in [integration_test.json](./jobs/environments/integration_test.json) under `.active_jobs` section.

`make integration_test`
