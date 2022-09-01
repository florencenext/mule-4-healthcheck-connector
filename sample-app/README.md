# mule-4-healthcheck-connector-sample-app

Mule 4 project to show healthcheck capabilities and usage in a similar use case scenario.


# Use case

The sample app replicate a 3 layer API-Led Architecture composed by the following services:

- 1 experience API
- 2 process API
- 2 system API 

with off course a bunch of external sytem:

- 2 HTTP Services
- 1 SOAP Service
- 1 Active MQ Message broker
- 1 DB MySQL

# Run the sample app

## Spin un local environment

In the docker folder present in this repo run the following command:
```
docker-compose up -d
```
in order to spin up all the external services required by the application.
Modify the docker-compose.yaml file to test other version/implementation of the external services.


## Run the project from Anypoint Studio
Import this project in Anypoint Studio with the Import wizard with the "Anypoint Studio project from File System" option.
Then as usual run the application.

## Test the endpoint 

The app will start on port 8094 with basepath /api, and each microservices will expose an /healthcheck endpoint.

A list of curl command to test the /healthcheck endpoint of all the api led:

```sh
curl 'localhost:8094/api/xapi-1/healthcheck'
curl 'localhost:8094/api/papi-1/healthcheck'
curl 'localhost:8094/api/papi-2/healthcheck'
curl 'localhost:8094/api/sapi-1/healthcheck'
curl 'localhost:8094/api/sapi-2/healthcheck'

```


# Prerequisites

- Docker
- Anypoint Studio

