package org.zlatko.testing.spring.azsptest.app;

public class LogMessages {
	
	static final String LOG_ERROR_MISSING_PARAM(String param) {
		return String.format("missing mandatory parameter %s", param);
	}
	
	static final String LOG_BEFORE_SERVICE_START(String service,String provider,String confPath) {
		return String.format("starting service %s on provider %s with configuration file %s",service,provider,confPath);
	}
		
	static final String LOG_ERROR_INVALID_SERVICE(String service,String validEntries) {
		return String.format("invalid service %s, valid entries are : %s",service,validEntries);
	}
	static final String LOG_ERROR_INVALID_PROVIDER(String provider,String validEntries) {
		return String.format("invalid provider %s, valid entries are : %s",provider,validEntries);
	}
	static final String LOG_USAGE(String serviceValues,String providerValues) {
		return String.format("\n"+
	                              "Usage  : java -jar archive.jar service provider confFilePath\n"+
								  "Params :\n"+
								  " service      : service to run, possible values = %s\n"+
								  " provider     : infra provider to use, possible values = %s\n"+
								  " confFilePath : path to a valid configuration file\n",serviceValues,providerValues);
	}
}
