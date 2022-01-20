package org.zlatko.testing.spring.azsptest.kafka;

import java.util.Properties;

import org.zlatko.testing.spring.azsptest.kafka.Kafka.KafkaTestService;
import org.zlatko.testing.spring.azsptest.kafka.Kafka.TestWorkloadType;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

/**
 * base kafka test service class, provides common configuration processing
 * facilities
 */
abstract class BaseKafkaService implements KafkaTestService {

	private final static String KAFKA_SHARED_SERVICE = "kafka";

	private Properties kafkaProperties;
	private Properties serviceProperties;
	private TestWorkloadType serviceType;

	protected BaseKafkaService(TestWorkloadType serviceType, ServiceConfiguration appConfig) {
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
	public TestWorkloadType getServiceType() {
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