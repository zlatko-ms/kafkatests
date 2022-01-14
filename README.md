# Kafkatests

Simple Java Spring Boot app for testing Kafka compliant brokers.

Can be used as a base for Event driven processing to illustrate a number of consumer/producer use cases.

## Build 

### Build the Java packages

This is a java project using maven, so in order to build the application please issue the foloowing command : 

```console
mvn clean package
```

### Build a docker container

I'm definetly not a fan of the maven docker plugins, as they are cumbersome and usually end up with a huge lost of time.

Hence I prefear to use docker commands directly in the CI/CD pipeline and avoid any intermediary shells to maintain.

Here is an extract of the commands used to build the container in the CI/CD pipeline, that should work on your local workstation : 

```console

mkdir target/docker
cp target/*.jar target/docker/.
cp src/main/docker/Dockerfile target/docker/.
cp src/main/shell/startapp.sh target/docker/.
cp src/run/resources/configuration.properties target/docker/.
export RELEASE_TAG=`cat RELEASE`
cd target/docker
docker build -t your.docker.registry.io/kafkatests:"$RELEASE_TAG" .
docker push your.docker.registry.io/kafkatests:"$RELEASE_TAG"
```

## Run 

### Parameters 

The application takes two parameters : 

- the test service to execute [producer|consumer]
- the path to the configuration file 

### Configuration 

The configuration file uses a properties syntax, each property being prefixed by the service it is related to.

The **kafka** prefix is used to configure all the kafka related properties, such as brooker, sercurity ...

The **consumer** perfix is used to configure all the consumer properties, mainly related to the topic(s) to read ...

The **producer** prefix is used to configure all the producer properties, mainly related to the number of messages to send (-1 for infinite loop) and to the batch size.

An example of the configuration file for connecting to a local kafka cluyster can be found in [src/run/resources/configuration.properties](src/run/resources/configuration.properties) .

An example of a configuration file for connecting to an Azure Event Hub can be found in [src/run/resources/configuration.properties.azure](src/run/resources/configuration.properties.azure) . Update the bootstrap and jaas config values to fit your environnement.

## Override via Environnement variables

In order to avoid storing sensible infromation in the configuration file, the application will overload all the **kafka** prefixed properties with the values of environnement variables starting with KAFKA.

For instance to overload the kafka.bootstrap.servers property, define the KAFKA_BOOTSTRAP_SERVERS environnement variable. The Env variable will take precedence over the property.

This system provides a conventient way to run the application in a container, by propagating sensible values via the environnement variables, which can be easly  secured on your CI/CD side.

The **kafka** service properties can be fetched from the envrionnement, in order to run the app from Docker containers without propagating a conf file 

### Run Locally 

To run the app launch the following cmd line : 

```console
java -jar target/azsptest-<version>-SNAPSHOT.jar serviceName configurationFle
```

Where : 
- **serviceName** is one of the following : **consumer** or **producer**
- **configurationFle** is a path to a valid configuration properties file

## Run from Docker

Just ensure that the foloowing evironnement variables are passed to the docker executor : 

KAFKA_BOOTSTRAP_SERVERS

KAFKA_SECURITY_PROTOCOL

KAFKA_SASL_MECHANISM

KAFKA_SASL_JAAS_CONFIG

