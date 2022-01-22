package org.zlatko.testing.spring.azsptest.services.provider.azure;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.ServiceType;
import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataConsumerGroup;
import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataFetcher;
import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataNode;
import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataTopic;
import org.zlatko.testing.spring.azsptest.services.base.metadata.AbstractBaseMetadataFetcher;
import org.zlatko.testing.spring.azsptest.services.base.metadata.MetadataConsumerGroupDesc;
import org.zlatko.testing.spring.azsptest.services.base.metadata.MetadataTopicDesc;
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

public class EventHubMetadataFetcher extends AbstractBaseMetadataFetcher implements MetadataFetcher {

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

	public EventHubMetadataFetcher(ServiceConfiguration appConfig) {
		super(ServiceType.METADATA_AZURE, appConfig);

		ressourceGroupName = getMandatoryProperty(ConfigurationProperties.CONF_RGNAME);
		String subscriptionId = getMandatoryProperty(ConfigurationProperties.CONF_SUBSCRIPTION_ID);
		String tenantId = getMandatoryProperty(ConfigurationProperties.CONF_TENANT_ID);
		String clientId = getMandatoryProperty(ConfigurationProperties.CONF_CLIENT_ID);
		String secret = getMandatoryProperty(ConfigurationProperties.CONF_CLIENT_SECRET);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(clientId)
				.clientSecret(secret)
				.tenantId(tenantId)
				.build();
		
		eventHubNamespace = getNamespaceFromKafkaProps();
		AzureProfile azureProfile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
		eventHubs = EventHubsManager.authenticate(clientSecretCredential, azureProfile).eventHubs();

	}

	@Override
	public Optional<List<MetadataNode>> getNodesDescriptionDescLines() {
		// there are no nodes on a manages solution such as Azure Event Hubs
		return Optional.empty();
	}

	@Override
	public Optional<List<MetadataTopic>> getTopicsDescriptionDescLines() {
		List<MetadataTopic> topics = Lists.newArrayList();
		eventHubs.listByNamespace(ressourceGroupName, eventHubNamespace).forEach(eh -> {
			topics.add(MetadataTopicDesc.builder(eh.name()).withPartitions(eh.partitionIds().size()));
		});
		return Optional.of(topics);
	}

	@Override
	public Optional<List<MetadataConsumerGroup>> getConsumerGroupDescLines() {
		List<MetadataConsumerGroup> cgs = Lists.newArrayList();
		eventHubs.listByNamespace(ressourceGroupName, eventHubNamespace).stream().forEach(eh -> {
			eh.listConsumerGroups().stream().forEach(cg -> {
				cgs.add(MetadataConsumerGroupDesc.builder(cg.name()).forTopic(eh.name()).withId(cg.id()));
			});
		});
		return Optional.of(cgs);
	}


}
