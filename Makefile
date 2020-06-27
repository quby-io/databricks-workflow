GIT_REVISION:=$(shell git rev-parse --short HEAD)
JAR_VERSION:=$(shell cat scala/build.sbt | grep "version := " | sed -e 's/.*\(version := \)//' | tr -d "\"")

assembly: ## Creates artifacts
	sbt assembly
	echo "OK, assembled."

.PHONY: test
test: ## Runs unit tests
	cd scala && $(MAKE) test
	echo "OK, tested."

.PHONY: integration_test
integration_test: publish ## Deploys integration_test jobs to Databricks and runs them
	bash scripts/deploy_jobs.sh --environment=integration_test --build_version="$(JAR_VERSION)-$(GIT_REVISION)"
	bash scripts/run_jobs.sh integration_test

.PHONY: install_on_cluster
install_on_cluster: assembly ## Installs current jar on a specified cluster [make install_on_cluster cluster=test]
	bash scripts/install_library_on_cluster.sh $(cluster)

.PHONY: publish
publish: assembly ## Publishes artifacts on Databricks dbfs
	./scripts/publish_artifacts.sh "$(JAR_VERSION)-$(GIT_REVISION)"

.PHONY: deploy
deploy: publish ## Deploys artifacts, notebooks and jobs to Databricks [make deploy env=staging]
	echo "Uploading notebooks and jar"
	bash scripts/upload_notebooks_and_jar.sh --environment=$(env) --artifact_id="$(JAR_VERSION)-$(GIT_REVISION)"
ifndef skip-jobs
	echo "Deploying jobs"
	bash scripts/deploy_jobs.sh --environment=$(env) --artifact_id="$(JAR_VERSION)-$(GIT_REVISION)"
endif

.PHONY: import_notebooks
import_notebooks: ## Imports notebooks from environment deployment [make import_notebooks env=staging]
	bash scripts/import_notebooks.sh $(env) "$(JAR_VERSION)-$(GIT_REVISION)"

.PHONY: help
help: ## Shows this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'

.DEFAULT_GOAL := help