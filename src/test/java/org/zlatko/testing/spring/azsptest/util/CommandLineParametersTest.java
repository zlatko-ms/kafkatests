package org.zlatko.testing.spring.azsptest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.zlatko.testing.spring.azsptest.util.Configuration.CommandLineParameters;

class CommandLineParametersTest {

	@Test
	void testProvidedParameters() {
		
		CommandLineParameters appParameters = Configuration.buildParameters("service","conf");
		String[] cmdlineParamsOne = { "serviceName" , "confFilePath" };
		appParameters.parse(cmdlineParamsOne);
		
		Optional<String> paramService = appParameters.getParam("service");
		assertFalse(paramService.isEmpty(),"service param is provided");
		assertEquals(paramService.get(),"serviceName");
		
		Optional<String> paramConfig = appParameters.getParam("conf");
		assertFalse(paramConfig.isEmpty(),"config param is provided");
		assertEquals(paramConfig.get(),"confFilePath");
	}
	
	
	@Test
	void testNonProvidedParameters() {
		CommandLineParameters appParameters = Configuration.buildParameters("service","conf","whatever");
		String[] cmdlineParamsOne = { "serviceName" };
		appParameters.parse(cmdlineParamsOne);
		
		Optional<String> paramService = appParameters.getParam("service");
		assertFalse(paramService.isEmpty(),"service param is provided");
		assertEquals(paramService.get(),"serviceName");
		
		Optional<String> paramConfig = appParameters.getParam("conf");
		assertTrue(paramConfig.isEmpty());
		
		Optional<String> paramWhatever= appParameters.getParam("whatever");
		assertTrue(paramWhatever.isEmpty());
	}

}
