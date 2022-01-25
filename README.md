# Purpose

Simple Java application to test Kafka compatible cloud brokers.

In the current 1.1.0 version the code provides the support for the following services and cloud solutions: 

|Service |Purpose  |Client APIs|Backend Solutions
|--|--|--|--|
|Producer|Produce Events, with configurable event payload size ranging from 1k to 2M  | Kafka|Kafka, EventHub |
|||EventHub|EventHub
|Consumer|Consumes messages from the topics/hubs|Kafka|Kafka,Event Hub|
|||EventHub|EventHub|
Metadata|Fetches metadata (topics, cluser, consumer groups)|Kafka|Kafka|
|||EventHub|EventHub

So far, with the Kafka client you can only address the Consumer and Producer services on both Kafka and Azure Event Hub.

Indeed the metadata fetching is not supported in the current version of the Azure Event Hub and this requires the usage of the specific client.

# Code

The app uses Spring Boot, not for the DI features, but more for additional packaging facilities and the fact that you can quicky add some http handlers on top to fit your use cases. 

The code is pretty straightforward, and I hope readable. It is not intended for production purposes hence it has not been extensively TU-ed.

In order to navigate in the code use the following pointers: 

* **api** package contains all the abstract interfaces used by the services and the application
* **base** package(s) contain all the abstract classes that define generic behaviors
* **provider** package(s) contain provider (Kafka, Azure) specific implementations of the above defined abstractions.

# Build

It is a standard maven build so all you have to do is clone the source and issue the following command: 

```console
mvn clean package
```

# Run 

The jar has a main entry point that will start the application. 

The usage is the following : 

```console
Usage  : java -jar archive.jar service provider confFilePath
Params :
 service      : service to run, possible values = producer|consumer|metadata
 provider     : infra provider to use, possible values = kafka|azure
 confFilePath : path to a valid configuration file
```

# Configuration 

The application can be configured via properties file and/or environment variables.

The environment variables take precedence, mainly in order to provide a safe way for handling sensitive information such as secrets and to avoid to store them in files.

## Files

A sample configuration file is provided in [src/run/conf/configuration.properties](src/run/conf/configuration.properties) .

You can use it as/is to connect to a local Kafka cluster that does not require authentication.

In order to address an auth protected Kafka cluster, you’ll have to provide the jaas properties that fit your auth plan.

In order to address an Azure event hub, you’ll also have to fill out the azure prefix properties : 

* azure.auth.spn properties require the information on the Azure Service Principal that has management access to you Event Hub namespace
* azure.auth.sas properties require the information on the Shared Access key configured for reading and writing on the Event Hub

Properties are pretty self-explanatory with some comments in the sample files, so it should not be too hard to work around them.

## Environnent variables

In order to override a property, provide an env variable in upper case where the dots (.) are replaced by underscores (_).

For instance , to override the  **kafka.sasl.mechanism property** value, provide the **KAFKA_SASL_MECHANISM** environment variable.


# Docker Container

A docker container is available on the [Docker Hub](https://hub.docker.com/repository/docker/zlatkoa/kafkatests).

Use environnement variable override to safely provide the sensible parameters to your container.

# Performance bench

One of the purpose of this project was also to test the performances on the possible client/backend combinations.

Hence the producer and consumer services provide specifc INFO level messages that can be used to create a CSV file you can then work on your faviorite visualizaton tool. 

You can use the **parselog.sh** script located at root level to parse an application log and transform it to a csv file : 
```console
parselog.sh kafkatests.log > kakfaperf.csv
``` 






