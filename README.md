Executor for AWS Lambda "instance-resolver"
===

Executor to execute AWS Lambda "instance-resolver" and process results. Project in the context of Hauptseminar "Serverless & IoT", Summer Term 2020 - Munich University of Applied Sciences.

Running on Java 8 and Maven.

Command Line Arguments
---
* first argument has to be ARN of the AWS Lambda. Lambda has to be in Region EU_CENTRAL_1 or US_EAST_2.
* second argument has to be one of
    * `-lt`: LifetimeExecutor
    * `-du`: DurableExecutor
    * `-co`: CountedExecutor. Requires one more integer parameter, specifying the number of AWS Lambda calls to do.
    * `-rt`: RuntimeExecutor
