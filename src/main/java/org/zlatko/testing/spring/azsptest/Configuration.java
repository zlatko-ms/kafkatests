package org.zlatko.testing.spring.azsptest;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
public class Configuration {

	/** contains all properties for the distinct test services */
	public interface ServiceConfiguration {

		void loadFrom(Properties p);

		void loadFrom(String filePath);

		Properties getServiceConfiguration(String serviceName);

		String getLoadedConfigurationFilePath();
	}

	/** abstracts the command line parameters */
	public interface CommandLineParameters {

		void parse(String[] cmdLineParams);

		Optional<String> getParam(String name);

	}

	/** builds a CommandLineParameters implementation */
	public static final CommandLineParameters buildParameters(String... parameters) {
		return new CLIParamsImpl(parameters);
	}

	/** builds a service configuration implementation */
	public static final ServiceConfiguration buildConfiguration() {
		return new ServiceConfigurationImpl();

	}

	/** provides the CLI parameter parsing implementation */
	static class CLIParamsImpl implements CommandLineParameters {

		private String[] paramNames;
		private Map<String, Optional<String>> paramHash = new HashMap<String, Optional<String>>();

		public CLIParamsImpl(String... parameters) {
			paramNames = Arrays.copyOf(parameters, parameters.length);
		}

		@Override
		public void parse(String[] cmdLineParams) {
			int index = 0;
			while (index < paramNames.length) {
				String paramName = paramNames[index];
				if (cmdLineParams.length > index) {
					Optional<String> value = Optional.empty();
					String svalue = cmdLineParams[index];
					if (!com.google.common.base.Strings.isNullOrEmpty(svalue)) {
						value = Optional.of(svalue);
					}
					paramHash.put(paramName, value);
				}
				index++;
			}
		}

		@Override
		public Optional<String> getParam(String name) {
			Optional<String> paramValue = paramHash.get(name);
			return (paramValue != null) ? paramValue : Optional.empty();
		}
	}

	/** provides the service configuration implementation */
	static final class ServiceConfigurationImpl implements ServiceConfiguration {

		private static final String PROPERTY_SEPARATOR = ".";
		private static final String ENVVAR_SEPARATOR = "_";

		private Properties loadedProperties;
		private String loadedConfFilePath = null;

		public ServiceConfigurationImpl() {
			loadedProperties = new Properties();
		}

		private void overrideWithEnvProvidedConfiguration() {

			ArrayList<String> envVarList = Lists.newArrayList(
					"KAFKA_BOOTSTRAP_SERVERS", 
					"KAFKA_SECURITY_PROTOCOL", 
					"KAFKA_SASL_MECHANISM",
					"KAFKA_SASL_JAAS_CONFIG");
			
			for (String envVar : envVarList) {
				String propertyName = envVar.toLowerCase().replace(ENVVAR_SEPARATOR, PROPERTY_SEPARATOR);
				String propertyValue = System.getenv(envVar);
				if (!Strings.isNullOrEmpty(propertyValue)) {
					System.out.println("ZDBG OVERRIDING PROP BY ENV  "+propertyName+"="+propertyValue);
					loadedProperties.put(propertyName, propertyValue);
				}
			}
		}

		@Override
		public void loadFrom(Properties p) {
			loadedProperties.putAll(p);
			overrideWithEnvProvidedConfiguration();
		}

		@Override
		@SneakyThrows
		public void loadFrom(String filePath) {

			File confFile = Path.of(filePath).toFile();
			if (confFile.exists()) {
				try (FileInputStream fis = new FileInputStream(confFile)) {
					loadedProperties.load(fis);
					loadedConfFilePath = filePath;
				}
				overrideWithEnvProvidedConfiguration();
			} else {
				String message = "unable to read configuration file " + filePath;
				log.severe(message);
				throw new RuntimeException(message);
			}
		}

		@Override
		public Properties getServiceConfiguration(String serviceName) {
			Properties ret = new Properties();
			String tagService = serviceName.toLowerCase();
			loadedProperties.keySet().forEach(key -> {
				String sKey = key.toString().toLowerCase();
				if (sKey.startsWith(tagService)) {
					String serviceKey = sKey.replace(serviceName + PROPERTY_SEPARATOR, "");
					ret.put(serviceKey, loadedProperties.getProperty(sKey));
				}
			});
			return ret;
		}

		@Override
		public String getLoadedConfigurationFilePath() {
			return loadedConfFilePath;
		}

	}

}
