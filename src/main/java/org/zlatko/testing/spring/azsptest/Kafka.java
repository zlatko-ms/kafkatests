package org.zlatko.testing.spring.azsptest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.zlatko.testing.spring.azsptest.Configuration.ServiceConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
public class Kafka {

	/** defines the types of test workloads */
	public enum TestWorkloadType {
		PRODUCER, CONSUMER
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
	public static final KafkaTestService buildTestService(TestWorkloadType type,ServiceConfiguration appConf) {
		switch (type) {
		case PRODUCER:
			return new SimpleKafkaProducer(appConf);
		case CONSUMER:
			return new SimpleKafkaConsumer(appConf);
		}
		throw new IllegalArgumentException(type.name()+" is not supported yet");
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
	
	/**Implementation of a simple kafka , string transported, key/value message  */
	@Getter
	public static class SimpleKafkaMessage implements KafkaTestMessage {
		
		static ObjectWriter jsonObjectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

		private String key = UUID.randomUUID().toString();
		private Object value;

		public SimpleKafkaMessage(Object value) {
			this.value = value;
		}
		
		public SimpleKafkaMessage(String key,Object value) {
			this.key=key;
			this.value=value;
		}

		@SneakyThrows
		@Override
		public String getValueAsJson() {
			return jsonObjectWriter.writeValueAsString(value);
		}
	}
	
	/** base kafka test service class, provides common configuration processing facilities */ 
	abstract static class BaseKafkaService implements KafkaTestService {

		private final static String KAFKA_SHARED_SERVICE = "kafka";

		private Properties kafkaProperties;
		private Properties serviceProperties;
		private TestWorkloadType serviceType;

		protected BaseKafkaService(TestWorkloadType serviceType, ServiceConfiguration appConfig) {
			this.serviceType = serviceType;
			kafkaProperties = new Properties();
			kafkaProperties.putAll(appConfig.getServiceConfiguration(KAFKA_SHARED_SERVICE));
			serviceProperties = new Properties();
			serviceProperties.putAll(appConfig.getServiceConfiguration(getName()));
		}

		protected void addSpecificKafkaProp(String key, String value) {
			kafkaProperties.put(key, value);
		}

		@Override
		public TestWorkloadType getServiceType() {
			return this.serviceType;
		}

		@Override
		public String getName() {
			return getServiceType().name().toLowerCase();
		}

		@Override
		public Properties getKafkaProperties() {
			return kafkaProperties;
		}

		@Override
		public Properties getServiceProperties() {
			return serviceProperties;
		}
	}
	

	/** simple kafka consumer test service */
	final static class SimpleKafkaConsumer extends BaseKafkaService implements KafkaTestService {

		private final class ConfigurationProperties {
			static final String CONF_POLL_DURATION_MS = "poll.interval.ms";
			static final String CONF_TOPIC_NAME = "topic.name";
			static final String CONF_WAIT_AFTER_POLL_MS = "wait.after.poll.ms";
			static final String CONF_CONSUMER_GROUP_ID = "group.id";
		}

		private Consumer<String, String> kafkaConsumer;
		private String topicName;
		private long pollIntervalMs;
		private long waitAfterPollMs;
		private String consumerGroup;

		public SimpleKafkaConsumer(ServiceConfiguration configuration) {

			super(TestWorkloadType.CONSUMER, configuration);

			topicName = getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_NAME, "");
			pollIntervalMs = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_POLL_DURATION_MS, "1000"));
			waitAfterPollMs = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_WAIT_AFTER_POLL_MS, "1000"));
			consumerGroup = getServiceProperties().getProperty(ConfigurationProperties.CONF_CONSUMER_GROUP_ID, "azTestConsumerGroup");

			addSpecificKafkaProp(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
			addSpecificKafkaProp(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			addSpecificKafkaProp(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

			kafkaConsumer = new KafkaConsumer<String, String>(getKafkaProperties());
		}

		@SuppressWarnings("deprecation")
		@Override
		@SneakyThrows
		public void run() {

			try {
				long overallCount = 0;
				log.info(String.format(LogMessageConstants.MESSAGE_STARTING_SERVICE, getName(), topicName));
				kafkaConsumer.subscribe(Collections.singletonList(topicName));

				while (true) {
					final ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(pollIntervalMs);
					long polledRecords = consumerRecords.count();
					overallCount += polledRecords;
					log.info(String.format(LogMessageConstants.MESSAGE_CONSUMED_LOG_DATA, pollIntervalMs, polledRecords, overallCount));
					kafkaConsumer.commitAsync();
					consumerRecords.forEach((record) -> {
						log.fine("got message key=" + record.key() + " value=" + record.value());
					});
					log.info(String.format(LogMessageConstants.MESSAGES_WAIT, waitAfterPollMs));
					Thread.sleep(waitAfterPollMs);
				}
			} finally {
				kafkaConsumer.close();
			}
		}

	}
	
	/** simple kafka producer test service */
	final static class SimpleKafkaProducer extends BaseKafkaService implements KafkaTestService {
		
		private final class ConfigurationProperties {
			static final String CONF_BATCH_SIZE = "messages.per.batch";
			static final String CONF_WAIT_AFTER_BATCH = "wait.after.batch.ms";
			static final String CONF_MAX_MESSAGES = "messages.max";
			static final String CONF_TOPIC_NAME = "topic.name";
			static final String CONF_TOPIC_CREATE = "topic.create";
		}

		private KafkaProducer<String, String> producer;
		private AdminClient adminClient;
		private long messagesPerBatch;
		private long waitAfterBatchMs;
		private long messageCounter;
		private String topicName;
		private boolean topicCreate;
		private long messageLimit;

		public SimpleKafkaProducer(ServiceConfiguration configuration) {
			
			super(TestWorkloadType.PRODUCER,configuration);
			
			messagesPerBatch = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_BATCH_SIZE,"1"));
			waitAfterBatchMs = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_WAIT_AFTER_BATCH,"5000"));;
			topicName = getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_NAME,"");
			topicCreate = Boolean.parseBoolean(getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_CREATE,"true"));
			messageLimit = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_MAX_MESSAGES,"-1"));
			
			adminClient = AdminClient.create(getKafkaProperties());
			addSpecificKafkaProp(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			addSpecificKafkaProp(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
			producer = new KafkaProducer<String, String>(getKafkaProperties());
		}
		
		@SneakyThrows
		private void createTopic() {
			log.info(String.format(LogMessageConstants.MESSAGE_TOPIC_CREATING,topicName));
			List<NewTopic> topicList = new ArrayList<NewTopic>();
			Map<String, String> configs = new HashMap<String, String>();
			int partitions = 1;
			Short replication = 1;
			NewTopic newTopic = new NewTopic(topicName, partitions, replication).configs(configs);
			topicList.add(newTopic);
			adminClient.createTopics(topicList);
			log.info(String.format(LogMessageConstants.MESSAGE_TOPIC_CREATED,topicName));
		}

		@SneakyThrows
		private void ensureTopicExists() {
			log.info(String.format(LogMessageConstants.MESSAGE_TOPIC_CHECKING,topicName));
			ListTopicsResult listTopics = adminClient.listTopics();
			Set<String> names = listTopics.names().get();
			if ( names.contains(topicName) ) {
				log.info(String.format(LogMessageConstants.MESSAGE_TOPIC_EXISTS,topicName));
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
			
			long resultingOffset=0;
			try {
				for (KafkaTestMessage msg : messages) {
					final RecordMetadata rm = producer
							.send(new ProducerRecord<String, String>(topicName, msg.getKey().toString(), msg.getValueAsJson()))
							.get();
					resultingOffset = rm.offset();
				}			
			} finally {
				producer.flush();
				messageCounter+=messages.size();
			}
			log.info(String.format(LogMessageConstants.MESSAGE_PRODUCED_LOG_DATA,messagesPerBatch,resultingOffset,messageCounter ));

		}

		@SuppressWarnings("static-access")
		@Override
		@SneakyThrows
		public void run() {
			log.info(String.format(LogMessageConstants.MESSAGE_STARTING_SERVICE,getName(),topicName));
			if (topicCreate)
				ensureTopicExists();
			while (true) {			
				List<KafkaTestMessage> messages = buildBatch();
				sendBatchToTopic(topicName, messages);
				if ( (messageLimit > 0 ) && (messageCounter >= messageLimit)){
					log.info(String.format(LogMessageConstants.MESSAGE_LIMIT_REACHED, messageLimit));
					return;
				}
				log.info(String.format(LogMessageConstants.MESSAGES_WAIT,waitAfterBatchMs));
				Thread.currentThread().sleep(waitAfterBatchMs);
			}
		}

	}
	
	/** centralizes all log messages for the kafka services */
	static class LogMessageConstants {

		public static final String MESSAGE_TOPIC_CREATING = "creating topic %s";
		public static final String MESSAGE_TOPIC_EXISTS = "topic %s already exists";
		public static final String MESSAGE_TOPIC_CREATED = "topic %s created";
		public static final String MESSAGE_TOPIC_CHECKING = "checking existance of topic %s";

		public static final String MESSAGES_SEND = "messages send";
		public static final String MESSAGES_SEND_COUNTERS = "batchSize=%d;topicOffset=%d;totalMessages=%d";

		public static final String MESSAGES_READ = "messages polled";
		public static final String MESSAGES_READ_COUNTERS = "pollTime=%d;polledMessages=%d;totalMessages=%d";

		public static final String MESSAGE_STARTING_SERVICE = "starting service %s on topic %s";
		public static final String MESSAGES_WAIT = "pausing for %d ms";

		public static final String MESSAGE_PRODUCED_LOG_DATA = MESSAGES_SEND + ";" + MESSAGES_SEND_COUNTERS;
		public static final String MESSAGE_CONSUMED_LOG_DATA = MESSAGES_READ + ";" + MESSAGES_READ_COUNTERS;
		
		public static final String MESSAGE_LIMIT_REACHED ="all messages have been sent, limit of %d reached";
	}

}
