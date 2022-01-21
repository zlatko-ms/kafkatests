package org.zlatko.testing.spring.azsptest.services;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.zlatko.testing.spring.azsptest.services.azure.MetadataEventHubTestService;
import org.zlatko.testing.spring.azsptest.services.kafka.SimpleKafkaConsumer;
import org.zlatko.testing.spring.azsptest.services.kafka.SimpleKafkaMetadataFetcher;
import org.zlatko.testing.spring.azsptest.services.kafka.SimpleKafkaProducer;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.collect.Lists;

public final class Services {

	/** defines the types of test workloads */
	public enum ServiceType {
		PRODUCER, CONSUMER, METADATA_KAFKA, METADATA_AZURE
	}

	/** defines the kafka test service interface */
	public interface TestService {
		// returns the name of the service
		public String getName();
		// returns the service workload type
		public ServiceType getServiceType();
		// runs the service
		public void run();
		// returns general Kafka level properties (for joining the cluster)
		public Properties getKafkaProperties();
		// returns the specific service workload properties
		public Properties getServiceProperties();
	}

	/** basic interface for getting metadata */
	public interface MetadataTestService extends TestService {
		// returns one description line per node if node fetching is supported, Optional.Empty if not supported
		Optional<List<String>> getNodesDescriptionDescLines();
		// returns one description line per topic if topc fetching is supported, Optional.Empty if not supported
		Optional<List<String>> getTopicsDescriptionDescLines();
		// returns one description line per consumer groups if cg fetching is supported, Optional.Empty if not supported
		Optional<List<String>> getConsumerGroupDescLines();
	}
	
	/** simple kafka message abstraction */
	public interface PubSubMessage {
		// message key
		String getKey();
		// message value (as object)
		Object getValue();
		// message value as Json String
		String getValueAsJson();
	}

	/** builds the kafka service to test */
	@SuppressWarnings("incomplete-switch")
	public static final TestService buildTestService(ServiceType type, ServiceConfiguration appConf) {
		switch (type) {
		case PRODUCER:
			return new SimpleKafkaProducer(appConf);
		case CONSUMER:
			return new SimpleKafkaConsumer(appConf);
		case METADATA_KAFKA:
			return new SimpleKafkaMetadataFetcher(appConf);
		case METADATA_AZURE:
			return new MetadataEventHubTestService(appConf);
		}
		throw new IllegalArgumentException(type.name() + " is not supported yet");
	}

	/** list the available services with custom separator*/
	public static String getValidServiceTypesAsString(String separator) {
		List<String> validServices = Lists.newArrayList();
		for (ServiceType type : ServiceType.values()) {
			validServices.add(type.name().toLowerCase());
		}
		return String.join(separator, validServices);
	}

	/** list the available services */
	public static String getValidServiceTypesAsString() {
		return getValidServiceTypesAsString(",");
	}

}
