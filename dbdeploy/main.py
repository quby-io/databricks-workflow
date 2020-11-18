import json
import os
from databricks_cli.sdk.api_client import ApiClient

client = ApiClient(user="", token="", host="https://quby.cloud.databricks.com")

base_path = "/Users/erni/git/databricks-workflow/jobs"


def get_config(path, params={}):
    with open(path, 'r') as file:
        data = file.read()
        for (k, v) in params.items():
            data = data.replace(k, v)
    return data

def merge_dicts(*dict_args):
    """
    Given any number of dictionaries, shallow copy and merge into a new dict,
    precedence goes to key value pairs in latter dictionaries.
    """
    result = {}
    for dictionary in dict_args:
        result = merge(result, dictionary)
    return result

def merge(source, destination):
    """
    run me with nosetests --with-doctest file.py

    >>> a = { 'first' : { 'all_rows' : { 'pass' : 'dog', 'number' : '1' } } }
    >>> b = { 'first' : { 'all_rows' : { 'fail' : 'cat', 'number' : '5' } } }
    >>> merge(b, a) == { 'first' : { 'all_rows' : { 'pass' : 'dog', 'fail' : 'cat', 'number' : '5' } } }
    True
    """
    for key, value in source.items():
        if isinstance(value, dict):
            # get node or create one
            node = destination.setdefault(key, {})
            merge(value, node)
        else:
            destination[key] = value

    return destination

def get_job_config(env, job_name):
    params = {
        "{{ENVIRONMENT}}": env,
        "{{JOB_NAME}}": job_name,
        "{{JAR_DESTINATION}}": "dbfs:/toon-libs-dev/eneco_dev_erni/jars/toon-datascience-assembly-4.3.9.jar",
        "{{NOTEBOOK_ROOT_PATH}}": "/Users/erni.durdevic@quby.com/multitask_jobs",
    }
    base = json.loads(get_config(os.path.join(base_path, "job_configuration", "_base_.json"), params))
    job = json.loads(get_config(os.path.join(base_path, "job_configuration", job_name + ".json"), params))
    env = json.loads(get_config(os.path.join(base_path, "environments", env + ".json"), params))["config"]
    return merge_dicts(base, job, env)["settings"]

def configure_pipeline(pipeline, env):
    configured_tasks = []
    for (i, task) in enumerate(pipeline["tasks"]):
        task_key = task["task_key"]
        task_config = get_job_config(env, task_key)
        configured_tasks.append(merge_dicts(task, task_config))

    pipeline["tasks"] = configured_tasks
    return pipeline


pipeline = json.loads(get_config(os.path.join(base_path, "multitask", "pipeline.json")))
configured_pipeline = configure_pipeline(pipeline, "staging")
print(json.dumps(configured_pipeline))


client.perform_query('POST', '/jobs/create', data=configured_pipeline)

# client.perform_query('POST', '/jobs/run-now', data={"job_id": 208612})

# print(jobs)