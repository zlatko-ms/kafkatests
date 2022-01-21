package org.zlatko.testing.spring.azsptest.kafka;

import java.util.List;
import java.util.Properties;

import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.collect.Lists;

public class Kafka {

	/** defines the types of test workloads */
	public enum TestWorkloadType {
		PRODUCER, CONSUMER, METADATA_KAFKA
	}

	/** defines the kafka test service interface */
	public interface KafkaTestService {

		// returns the name of the service
		public String getName();

		// returns the service workload type
		public TestWorkloadType getServiceType();

		// runs the service
		public void run();

		// returns general Kafka level properties (for joining the cluster)
		public Properties getKafkaProperties();

		// returns the specific service workload properties
		public Properties getServiceProperties();
	}

	/** simple kafka message abstraction */
	public interface KafkaTestMessage {
		String getKey();

		Object getValue();

		String getValueAsJson();
	}

	/** builds the kafka service to test */
	public static final KafkaTestService buildTestService(TestWorkloadType type, ServiceConfiguration appConf) {
		switch (type) {
		case PRODUCER:
			return new SimpleKafkaProducer(appConf);
		case CONSUMER:
			return new SimpleKafkaConsumer(appConf);
		case METADATA_KAFKA:
			return new MetadataKafkaTestService(appConf);
		}
		throw new IllegalArgumentException(type.name() + " is not supported yet");
	}

	/** list the available services with custom separator*/
	public static String getValidServiceTypesAsString(String separator) {
		List<String> validServices = Lists.newArrayList();
		for (TestWorkloadType type : TestWorkloadType.values()) {
			validServices.add(type.name().toLowerCase());
		}
		return String.join(separator, validServices);
	}

	/** list the available services */
	public static String getValidServiceTypesAsString() {
		return getValidServiceTypesAsString(",");
	}

}
