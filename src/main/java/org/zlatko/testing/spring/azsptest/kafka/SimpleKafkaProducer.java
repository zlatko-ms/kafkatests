package org.zlatko.testing.spring.azsptest.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.zlatko.testing.spring.azsptest.kafka.Kafka.KafkaTestMessage;
import org.zlatko.testing.spring.azsptest.kafka.Kafka.KafkaTestService;
import org.zlatko.testing.spring.azsptest.kafka.Kafka.TestWorkloadType;
import org.zlatko.testing.spring.azsptest.kafka.partition.EvenOddPartitioner;
import org.zlatko.testing.spring.azsptest.kafka.partition.EvenOddPrimePartioner;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.base.Joiner;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

/** simple kafka producer test service */
@Log
final class SimpleKafkaProducer extends BaseKafkaService implements KafkaTestService {

	private final class ConfigurationProperties {
		static final String CONF_BATCH_SIZE = "messages.per.batch";
		static final String CONF_WAIT_AFTER_BATCH = "wait.after.batch.ms";
		static final String CONF_MAX_MESSAGES = "messages.max";
		static final String CONF_TOPIC_NAME = "topic.name";
		static final String CONF_TOPIC_CREATE = "topic.create";
		static final String CONF_TOPIC_CREATE_PARTITIONS = "topic.create.partitions";
		static final String CONF_TOPIC_CREATE_REPLICAS= "topics.create.replication";
		static final String CONF_PRODUCER_REPLICATION="topic.partitioner";
	}
	
	enum PartitionnerType {
		UNIQUE, EVEN_ODD, EVEN_ODD_PRIME
	}
	
	private KafkaProducer<String, String> producer;
	private AdminClient adminClient;
	private long messagesPerBatch;
	private long waitAfterBatchMs;
	private long messageCounter;
	private String topicName;
	private boolean topicCreate;
	private long messageLimit;
	private int topicsCreatePartitions;
	private short topicsCreateReplication;
	
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

		super(TestWorkloadType.PRODUCER, configuration);

		messagesPerBatch = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_BATCH_SIZE, "1"));
		waitAfterBatchMs = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_WAIT_AFTER_BATCH, "5000"));
		topicName = getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_NAME, "");
		topicCreate = Boolean.parseBoolean(getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_CREATE, "true"));
		messageLimit = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_MAX_MESSAGES, "-1"));

		topicsCreatePartitions = Integer.parseInt(getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_CREATE_PARTITIONS, "1"));
		topicsCreateReplication = Short.parseShort(getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_CREATE_REPLICAS, "1"));
		String partitionnerclass = getPartitionnerClassName(getServiceProperties().getProperty(ConfigurationProperties.CONF_PRODUCER_REPLICATION,"UNIQUE"));
		
		adminClient = AdminClient.create(getKafkaProperties());
		addSpecificKafkaProp(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		addSpecificKafkaProp(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		if (partitionnerclass!=null) {
			addSpecificKafkaProp(ProducerConfig.PARTITIONER_CLASS_CONFIG, partitionnerclass);
			log.info("Using partitioner class "+partitionnerclass);
		}
		
		producer = new KafkaProducer<String, String>(getKafkaProperties());
	}

	@SneakyThrows
	private void createTopic() {
		log.info(String.format(LogMessageConstants.MESSAGE_TOPIC_CREATING, topicName));
		List<NewTopic> topicList = new ArrayList<NewTopic>();
		Map<String, String> configs = new HashMap<String, String>();
		NewTopic newTopic = new NewTopic(topicName, topicsCreatePartitions, topicsCreateReplication).configs(configs);
		topicList.add(newTopic);
		adminClient.createTopics(topicList);
		log.info(String.format(LogMessageConstants.MESSAGE_TOPIC_CREATED, topicName, topicsCreatePartitions, topicsCreateReplication));
	}

	@SneakyThrows
	private void ensureTopicExists() {
		log.info(String.format(LogMessageConstants.MESSAGE_TOPIC_CHECKING, topicName));
		ListTopicsResult listTopics = adminClient.listTopics();
		Set<String> names = listTopics.names().get();
		if (names.contains(topicName)) {
			log.info(String.format(LogMessageConstants.MESSAGE_TOPIC_EXISTS, topicName));
		} else {
			createTopic();
		}
	}

	private List<KafkaTestMessage> buildBatch() {
		List<KafkaTestMessage> batch = new ArrayList<KafkaTestMessage>();
		for (int i = 0; i < messagesPerBatch; i++) {
			Properties p = new Properties();
			p.put("message", "hello-" + (messageCounter + i));
			KafkaTestMessage msg = new SimpleKafkaMessage(p);
			batch.add(msg);
		}
		return batch;
	}

	@SneakyThrows
	private void sendBatchToTopic(String topicName, List<KafkaTestMessage> messages) {

		long resultingOffset = 0;
		try {
			for (KafkaTestMessage msg : messages) {
				final RecordMetadata rm = producer.send(new ProducerRecord<String, String>(topicName,
						msg.getKey().toString(), msg.getValueAsJson())).get();
				resultingOffset = rm.offset();
			}
		} finally {
			producer.flush();
			messageCounter += messages.size();
		}
		log.info(String.format(LogMessageConstants.MESSAGE_PRODUCED_LOG_DATA, messagesPerBatch, resultingOffset,
				messageCounter));

	}

	@SuppressWarnings("static-access")
	@Override
	@SneakyThrows
	public void run() {
		log.info(String.format(LogMessageConstants.MESSAGE_STARTING_SERVICE, getName(), topicName));
		if (topicCreate)
			ensureTopicExists();
		while (true) {
			List<KafkaTestMessage> messages = buildBatch();
			sendBatchToTopic(topicName, messages);
			if ((messageLimit > 0) && (messageCounter >= messageLimit)) {
				log.info(String.format(LogMessageConstants.MESSAGE_LIMIT_REACHED, messageLimit));
				return;
			}
			log.info(String.format(LogMessageConstants.MESSAGES_WAIT, waitAfterBatchMs));
			Thread.currentThread().sleep(waitAfterBatchMs);
		}
	}

}