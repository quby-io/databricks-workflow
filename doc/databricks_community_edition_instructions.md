# Differences from the paid version

Working with the community edition of Databricks has some limitations:
1. Deploying jobs to Databricks is not possible
2. Integration test cannot be run

This is because `jobs` are not available in the community edition. Also, both require to set-up additional worker nodes,
which is not possible in the community edition. You only get one driver node. A customized version of the 
deployment needs to be carried out in a later step when available. 

## Getting started

In order to get started with a community version of Databricks, follow the steps below:

1. Setup your [Databricks community account](https://databricks.com/try-databricks) (Databricks provides a driver 
instance for free)
2. Install and configure your [Databricks CLI](https://docs.databricks.com/dev-tools/cli/index.html) (This example 
repository uses your default Databricks profile) 
   1. `pip install databricks-cli`
   2. `databricks configure` (you'll be prompted for your domain, username and password)
3. Install [jq](https://stedolan.github.io/jq/), we use it to parse and combine JSON configuration files for the jobs
   1. `brew install jq`
4. Clone this repository on your local machine 
   1. `git clone git@github.com:quby-io/databricks-workflow.git`

You are good to go :)

Try to execute `make help`, this will show the the following actions (note that `deploy` and `integration test` are not 
available with the community edition):
- assembly
- help
- import_notebooks
- install_on_cluster
- publish
- test
- ~~deploy~~
- ~~integration_test~~

You can now continue with the `project structure` in the original [Readme.md](./jobs/Readme.md).