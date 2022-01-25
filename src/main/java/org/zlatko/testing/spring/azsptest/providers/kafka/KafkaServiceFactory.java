package org.zlatko.testing.spring.azsptest.providers.kafka;

import org.zlatko.testing.spring.azsptest.providers.kafka.impl.KafkaConsumerService;
import org.zlatko.testing.spring.azsptest.providers.kafka.impl.KafkaMetadataService;
import org.zlatko.testing.spring.azsptest.providers.kafka.impl.KafkaProducerService;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.api.Service.ConfigurableService;
import org.zlatko.testing.spring.azsptest.services.api.Service.ServiceType;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

public class KafkaServiceFactory implements Service.ServiceProvider {

	@Override
	public ConfigurableService getService(ServiceType type, ServiceConfiguration conf) {
		switch(type) {
			case PRODUCER:
				return new KafkaProducerService(conf);
			case CONSUMER : 
				return new KafkaConsumerService(conf);
			case METADATA:
				return new KafkaMetadataService(conf);
		}
		throw new UnsupportedOperationException("Kafka provider does not support the service "+type.name().toLowerCase());
	}


}
