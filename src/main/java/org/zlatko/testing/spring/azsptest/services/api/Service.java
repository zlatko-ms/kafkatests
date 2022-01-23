package org.zlatko.testing.spring.azsptest.services.api;

import java.util.Properties;

/** defines the test service abstractions */
public final class Service {

	/** defines a test service that can be configured via a common properties file */
	public interface ConfigurableService {
		// returns the name of the service
		public String getName();
		// returns the service workload type
		public ServiceType getServiceType();
		// runs the service
		public void run();
		// returns general Kafka level properties (if appliable)
		public Properties getKafkaProperties();
		// returns the specific service workload properties
		public Properties getServiceProperties();
	}
	
	/** available service types */
	public enum ServiceType {
		PRODUCER, CONSUMER, METADATA_KAFKA, METADATA_AZURE
	}
}
