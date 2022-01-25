package org.zlatko.testing.spring.azsptest.services.base;

import java.util.Properties;

import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.base.Strings;

/**
 * base kafka test service class, provides common configuration processing
 * facilities
 */
public abstract class AbstractConfigurableService implements Service.ConfigurableService {

	private ServiceConfiguration globalConfiguration;
	private Service.ServiceType serviceType;

	protected AbstractConfigurableService(Service.ServiceType serviceType, ServiceConfiguration appConfig) {
		this.serviceType = serviceType;
		globalConfiguration=appConfig;
	}

	@Override
	public Service.ServiceType getServiceType() {
		return this.serviceType;
	}

	@Override
	public String getName() {
		return getServiceType().name().toLowerCase();
	}

	@Override
	public Properties getServiceProperties() {
		return globalConfiguration.getConfiguration(getName());
	}
	
	@Override
	public ServiceConfiguration getGlobalConfiguration() {
		return globalConfiguration;
	}
	
	
	@Override
	public String getMandatoryServiceProperty(String propName) {
		return getMandatoryProperty(getServiceType().name().toLowerCase(), propName);
	}
	
	@Override
	public String getMandatoryProperty(String prefix,String propName) {
		String val =getGlobalConfiguration().getConfiguration().getProperty(prefix+"."+propName,null);
		if (Strings.isNullOrEmpty(val)) {
			String msg = String.format("Missing mandatory configuration parmeter %s.%s",
					prefix, propName);
			throw new RuntimeException(msg);
		}
		return val;
	}
	
}