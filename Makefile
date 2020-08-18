GIT_REVISION:=$(shell git rev-parse --short HEAD)
JAR_VERSION:=$(shell cat scala/build.sbt | grep "version := " | sed -e 's/.*\(version := \)//' | tr -d "\"")
ARTIFACT_ID:=$(JAR_VERSION)-$(GIT_REVISION)
assembly: ## Creates artifacts
	sbt assembly
	echo "OK, assembled."


.PHONY: test
test: ## Runs unit tests
	cd scala && $(MAKE) test
	echo "OK, tested."

.PHONY: integration_test
integration_test: publish ## Deploys integration_test jobs to Databricks and runs them
	bash scripts/deploy_jobs.sh integration_test "$(ARTIFACT_ID)"
	bash scripts/run_jobs.sh integration_test

.PHONY: install_on_cluster
install_on_cluster: assembly ## Installs current jar on a specified cluster [make install_on_cluster cluster=test]
	bash scripts/install_library_on_cluster.sh $(cluster)

.PHONY: publish
publish: assembly ## Publishes artifacts on Databricks dbfs
	./scripts/publish_artifacts.sh $(ARTIFACT_ID)

.PHONY: deploy
deploy: publish ## Deploys artifacts, notebooks and jobs to Databricks [make deploy env=staging]
	bash scripts/deploy_jobs.sh $(env) $(ARTIFACT_ID)

.PHONY: dev
dev: publish ## Deploys artifacts, notebooks to Databricks [make dev env=staging job=create_features]
	bash scripts/dev.sh $(env) $(job) $(ARTIFACT_ID)

.PHONY: import_dev_notebooks
import_dev_notebooks: ## Imports notebooks from dev [make import_dev_notebooks env=staging job=create_features]
	bash scripts/import_notebooks.sh "/dev/$(env)/$(job)/$(ARTIFACT_ID)"

.PHONY: import_job_notebooks
import_job_notebooks: ## Imports notebooks from a job environment deployment [make import_job_notebooks env=staging]
	bash scripts/import_notebooks.sh /pipeline/$(env)/$(ARTIFACT_ID)

.PHONY: import_notebooks
import_notebooks: ## Imports notebooks from a job environment deployment [make import_job_notebooks path=/path/to/notebooks]
	bash scripts/import_notebooks.sh $(path)

.PHONY: help
help: ## Shows this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help