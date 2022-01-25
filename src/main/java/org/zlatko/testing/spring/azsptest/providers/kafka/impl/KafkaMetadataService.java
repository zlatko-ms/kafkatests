package org.zlatko.testing.spring.azsptest.providers.kafka.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.TopicPartitionInfo;
import org.zlatko.testing.spring.azsptest.services.api.Metadata;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.metadata.AbstractMetadataFetcherService;
import org.zlatko.testing.spring.azsptest.services.base.metadata.ConsumerGroupDesc;
import org.zlatko.testing.spring.azsptest.services.base.metadata.NodeDesc;
import org.zlatko.testing.spring.azsptest.services.base.metadata.TopicDesc;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
public class KafkaMetadataService extends AbstractMetadataFetcherService implements Metadata.FetcherService {

	private final class ConfigurationProperties {
		static final String CONF_DESCRIBE_INTERNAL_TOPICS = "describe.internal.topics";
		static final String CONF_TOPICS_TO_DESCRIBE = "describe.topics.list";
	}

	private AdminClient adminClient;
	private boolean describeTopicsPrivate = true;
	private List<String> describeTopicList = Lists.newArrayList();

	// parse list of topics to describe
	private List<String> getTopicsToDescribe() {
		List<String> ret = Lists.newArrayList();
		String listOfTopicsFromConf = getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPICS_TO_DESCRIBE, "");
		if (!Strings.isNullOrEmpty(listOfTopicsFromConf)) {
			Iterable<String> names = Splitter.on(",").split(listOfTopicsFromConf);
			Iterators.addAll(ret,names.iterator());
		}
		return ret;
	}
	
	public KafkaMetadataService(ServiceConfiguration appConfig) {
		super(Service.ServiceType.METADATA,appConfig);
		describeTopicsPrivate = Boolean.parseBoolean(getServiceProperties().getProperty(ConfigurationProperties.CONF_DESCRIBE_INTERNAL_TOPICS, "false"));
		describeTopicList.addAll(getTopicsToDescribe());
		Properties kafkaProperties = appConfig.getConfiguration(ConfConstants.KAFKA_SHARED_SERVICE);
		adminClient = AdminClient.create(kafkaProperties);
	}
	
	@Override
	public Optional<List<Metadata.Node>> getNodesDescriptionDescLines() {
		List<Metadata.Node> nodes = Lists.newArrayList();
		try {
			DescribeClusterResult clusterDesc = adminClient.describeCluster();
			clusterDesc.nodes().get().forEach(node -> {
				
				nodes.add(NodeDesc.builder(node.host())
					.withId(node.idString())
					.withPort(node.port())
					.withRack(node.hasRack() ? node.rack() : null));
			});

		} catch (Throwable e) {
			log.severe(("unable to fetch cluster node metadata, cause=" + e.getMessage()));
		}
		return Optional.of(nodes);
	}

	@Override
	public Optional<List<Metadata.Topic>> getTopicsDescriptionDescLines() {
		List<Metadata.Topic> topicsDesc = Lists.newArrayList();

		try {

			if (describeTopicList.isEmpty()) {
				describeTopicList.addAll(fetchAllTopicNames());
			}

			Map<String, TopicDescription> topics = adminClient.describeTopics(describeTopicList).all().get();
			topics.values().forEach(topic -> {

				
				int replicaCount = 0;
				List<TopicPartitionInfo> partitions = topic.partitions();
				for (TopicPartitionInfo tpi : partitions) {
					replicaCount+=tpi.replicas().size();
				}
					
				topicsDesc.add(TopicDesc.builder(topic.name())
					.withPartitions(topic.partitions().size())
					.withReplication(replicaCount)
					.withId(topic.topicId().toString())
					.asInternal(topic.isInternal()));
			});

		} catch (Throwable e) {
			log.info(String.format("Error while fetching topic description " + e.getMessage()));
		}

		return Optional.of(topicsDesc);
	}

	@Override
	
	public Optional<List<Metadata.ConsumerGroup>> getConsumerGroupDescLines() {
		List<Metadata.ConsumerGroup> cgDescs = Lists.newArrayList();
		try {
			Collection<ConsumerGroupListing> consumerGroupList = adminClient.listConsumerGroups().all().get();
			consumerGroupList.forEach(cg -> {
				cgDescs.add(ConsumerGroupDesc.builder(cg.groupId()).asSimple(cg.isSimpleConsumerGroup()));
			});
		} catch (Throwable e) {
			log.info(String.format("Error while fetching consumer groups description " + e.getMessage()));
		}
		return Optional.of(cgDescs);
	}

	@SneakyThrows
	private List<String> fetchAllTopicNames() {
		List<String> res = Lists.newArrayList();
		log.info("Listing available topics");
		Collection<TopicListing> topics = adminClient.listTopics().listings().get();
		topics.forEach( topic -> {
			if ((describeTopicsPrivate && topic.isInternal()) || (!topic.isInternal()))
				res.add(topic.name());
		});
		log.info(String.format("Listed %d topics from the cluster",res.size()));
		return res;
	}


}