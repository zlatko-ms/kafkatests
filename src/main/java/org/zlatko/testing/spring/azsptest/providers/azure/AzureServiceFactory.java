package org.zlatko.testing.spring.azsptest.providers.azure;

import org.zlatko.testing.spring.azsptest.providers.azure.impl.EventHubMetadataService;
import org.zlatko.testing.spring.azsptest.providers.azure.impl.EventHubProducerService;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.api.Service.ConfigurableService;
import org.zlatko.testing.spring.azsptest.services.api.Service.ServiceType;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

public class AzureServiceFactory implements Service.ServiceProvider {

	@SuppressWarnings("incomplete-switch")
	@Override
	public ConfigurableService getService(ServiceType type, ServiceConfiguration conf) {
		switch (type) {
		case PRODUCER:
			return new EventHubProducerService(conf);
		case METADATA:
			return new EventHubMetadataService(conf);
		}
		throw new UnsupportedOperationException("Azure provider does not support the service " + type.name().toLowerCase());
	}
}
