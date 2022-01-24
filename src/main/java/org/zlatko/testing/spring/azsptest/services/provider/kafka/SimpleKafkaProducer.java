package org.zlatko.testing.spring.azsptest.services.provider.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/** simple Kafka producer test service */
@Log
public class SimpleKafkaProducer extends AbstractProducerService  {

	private final class ConfigurationProperties {
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

	private String partitionerClassName;
	private KafkaProducer<String, String> producer;
	private AdminClient adminClient;

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

		partitionerClassName = getPartitionnerClassName(getServiceProperties().getProperty(ConfigurationProperties.CONF_PRODUCER_REPLICATION, "UNIQUE"));
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
		log.info(String.format(LogMessages.MESSAGE_TOPIC_CREATING, getTopicName()));
		List<NewTopic> topicList = new ArrayList<NewTopic>();
		Map<String, String> configs = new HashMap<String, String>();
		short replicas = getTopicReplicaCount().isEmpty() ? 1 : getTopicReplicaCount().get();
		NewTopic newTopic = new NewTopic(getTopicName(), getTopicPartitionCount(), replicas).configs(configs);
		topicList.add(newTopic);
		adminClient.createTopics(topicList);
		log.info(String.format(LogMessages.MESSAGE_TOPIC_CREATED, getTopicName(), getTopicPartitionCount(),replicas));
	}

	@SneakyThrows
	@Override
	public void ensureTopicCreated() {
		log.info(String.format(LogMessages.MESSAGE_TOPIC_CHECKING, getTopicName()));
		ListTopicsResult listTopics = adminClient.listTopics();
		Set<String> names = listTopics.names().get();
		if (names.contains(getTopicName())) {
			log.info(String.format(LogMessages.MESSAGE_TOPIC_EXISTS, getTopicName()));
		} else {
			createTopic();
		}
	}

	@Override
	@SneakyThrows
	public long sendEvents(List<PubSub.Event> messages) {
		long resultingOffset = -1;
		try {
			for (PubSub.Event msg : messages) {
				final RecordMetadata rm = producer.send(
						new ProducerRecord<String, String>(getTopicName(), msg.getKey().toString(), msg.getValueAsJson()))
						.get();
				resultingOffset = rm.offset();
			}
		} 	
		finally {
			producer.flush();
		}
		return resultingOffset;
	}

}