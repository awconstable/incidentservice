# incidentservice
An incident repository to measure team performance

[![CircleCI](https://circleci.com/gh/awconstable/incidentservice.svg?style=shield)](https://circleci.com/gh/awconstable/incidentservice)
![CodeQL](https://github.com/awconstable/incidentservice/workflows/CodeQL/badge.svg)
[![codecov](https://codecov.io/gh/awconstable/incidentservice/branch/main/graph/badge.svg)](https://codecov.io/gh/awconstable/incidentservice)
[![Libraries.io dependency status for GitHub repo](https://img.shields.io/librariesio/github/awconstable/incidentservice.svg)](https://libraries.io/github/awconstable/incidentservice)
[![dockerhub](https://img.shields.io/docker/pulls/awconstable/incidentservice.svg)](https://cloud.docker.com/repository/docker/awconstable/incidentservice)

## Limitations

This application is a proof of concept, it is **not** production ready.
A non-exhaustive list of known limitations:
* No security whatsoever - anonymous users can easily delete or alter all data

### Run app as a Docker container

*See https://github.com/docker-library/openjdk/issues/135 as to why spring.boot.mongodb.. env vars don't work*

```
docker stop incidentservice
docker rm incidentservice
docker pull awconstable/incidentservice
docker run --name incidentservice -d -p 8080:8080 --network <mongo network> -e spring_data_mongodb_host=<mongo host> -e spring_data_mongodb_port=<mongo port> -e spring_data_mongodb_database=<mondo db> -e server_ssl_key-store-type=<keystore type - PKCS12> -e server_ssl_key-store=/incidentservice.p12 -e server_ssl_key-store-password=<password> -e server_ssl_key-alias=<alias> -e spring_cloud_consul_host=<consul host> -e spring_cloud_consul_port=<consul port> -v <cert path>:/incidentservice.p12 awconstable/incidentservice:latest
```

[Spring Boot Initilizr Config](https://start.spring.io/#!type=maven-project&language=java&platformVersion=2.4.0.RELEASE&packaging=jar&jvmVersion=11&groupId=team&artifactId=incidentservice&name=incidentservice&description=A%20deployments%20repository%20to%20measure%20team%20performance&packageName=team.incidentservice&dependencies=devtools,lombok,web,data-mongodb,testcontainers,security,actuator,prometheus,cloud-starter-consul-discovery,cloud-starter-consul-config)