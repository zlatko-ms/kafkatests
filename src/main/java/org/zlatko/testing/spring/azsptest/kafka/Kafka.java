package org.zlatko.testing.spring.azsptest.kafka;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
public class Kafka {

	/** defines the types of test workloads */
	public enum TestWorkloadType {
		PRODUCER, CONSUMER, METADATA
	}

	/** defines the kafka test service interface */
	public interface KafkaTestService {

		// returns the name of the service
		public String getName();

		// returns the service workload type
		public TestWorkloadType getServiceType();

		// runs the service
		public void run();

		// returns general Kafka level properties (for joining the cluster)
		public Properties getKafkaProperties();

		// returns the specific service workload properties
		public Properties getServiceProperties();
	}

	/** simple kafka message abstraction */
	public interface KafkaTestMessage {
		String getKey();

		Object getValue();

		String getValueAsJson();
	}

	/** builds the kafka service to test */
	public static final KafkaTestService buildTestService(TestWorkloadType type, ServiceConfiguration appConf) {
		switch (type) {
		case PRODUCER:
			return new SimpleKafkaProducer(appConf);
		case CONSUMER:
			return new SimpleKafkaConsumer(appConf);
		case METADATA:
			return new AdminKafkaService(appConf);
		}
		throw new IllegalArgumentException(type.name() + " is not supported yet");
	}

	public static String getValidServiceTypesAsString(String separator) {
		List<String> validServices = Lists.newArrayList();
		for (TestWorkloadType type : TestWorkloadType.values()) {
			validServices.add(type.name().toLowerCase());
		}
		return String.join(separator, validServices);
	}

	public static String getValidServiceTypesAsString() {
		return getValidServiceTypesAsString(",");
	}

	final static class AdminKafkaService extends BaseKafkaService implements KafkaTestService {

		private final class ConfigurationProperties {
			static final String CONF_DESCRIBE_CLUSTER = "describe.cluster";
			static final String CONF_DESCRIBE_TOPICS = "describe.topics";
			static final String CONF_DESCRIBE_INTERNAL_TOPICS = "describe.internal.topics";
			static final String CONF_TOPICS_TO_DESCRIBE = "describe.topics.list";
		}

		private AdminClient adminClient;
		private boolean describeTopicsPrivate = true;
		private boolean describeClusterNodes = false;
		private boolean describeTopics = true;
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
		
		public AdminKafkaService(ServiceConfiguration appConfig) {
			super(TestWorkloadType.METADATA,appConfig);
			describeClusterNodes = Boolean.parseBoolean(getServiceProperties().getProperty(ConfigurationProperties.CONF_DESCRIBE_CLUSTER, "false"));
			describeTopics = Boolean.parseBoolean(getServiceProperties().getProperty(ConfigurationProperties.CONF_DESCRIBE_TOPICS, "true"));
			describeTopicsPrivate = Boolean.parseBoolean(getServiceProperties().getProperty(ConfigurationProperties.CONF_DESCRIBE_INTERNAL_TOPICS, "false"));
			describeTopicList.addAll(getTopicsToDescribe());
			adminClient = AdminClient.create(getKafkaProperties());
		}

		private void describeClusterNodes() {
			try {
			if (describeClusterNodes) {
				log.info(LogMessageConstants.MESSAGE_METADATA_FETCHING_NODES);
				DescribeClusterResult clusterDesc = adminClient.describeCluster();
				clusterDesc.nodes().get().forEach(node -> {
					log.info(String.format("[*] node : host=%s id=%s rack=%s, port=%s", 
							node.host(), 
							node.idString(),
							(node.hasRack() ? node.rack() : "none"),
							node.port()));
				});
			}
			} catch(Throwable e) {
				log.severe(("unable to fetch cluster node metadata, cause="+e.getMessage()));;
			}
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

		private void describeTopics() {
			try {
				if (describeTopics) {
					if (describeTopicList.isEmpty()) {
						describeTopicList.addAll(fetchAllTopicNames());
					}
					log.info(String.format("Fetching %d topic list metadata", describeTopicList.size()));
					Map<String, TopicDescription> topics = adminClient.describeTopics(describeTopicList).all().get();
					log.info(String.format("Fetched %d topic list metadata", topics.size()));

					topics.values().forEach(topic -> {

						StringBuilder sb = new StringBuilder("[*] topic name=" + topic.name());
						sb.append(" partitions=" + topic.partitions().size());
						sb.append(" internal=" + topic.isInternal());
						sb.append(" id=" + topic.topicId().toString());
						topic.partitions().forEach(partition -> {
							sb.append(" partition_leader="+ partition.leader().host());
							sb.append(" replicas="+partition.replicas().size());
						});
						log.info(sb.toString());
					});

				}
			} catch (Throwable e) {
				log.info(String.format("Error while fetching topic description " + e.getMessage()));
			}
		}

		@Override
		public void run() {
			describeClusterNodes();
			describeTopics();
		}

	}

}
