package org.zlatko.testing.spring.azsptest.services.api;

import java.util.Properties;

/** defines the low level configuable service interface */
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