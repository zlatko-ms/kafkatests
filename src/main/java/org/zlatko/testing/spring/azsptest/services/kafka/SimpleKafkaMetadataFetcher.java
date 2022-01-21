package org.zlatko.testing.spring.azsptest.services.kafka;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.zlatko.testing.spring.azsptest.services.AbstractBaseMetadataFetcher;
import org.zlatko.testing.spring.azsptest.services.Services.MetadataTestService;
import org.zlatko.testing.spring.azsptest.services.Services.ServiceType;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
public class SimpleKafkaMetadataFetcher extends AbstractBaseMetadataFetcher implements MetadataTestService {

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
	
	public SimpleKafkaMetadataFetcher(ServiceConfiguration appConfig) {
		super(ServiceType.METADATA_KAFKA,appConfig);
		describeTopicsPrivate = Boolean.parseBoolean(getServiceProperties().getProperty(ConfigurationProperties.CONF_DESCRIBE_INTERNAL_TOPICS, "false"));
		describeTopicList.addAll(getTopicsToDescribe());
		adminClient = AdminClient.create(getKafkaProperties());
	}

	@Override
	public Optional<List<String>> getNodesDescriptionDescLines() {
		List<String> nodes = Lists.newArrayList();
		try {
			DescribeClusterResult clusterDesc = adminClient.describeCluster();
			clusterDesc.nodes().get().forEach(node -> {
				nodes.add(String.format("host=%s id=%s rack=%s, port=%s", 
						node.host(), 
						node.idString(),
						(node.hasRack() ? node.rack() : "none"),
						node.port()));
			});

		} catch (Throwable e) {
			log.severe(("unable to fetch cluster node metadata, cause=" + e.getMessage()));
		}
		return Optional.of(nodes);
	}

	@Override
	public Optional<List<String>> getTopicsDescriptionDescLines() {
		List<String> topicsDesc = Lists.newArrayList();

		try {

			if (describeTopicList.isEmpty()) {
				describeTopicList.addAll(fetchAllTopicNames());
			}
			//log.info(String.format("Fetching %d topic list metadata", describeTopicList.size()));
			Map<String, TopicDescription> topics = adminClient.describeTopics(describeTopicList).all().get();
			//log.info(String.format("Fetched %d topic list metadata", topics.size()));

			topics.values().forEach(topic -> {

				StringBuilder sb = new StringBuilder("name=" + topic.name());
				sb.append(" partitions=" + topic.partitions().size());
				sb.append(" internal=" + topic.isInternal());
				sb.append(" id=" + topic.topicId().toString());
				topic.partitions().forEach(partition -> {
					sb.append(" partition_leader=" + partition.leader().host());
					sb.append(" replicas=" + partition.replicas().size());
				});
				topicsDesc.add(sb.toString());
			});

		} catch (Throwable e) {
			log.info(String.format("Error while fetching topic description " + e.getMessage()));
		}

		return Optional.of(topicsDesc);
	}

	@Override
	
	public Optional<List<String>> getConsumerGroupDescLines() {
		List<String> cgDescs = Lists.newArrayList();
		try {
			Collection<ConsumerGroupListing> consumerGroupList = adminClient.listConsumerGroups().all().get();
			consumerGroupList.forEach(cg -> {
				cgDescs.add(String.format("name=%s isSimple=%b", cg.groupId(), cg.isSimpleConsumerGroup()));
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