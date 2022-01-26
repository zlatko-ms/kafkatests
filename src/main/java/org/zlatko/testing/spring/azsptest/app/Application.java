package org.zlatko.testing.spring.azsptest.app;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.zlatko.testing.spring.azsptest.providers.azure.AzureServiceFactory;
import org.zlatko.testing.spring.azsptest.providers.kafka.KafkaServiceFactory;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.api.Service.ConfigurableService;
import org.zlatko.testing.spring.azsptest.services.api.Service.Provider;
import org.zlatko.testing.spring.azsptest.util.Configuration;
import org.zlatko.testing.spring.azsptest.util.Configuration.CommandLineParameters;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
@SpringBootApplication
public class Application {

	@SuppressWarnings("serial")
	class ConfException extends Throwable {
		ConfException(String message) {
			super(message);
		}
	}

	private Service.ServiceProvider azureFactory = new AzureServiceFactory();
	private Service.ServiceProvider kafkaFactory = new KafkaServiceFactory();

	private CommandLineParameters parseParameters(String[] args) {
		CommandLineParameters params = Configuration.buildParameters(ConfParams.PARAM_SERVICE,
				ConfParams.PARAM_PROVIDER, ConfParams.PARAM_CONF);
		params.parse(args);
		return params;
	}

	private Service.ServiceType getServiceParameter(CommandLineParameters params) throws ConfException {
		Optional<String> param = params.getParam(ConfParams.PARAM_SERVICE);
		Service.ServiceType mode = null;
		if (!param.isEmpty()) {
			try {
				mode = Service.ServiceType.valueOf(param.get().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new ConfException(LogMessages.LOG_ERROR_INVALID_SERVICE(param.get(), getValidServices()));
			}
		} else {
			throw new ConfException(LogMessages.LOG_ERROR_MISSING_PARAM(ConfParams.PARAM_SERVICE));
		}
		return mode;
	}

	@SneakyThrows
	private Service.Provider getProviderParameter(CommandLineParameters params) throws ConfException {
		Optional<String> param = params.getParam(ConfParams.PARAM_PROVIDER);
		Service.Provider provider = null;
		if (!param.isEmpty()) {
			try {
				provider = Service.Provider.valueOf(param.get().toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new ConfException(LogMessages.LOG_ERROR_INVALID_PROVIDER(param.get(), getValidProviders()));
			}
		} else {
			throw new ConfException(LogMessages.LOG_ERROR_MISSING_PARAM(ConfParams.PARAM_PROVIDER));
		}
		return provider;
	}

	@SneakyThrows
	private ServiceConfiguration getConfiguration(CommandLineParameters params) {

		List<String> envVarPrefixes = Lists.newArrayList();

		Service.ServiceType[] services = Service.ServiceType.values();
		for (Service.ServiceType service : services) {
			envVarPrefixes.add(service.name().toUpperCase());
		}

		Service.Provider[] providers = Service.Provider.values();
		for (Service.Provider provider : providers) {
			envVarPrefixes.add(provider.name().toUpperCase());
		}

		Optional<String> confFile = params.getParam(ConfParams.PARAM_CONF);
		ServiceConfiguration appConf = null;
		if (confFile.isEmpty())
			throw new ConfException(LogMessages.LOG_ERROR_MISSING_PARAM(ConfParams.PARAM_CONF));
		File propFile = new File(confFile.get());
		if (propFile.exists()) {
			appConf = Configuration.buildConfiguration(envVarPrefixes);
			appConf.loadFrom(confFile.get());
		}
		return appConf;
	}

	private static final String getValidProviders() {
		Service.Provider[] values = Service.Provider.values();
		List<String> svalues = Lists.newArrayList();
		for (Service.Provider p : values) {
			svalues.add(p.name().toLowerCase());
		}
		return Joiner.on("|").join(svalues);
	}

	private static final String getValidServices() {
		Service.ServiceType[] values = Service.ServiceType.values();
		List<String> svalues = Lists.newArrayList();
		for (Service.ServiceType p : values) {
			svalues.add(p.name().toLowerCase());
		}
		return Joiner.on("|").join(svalues);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			try {
				CommandLineParameters params = parseParameters(args);
				Service.ServiceType service = getServiceParameter(params);
				Service.Provider provider = getProviderParameter(params);
				ServiceConfiguration conf = getConfiguration(params);

				log.info(LogMessages.LOG_BEFORE_SERVICE_START(service.name().toLowerCase(),
						provider.name().toLowerCase(), conf.getLoadedConfigurationFilePath()));

				ConfigurableService providerService = provider.equals(Provider.KAFKA)
						? kafkaFactory.getService(service, conf)
						: azureFactory.getService(service, conf);
				providerService.run();

			} catch (ConfException e) {
				log.severe("configuration error : " + e.getMessage());
				log.info(LogMessages.LOG_USAGE(getValidServices(), getValidProviders()));
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
