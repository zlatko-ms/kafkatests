package org.zlatko.testing.spring.azsptest.providers.azure.impl;

import java.util.List;
import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.Metadata;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.metadata.AbstractMetadataFetcherService;
import org.zlatko.testing.spring.azsptest.services.base.metadata.ConsumerGroupDesc;
import org.zlatko.testing.spring.azsptest.services.base.metadata.TopicDesc;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.models.EventHubs;
import com.google.common.collect.Lists;

public final class EventHubMetadataService extends AbstractMetadataFetcherService implements Metadata.FetcherService {

	private String ressourceGroupName;
	private String eventHubNamespace;
	private EventHubs eventHubs;

	public EventHubMetadataService(ServiceConfiguration appConfig) {
		super(Service.ServiceType.METADATA, appConfig);

		ressourceGroupName = getMandatoryProperty(ConfConstants.CONF_PREFIX,ConfConstants.CONF_RGNAME);
		String subscriptionId = getMandatoryProperty(ConfConstants.CONF_PREFIX,ConfConstants.CONF_SPN_SUBSCRIPTION_ID);
		String tenantId = getMandatoryProperty(ConfConstants.CONF_PREFIX,ConfConstants.CONF_SPN_TENANT_ID);
		String clientId = getMandatoryProperty(ConfConstants.CONF_PREFIX,ConfConstants.CONF_SPN_CLIENT_ID);
		String secret = getMandatoryProperty(ConfConstants.CONF_PREFIX,ConfConstants.CONF_SPN_CLIENT_SECRET);
		ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
				.clientId(clientId)
				.clientSecret(secret)
				.tenantId(tenantId)
				.build();
		
		eventHubNamespace =getMandatoryProperty(ConfConstants.CONF_PREFIX,ConfConstants.CONF_NAMESPACE);
		AzureProfile azureProfile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
		eventHubs = EventHubsManager.authenticate(clientSecretCredential, azureProfile).eventHubs();

	}

	@Override
	public Optional<List<Metadata.Node>> getNodesDescriptionDescLines() {
		// there are no nodes on a manages solution such as Azure Event Hubs
		return Optional.empty();
	}

	@Override
	public Optional<List<Metadata.Topic>> getTopicsDescriptionDescLines() {
		List<Metadata.Topic> topics = Lists.newArrayList();
		eventHubs.listByNamespace(ressourceGroupName, eventHubNamespace).forEach(eh -> {
			topics.add(TopicDesc.builder(eh.name()).withPartitions(eh.partitionIds().size()));
		});
		return Optional.of(topics);
	}

	@Override
	public Optional<List<Metadata.ConsumerGroup>> getConsumerGroupDescLines() {
		List<Metadata.ConsumerGroup> cgs = Lists.newArrayList();
		eventHubs.listByNamespace(ressourceGroupName, eventHubNamespace).stream().forEach(eh -> {
			eh.listConsumerGroups().stream().forEach(cg -> {
				cgs.add(ConsumerGroupDesc.builder(cg.name()).forTopic(eh.name()).withId(cg.id()));
			});
		});
		return Optional.of(cgs);
	}


}
