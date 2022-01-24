package org.zlatko.testing.spring.azsptest.services.provider.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.zlatko.testing.spring.azsptest.services.api.PubSub;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.pubsub.AbstractProducerService;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.base.Joiner;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

/** simple kafka producer test service */
@Log
public class SimpleKafkaProducer extends AbstractProducerService  {

	private final class ConfigurationProperties {
		static final String CONF_BATCH_SIZE = "messages.per.batch";
		static final String CONF_WAIT_AFTER_BATCH = "wait.after.batch.ms";
		static final String CONF_MAX_MESSAGES = "messages.max";
		static final String CONF_MESSAGE_SIZE= "messages.size";
		static final String CONF_TOPIC_NAME = "topic.name";
		static final String CONF_TOPIC_CREATE_PARTITIONS = "topic.create.partitions";
		static final String CONF_TOPIC_CREATE_REPLICAS = "topics.create.replication";
		static final String CONF_PRODUCER_REPLICATION = "topic.partitioner";
	}
	
	private final class LogMessages {
		public static final String MESSAGE_TOPIC_CREATING = "creating topic %s";
		public static final String MESSAGE_TOPIC_EXISTS = "topic %s already exists";
		public static final String MESSAGE_TOPIC_CREATED = "topic %s created, partitions=%d replicas=%d";
		public static final String MESSAGE_TOPIC_CHECKING = "checking existance of topic %s";	
	}

	enum PartitionnerType {
		UNIQUE, EVEN_ODD, EVEN_ODD_PRIME
	}

	private KafkaProducer<String, String> producer;
	private AdminClient adminClient;
	private int messagesPerBatch;
	private long waitAfterBatchMs;
	
	private String topicName;
	private long messageLimit;
	private int topicsCreatePartitions;
	private short topicsCreateReplication;
	private String partitionerClassName;
	private PubSub.EventSize messageSize;

	@Nullable
	private String getPartitionnerClassName(String paritionerSetting) {
		try {
			PartitionnerType type = PartitionnerType.valueOf(paritionerSetting.toUpperCase());
			switch (type) {
			case EVEN_ODD:
				return EvenOddPartitioner.class.getName();
			case EVEN_ODD_PRIME:
				return EvenOddPrimePartioner.class.getName();
			default:
				return null;
			}

		} catch (Throwable t) {
			log.warning(String.format(
					"Unable to determine the partitionner strategy from value %s, falling back to unique, possible values are %s",
					paritionerSetting, Joiner.on(",").join(PartitionnerType.values())));
			return null;
		}
	}

	public SimpleKafkaProducer(ServiceConfiguration configuration) {

		super(Service.ServiceType.PRODUCER, configuration);

		messagesPerBatch = Integer.parseInt(getServiceProperties().getProperty(ConfigurationProperties.CONF_BATCH_SIZE, "1"));
		waitAfterBatchMs = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_WAIT_AFTER_BATCH, "5000"));
		topicName = getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_NAME, "");
		messageLimit = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_MAX_MESSAGES, "-1"));

		topicsCreatePartitions = Integer.parseInt(getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_CREATE_PARTITIONS, "1"));
		topicsCreateReplication = Short.parseShort(getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_CREATE_REPLICAS, "1"));
		partitionerClassName = getPartitionnerClassName(getServiceProperties().getProperty(ConfigurationProperties.CONF_PRODUCER_REPLICATION, "UNIQUE"));
		messageSize = PubSub.EventSize.valueOf(getServiceProperties().getProperty(ConfigurationProperties.CONF_MESSAGE_SIZE, "M").toUpperCase());
		adminClient = AdminClient.create(getKafkaProperties());
		addSpecificKafkaProp(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		addSpecificKafkaProp(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		if (partitionerClassName != null) {
			addSpecificKafkaProp(ProducerConfig.PARTITIONER_CLASS_CONFIG, partitionerClassName);
			log.info("Using partitioner class " + partitionerClassName);
		}

		
		
		producer = new KafkaProducer<String, String>(getKafkaProperties());
	}

	@SneakyThrows
	private void createTopic() {
		log.info(String.format(LogMessages.MESSAGE_TOPIC_CREATING, topicName));
		List<NewTopic> topicList = new ArrayList<NewTopic>();
		Map<String, String> configs = new HashMap<String, String>();
		NewTopic newTopic = new NewTopic(topicName, topicsCreatePartitions, topicsCreateReplication).configs(configs);
		topicList.add(newTopic);
		adminClient.createTopics(topicList);
		log.info(String.format(LogMessages.MESSAGE_TOPIC_CREATED, topicName, topicsCreatePartitions,
				topicsCreateReplication));
	}

	@SneakyThrows
	@Override
	public void ensureTopicCreated() {
		log.info(String.format(LogMessages.MESSAGE_TOPIC_CHECKING, topicName));
		ListTopicsResult listTopics = adminClient.listTopics();
		Set<String> names = listTopics.names().get();
		if (names.contains(topicName)) {
			log.info(String.format(LogMessages.MESSAGE_TOPIC_EXISTS, topicName));
		} else {
			createTopic();
		}
	}

	@Override
	public Optional<Long> getMaxMessagesToProduce() {
		if (messageLimit>0)
			return Optional.of(messageLimit);
		return Optional.empty();
	}

	@Override
	public int getMessageBatchSize() {
		return messagesPerBatch;
	}

	@Override
	public long getPostBatchWaitTimeMs() {
		return waitAfterBatchMs;
	}

	@Override
	public String getTopicName() {
		return topicName;
	}

	@Override
	@SneakyThrows
	public long sendEvents(List<PubSub.Event> messages) {
		long resultingOffset = -1;
		try {
			for (PubSub.Event msg : messages) {
				final RecordMetadata rm = producer.send(
						new ProducerRecord<String, String>(topicName, msg.getKey().toString(), msg.getValueAsJson()))
						.get();
				resultingOffset = rm.offset();
			}
		} 	
		finally {
			producer.flush();
		}
		return resultingOffset;
	}

	@Override
	public PubSub.EventSize getEventSize() {
		return messageSize;
	}
}