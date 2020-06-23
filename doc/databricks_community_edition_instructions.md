# Differences from the paid version

The main differences of working with the community edition are the following: You only get a driver node, therefore
you cannot do integration tests. The integration tests rely heavily on worker nodes as well. You can't get access
to the `jobs` functionality in Databricks, therefore a customized version of the deployment needs to be carried out
in a later step. 

## Getting started

In order to get started with a community version of Databricks, follow the steps below.

1. Setup your [Databricks community account](https://databricks.com/try-databricks) (Databricks provides a driver instance for free)
2. Install and configure your [Databricks CLI](https://docs.databricks.com/dev-tools/cli/index.html) (This example repository uses your default Databricks profile) 
   1. `pip install databricks-cli`
   2. `databricks configure` (you'll be prompted for your domain, username and password)
3. Install [jq](https://stedolan.github.io/jq/), we use it to parse and combine JSON configuration files for the jobs.
   1. `brew install jq`
4. Clone this repository on your local machine 
   1. `git clone git@github.com:quby-io/databricks-workflow.git`

You are good to go :)

Try to execute `make help`, this will show the the following actions (note that `deploy`, `integration test` and 
`import_notebooks` are not available with the community edition):
- assembly
- help
- import_notebooks
- publish
- test
- ~~deploy~~
- ~~integration_test~~
- ~~import_notebooks~~

You can now continue with the `project structure` in the original [Readme.md](./jobs/Readme.md).