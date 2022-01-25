package org.zlatko.testing.spring.azsptest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Properties;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

class ServiceConfigurationTest {
	

	@Test
	void testGetServiceConfiguration() {
		
		ServiceConfiguration appConf = Configuration.buildConfiguration(Lists.newArrayList());
		Properties ptest = new Properties();
		ptest.put("service1.runner.id", "id1");
		ptest.put("service1.runner.threads", "42");
		ptest.put("service2.runner.id", "id2");
		ptest.put("service2.runner.threads", "48");
		appConf.loadFrom(ptest);
		
		Properties service1conf = appConf.getConfiguration("service1");
		Properties service2conf = appConf.getConfiguration("service2");
		assertNotNull(service1conf.get("runner.id"),"service 1 runner.id is provided");
		assertNotNull(service1conf.get("runner.threads"),"service 1 runner.threads is provided");
		assertEquals(service1conf.get("runner.id"),"id1");
		assertEquals(service1conf.get("runner.threads"),"42");
		assertNotNull(service2conf.get("runner.id"),"service 2 runner.id is provided");
		assertNotNull(service2conf.get("runner.threads"),"service 2 runner.threads is provided");
		assertEquals(service2conf.get("runner.id"),"id2");
		assertEquals(service2conf.get("runner.threads"),"48");
	}

}
