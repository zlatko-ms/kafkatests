# Kafkatests

Simple Java Spring Boot app for testing Kafka compliant brokers.

Can be used as a base for Event driven processing to illustrate a number of consumer/producer use cases.

## Build 

### Build the Java packages

This is a java project using maven, so in order to build the application please issue the foloowing command : 

```console
mvn clean package
```

### Binary distribution 

A tarball containing the jar archive and sample configuration file is available as an artifact of the Build & Deploy pipeline.

### Docker Container

The container is build upon changed and deployed to the the [Docker Hub](https://hub.docker.com/repository/docker/zlatkoa/kafkatests)

## Run 

### Parameters 

The application takes two parameters : 

- the test service to execute [producer|consumer|metadata]
- the path to the configuration file 

### Configuration 

The configuration file uses a properties syntax, each property being prefixed by the service it is related to.

The **kafka** prefix is used to configure all the kafka related properties, such as brooker, sercurity ...

The **consumer** perfix is used to configure all the consumer properties, mainly related to the topic(s) to read ...

The **producer** prefix is used to configure all the producer properties, mainly related to the number of messages to send (-1 for infinite loop) and to the batch size.

The **metadata** prefix is used to configure the metadata fetcher. Default settings apply, but you can disable some steps by uncommeting the settings in the sample conf file.

An example of the configuration file for connecting to a local kafka cluyster can be found in [src/run/conf/configuration.properties](src/run/conf/configuration.properties) .

An example of a configuration file for connecting to an Azure Event Hub can be found in [src/run/conf/configuration.properties.azure](src/run/conf/configuration.properties.azure) . Update the bootstrap and jaas config values to fit your environnement.

## Override via Environnement variables

In order to avoid storing sensible infromation in the configuration file, the application will overload all the **serviceName** prefixed properties with the values of environnement variables starting with KAFKA.

For instance, in order to overload : 
- kafka.bootstrap.servers property, define the KAFKA_BOOTSTRAP_SERVERS environnement variable
- producer.messages.max, define the PRODUCER_MESSAGES_MAX environnement variable. 

This system provides a conventient way to run the application in a container, by propagating sensible values via the environnement variables, which can be easly  secured on your CI/CD side.

The **kafka** service properties can be fetched from the envrionnement, in order to run the app from Docker containers without propagating a conf file 

### Run Locally 

To run the app launch the following cmd line : 

```console
java -jar target/azsptest-<version>-SNAPSHOT.jar serviceName configurationFle
```

Where : 
- **serviceName** is one of the following : **consumer** , **producer** or **metadata**
- **configurationFle** is a path to a valid configuration properties file

#### Consumer

The **producer** service sends batches of messages on a given topic, that can be created on the fly.

The **consumer** service reads messgages from a given topic.

The **metadata** service gathers information on the cluster (nodes) and on the topics (partitions, replication, ... ) available on the cluster.

## Run from Docker

Just ensure that the foloowing evironnement variables are passed to the docker executor : 

KAFKA_BOOTSTRAP_SERVERS

KAFKA_SECURITY_PROTOCOL

KAFKA_SASL_MECHANISM

KAFKA_SASL_JAAS_CONFIG

