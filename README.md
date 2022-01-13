# kafkatests

Simple Java Spring Boot app for testing Kafka compliant brokers

## build 

mvn clean package

## run 

To run the app launch the following cmd line : 

java -jar target/azsptest-0.0.1-SNAPSHOT.jar serviceName configuratioFile

### supported services 

producer : produces messages per batch, with possibiluty to limit the number of messages.

consumer : consumes the messages from a given topic

### configuration 

Each service configuration property is prefixed by the name of the service.

Examples can be found in src/run/resources, use configuration.properties for local run, configuration.properties.azure for azure.

If you are using Azure then please update the brooker and jaas config params to align with your entry point and access keys
