package org.zlatko.testing.spring.azsptest.services.api;

import java.util.Properties;

import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

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
		// returns the specific service workload properties
		public Properties getServiceProperties();
		// returns all the configuration
		public ServiceConfiguration getGlobalConfiguration();
		// returns the value of the mandatory service parameter, throws runtime exception otherwise
		public String getMandatoryServiceProperty(String propName);
		// returns the value of the mandatory param, throw rt exception otherwise
		public String getMandatoryProperty(String prefix,String propName);
	}
	
	/** available service types */
	public enum ServiceType {
		PRODUCER, CONSUMER, METADATA_KAFKA, METADATA_AZURE, PRODUCER_AZURE
	}
}
