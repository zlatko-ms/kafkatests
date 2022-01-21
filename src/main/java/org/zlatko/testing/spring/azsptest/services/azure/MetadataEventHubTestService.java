package org.zlatko.testing.spring.azsptest.services.azure;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.AbstractBaseMetadataFetcher;
import org.zlatko.testing.spring.azsptest.services.Services.MetadataTestService;
import org.zlatko.testing.spring.azsptest.services.Services.ServiceType;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.models.EventHubs;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import lombok.extern.java.Log;

@Log
public class MetadataEventHubTestService extends AbstractBaseMetadataFetcher implements MetadataTestService {

	private static class ConfigurationProperties {
		static final String CONF_CLIENT_ID = "auth.client.id";
		static final String CONF_CLIENT_SECRET = "auth.client.secret";
		static final String CONF_TENANT_ID = "auth.tenant.id";
		static final String CONF_SUBSCRIPTION_ID = "auth.subscription.id";
		static final String CONF_RGNAME = "rg.name";
	}

	private String ressourceGroupName;
	private String eventHubNamespace;
	private EventHubs eventHubs;

	private String getMandatoryProperty(String propName) {
		String val = getServiceProperties().getProperty(propName, null);
		if (Strings.isNullOrEmpty(val)) {
			String msg = String.format("Missing mandatory configuration parmeter %s.%s",
					getServiceType().name().toLowerCase(), propName);
			throw new RuntimeException(msg);
		}
		return val;
	}

	private String getNamespaceFromKafkaProps() {
		String bootstrapVal = getKafkaProperties().getProperty("bootstrap.servers", null);
		if (Strings.isNullOrEmpty(bootstrapVal)) {
			throw new RuntimeException(
					String.format("Missing mandatory configuration parmeter kafka.bootstrap.servers"));
		}
		Iterator<String> tokens = Splitter.on(".").split(bootstrapVal).iterator();
		if (tokens.hasNext()) {
			String name = tokens.next();
			return name;
		} else {
			throw new RuntimeException(
					String.format("Malformed mandatory configuration parmeter kafka.bootstrap.servers"));
		}
	}

	public MetadataEventHubTestService(ServiceConfiguration appConfig) {
		super(ServiceType.METADATA_AZURE, appConfig);

		ressourceGroupName = getMandatoryProperty(ConfigurationProperties.CONF_RGNAME);
		String subscriptionId = getMandatoryProperty(ConfigurationProperties.CONF_SUBSCRIPTION_ID);
		String tenantId = getMandatoryProperty(ConfigurationProperties.CONF_TENANT_ID);
		String clientId = getMandatoryProperty(ConfigurationProperties.CONF_CLIENT_ID);
		String secret = getMandatoryProperty(ConfigurationProperties.CONF_CLIENT_SECRET);
		eventHubNamespace = getNamespaceFromKafkaProps();

		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder().clientId(clientId)
				.clientSecret(secret).tenantId(tenantId).build();
		AzureProfile azureProfile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);

		eventHubs = EventHubsManager.authenticate(clientSecretCredential, azureProfile).eventHubs();
	}

	@Override
	public Optional<List<String>> getNodesDescriptionDescLines() {
		// there are no nodes on a cloud managed service
		return Optional.empty();
	}

	@Override
	public Optional<List<String>> getTopicsDescriptionDescLines() {
		List<String> topics = Lists.newArrayList();
		eventHubs.listByNamespace(ressourceGroupName, eventHubNamespace).forEach(eh -> {
			topics.add(String.format("name=%s partitions=%d", eh.name(), eh.partitionIds().size()));
		});
		return Optional.of(topics);
	}

	@Override
	public Optional<List<String>> getConsumerGroupDescLines() {
		List<String> cgs = Lists.newArrayList();
		eventHubs.listByNamespace(ressourceGroupName, eventHubNamespace).stream().forEach(eh -> {
			eh.listConsumerGroups().stream().forEach(cg -> {
				cgs.add(String.format("name=%s", cg.name()));
			});
		});
		return Optional.of(cgs);
	}

}
