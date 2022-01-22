package org.zlatko.testing.spring.azsptest.services.api;

import java.util.List;

import org.zlatko.testing.spring.azsptest.services.provider.azure.EventHubMetadataFetcher;
import org.zlatko.testing.spring.azsptest.services.provider.kafka.SimpleKafkaConsumer;
import org.zlatko.testing.spring.azsptest.services.provider.kafka.SimpleKafkaMetadataFetcher;
import org.zlatko.testing.spring.azsptest.services.provider.kafka.SimpleKafkaProducer;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.collect.Lists;

public final class ServiceFactory {

	/** builds service to test */
	public static final ConfigurableService buildTestService(ServiceType type, ServiceConfiguration appConf) {
		switch (type) {
		case PRODUCER:
			return new SimpleKafkaProducer(appConf);
		case CONSUMER:
			return new SimpleKafkaConsumer(appConf);
		case METADATA_KAFKA:
			return new SimpleKafkaMetadataFetcher(appConf);
		case METADATA_AZURE:
			return new EventHubMetadataFetcher(appConf);
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
