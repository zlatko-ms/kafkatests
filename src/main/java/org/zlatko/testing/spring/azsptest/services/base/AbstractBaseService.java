package org.zlatko.testing.spring.azsptest.services.base;

import java.util.Properties;

import org.zlatko.testing.spring.azsptest.services.api.ConfigurableService;
import org.zlatko.testing.spring.azsptest.services.api.ServiceType;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

/**
 * base kafka test service class, provides common configuration processing
 * facilities
 */
public abstract class AbstractBaseService implements ConfigurableService {

	private final static String KAFKA_SHARED_SERVICE = "kafka";

	private Properties kafkaProperties;
	private Properties serviceProperties;
	private ServiceType serviceType;

	protected AbstractBaseService(ServiceType serviceType, ServiceConfiguration appConfig) {
		this.serviceType = serviceType;
		kafkaProperties = new Properties();
		kafkaProperties.putAll(appConfig.getServiceConfiguration(KAFKA_SHARED_SERVICE));
		serviceProperties = new Properties();
		serviceProperties.putAll(appConfig.getServiceConfiguration(getName()));
	}

	protected void addSpecificKafkaProp(String key, String value) {
		kafkaProperties.put(key, value);
	}

	@Override
	public ServiceType getServiceType() {
		return this.serviceType;
	}

	@Override
	public String getName() {
		return getServiceType().name().toLowerCase();
	}

	@Override
	public Properties getKafkaProperties() {
		return kafkaProperties;
	}

	@Override
	public Properties getServiceProperties() {
		return serviceProperties;
	}
}