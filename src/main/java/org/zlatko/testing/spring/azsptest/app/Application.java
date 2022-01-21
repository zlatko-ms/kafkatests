package org.zlatko.testing.spring.azsptest.app;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.zlatko.testing.spring.azsptest.kafka.Kafka;
import org.zlatko.testing.spring.azsptest.util.Configuration;
import org.zlatko.testing.spring.azsptest.util.Configuration.CommandLineParameters;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.collect.Lists;

import lombok.extern.java.Log;

@Log
@SpringBootApplication
public class Application {

	private static final String PARAM_SERVICE = "serviceName";
	private static final String PARAM_CONF = "confPath";

	private static final String LOG_APP_USAGE = "usage is provided as follows:\n\nUsage : java -jar archiveName.jar serviceName configurationFile\nParameters : \n    serviceName       : "+Kafka.getValidServiceTypesAsString("|")+"\n    configurationFile : valid service configuration properties file \n";
	private static final String LOG_ERROR_MISSING_PARAM = "missing mandatory parameter %s";
	private static final String LOG_BEFORE_SERVICE_START = "service is %s, configuration is %s";
	private static final String LOG_ERROR_INVALID_SERVICE ="invalid service %s, valid entries are : %s";
	
	// parse the command line according to expected parameters
	private CommandLineParameters parseParameters(String[] args) {
		CommandLineParameters params = Configuration.buildParameters(PARAM_SERVICE, PARAM_CONF);
		params.parse(args);
		return params;
	}

	// return the test service to run
	private Kafka.TestWorkloadType getServiceModeParameter(CommandLineParameters params) {
		Optional<String> service = params.getParam(PARAM_SERVICE);
		Kafka.TestWorkloadType mode = null;
		if (!service.isEmpty()) {
			try {
				mode = Kafka.TestWorkloadType.valueOf(service.get().toUpperCase());
			} catch (IllegalArgumentException e) {
				log.severe(String.format(LOG_ERROR_INVALID_SERVICE,service.get(),Kafka.getValidServiceTypesAsString()));
			}
		}
		return mode;
	}

	// return the application configuration
	private ServiceConfiguration getConfigurationParameter(CommandLineParameters params) {
		List<String> envVarPrefixes = Lists.newArrayList();
		Kafka.TestWorkloadType[] services = Kafka.TestWorkloadType.values();
		for (Kafka.TestWorkloadType t : services ) {
			envVarPrefixes.add(t.name().toUpperCase());
		}
		
		Optional<String> confFile = params.getParam(PARAM_CONF);
		ServiceConfiguration appConf = null;
		if (!confFile.isEmpty()) {
			File propFile = new File(confFile.get());
			if (propFile.exists()) {
				appConf = Configuration.buildConfiguration(envVarPrefixes);
				appConf.loadFrom(confFile.get());
			}
		}
		return appConf;
	}
	
	private void errorMissingParameter(String paramName) {
		log.severe(String.format(LOG_ERROR_MISSING_PARAM, paramName));
		log.info(LOG_APP_USAGE);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			CommandLineParameters appParameters = parseParameters(args);
			Kafka.TestWorkloadType testServiceType = getServiceModeParameter(appParameters);
			ServiceConfiguration appConfiguration = getConfigurationParameter(appParameters);

			if (testServiceType == null) {
				errorMissingParameter(PARAM_SERVICE);
				return;
			}

			if (appConfiguration == null) {
				errorMissingParameter(PARAM_CONF);
				return;
			}
			
			log.fine(String.format(LOG_BEFORE_SERVICE_START, testServiceType.name().toLowerCase(),appConfiguration.getLoadedConfigurationFilePath()));

			Kafka.KafkaTestService kafkaService = Kafka.buildTestService(testServiceType, appConfiguration);

			if (kafkaService != null)
				kafkaService.run();

		};
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
