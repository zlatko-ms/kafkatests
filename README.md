# Kafkatests

Simple Java Spring Boot app for testing Kafka compliant brokers.

Use Cases : 

- test the compatibility with cloud native event infrastructures (such as Azure Event Hub)
- as a base for Event driven processing to illustrate a number of consumer/producer use cases (spring boot makes it very easy to add http handlers on top).

## Distributions

You can dowload a distribution tarball that is published as artifact upon each build or you can use the Docker image from the  [Docker Hub](https://hub.docker.com/repository/docker/zlatkoa/kafkatests).

## Build 

This is a java project using maven, so in order to build the application all you have to do is issue the following command : 

```console
mvn clean package
```
## Run 

### Parameters 

The application takes two parameters : 

- the test service to execute [producer|consumer|metadata_kafka|metadata_azure]
- the path to the configuration file 

### Services

#### Producer

The **producer** is a simple message producing service.

You can define on which topic you'll send the messages, the batch size , the wait between batches, as well as a maximum number of messages to send.

If the topic does not exist it will be created according to the partitioning and replication parameters provided in the configuration file.

You can also test the topic partitionning, the following partioners are available : 

- unique : sends only to the partition 0, handy when there is no partitionning
- even_odd : if the key hash is an even number, the message will be sent to partition 0, otherwise on parition 1
- even_odd_prime : if the key hash is an even number, the message will be sent to partition 0, if it is odd then parition 1 will be targeted, and if it is a prime number then we'll send it to partition 2

#### Consumer

The **consumer** is a simple message reader that will connect to a topic and read the messages.

#### Metadata_Kafka

The **metadata_kafka** is a simple metadata fetch service using the Kafka Admin API.

It will gather the info on the cluster nodes and topics and display it on the console. 

Very usefull to determine if your cloud native solution can be used with complex kafka orchestration requiring metadata processing.

Currently it shows that there is no compatibility on the fetchMetadata() between the Kafka Admin client and the Azure Event Hub. 

#### Metadata_Azure

The **metadata_azure** is a simple metadata fetch service of the Azure Event Hubs that uses the Azure Java SDK.

The service will inspect your Azure Event Hub and report information on your namespaces (aka topics) and consumer groups.

In order to use it you'll need to provide the azure SPN token infromation as well as the resource group where your Event Hub lives.

See the configuration properties for azure for more details.


### Configuration 

The configuration file uses a properties syntax, each property being prefixed by the service it is related to.

The **kafka** prefix is used to configure all the kafka related properties, such as brooker, sercurity ...

The **consumer** perfix is used to configure all the consumer properties, mainly related to the topic(s) to read ...

The **producer** prefix is used to configure all the producer properties, mainly related to the number of messages to send (-1 for infinite loop) and to the batch size.

The **metadata_kafka** prefix is used to configure the Kafka metadata fetcher. Default settings apply, but you can disable some steps by uncommeting the settings in the sample conf file.

The **metadata_azure** prefix is used to configure the Azure Event Hub metadata fetcher. 

An example of the configuration file for connecting to a local kafka cluster can be found in [src/run/conf/configuration.properties](src/run/conf/configuration.properties) .

An example of a configuration file for connecting to an Azure Event Hub can be found in [src/run/conf/configuration.properties.azure](src/run/conf/configuration.properties.azure) . Update the bootstrap and jaas config values to fit your environnement.

## Override via Environnement variables

In order to avoid storing sensible infromation in the configuration file, the application will overload all the **serviceName** prefixed properties with the values of environnement variables starting with KAFKA.

For instance, in order to overload : 

- kafka.bootstrap.servers property, define the KAFKA_BOOTSTRAP_SERVERS environnement variable
- producer.messages.max, define the PRODUCER_MESSAGES_MAX environnement variable. 
- ...

This system provides a conventient way to run the application in a container, by propagating sensible values via the environnement variables, which can be easly secured on your CI/CD side.

## Run Locally 

To run the app launch the following cmd line : 

```console
java -jar azsptest-<version>-SNAPSHOT.jar serviceName configurationFle
```

Where : 
- **serviceName** is one of the following : **consumer** , **producer**, **metadata_kafka** or **metadata_azure**
- **configurationFle** is a path to a valid configuration properties file

Note that the jar file will be created under the target dir once you perform the build process, so an example of usage from the source dir would be : 

```console
java -jar azsptest-<version>-SNAPSHOT.jar producer src/run/configuration.properties.azure
```


## Run from Docker

Unless you are running against a local kafka cluster witout any authentication, please ensure that the following evironnement variables are passed to the docker executor : 

KAFKA_BOOTSTRAP_SERVERS

KAFKA_SECURITY_PROTOCOL

KAFKA_SASL_MECHANISM

KAFKA_SASL_JAAS_CONFIG

If your're using the **metadata_azure** service you'll also need to provide the following env vars : 

METADATA_AZURE_AUTH_CLIENT_ID

METADATA_AZURE_AUTH_CLIENT_SECRET

METADATA_AZURE_AUTH_TENANT_ID

METADATA_AZURE_AUTH_SUBSCRIPTION_ID

METADATA_AZURE_RG_NAME




