package org.zlatko.testing.spring.azsptest.kafka;

import java.util.Collections;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.zlatko.testing.spring.azsptest.kafka.Kafka.KafkaTestService;
import org.zlatko.testing.spring.azsptest.kafka.Kafka.TestWorkloadType;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

/** simple kafka consumer test service */
@Log
final class SimpleKafkaConsumer extends BaseKafkaService implements KafkaTestService {

	private final class ConfigurationProperties {
		static final String CONF_POLL_DURATION_MS = "poll.interval.ms";
		static final String CONF_TOPIC_NAME = "topic.name";
		static final String CONF_WAIT_AFTER_POLL_MS = "wait.after.poll.ms";
		static final String CONF_CONSUMER_GROUP_ID = "group.id";
		static final String CONF_CONSUMER_PRINT_MESSAGE="message.unit.print";
	}

	private Consumer<String, String> kafkaConsumer;
	private String topicName;
	private long pollIntervalMs;
	private long waitAfterPollMs;
	private String consumerGroup;
	private boolean printMessages;
	
	public SimpleKafkaConsumer(ServiceConfiguration configuration) {

		super(TestWorkloadType.CONSUMER, configuration);

		topicName = getServiceProperties().getProperty(ConfigurationProperties.CONF_TOPIC_NAME, "");
		pollIntervalMs = Long.parseLong(
				getServiceProperties().getProperty(ConfigurationProperties.CONF_POLL_DURATION_MS, "1000"));
		waitAfterPollMs = Long.parseLong(
				getServiceProperties().getProperty(ConfigurationProperties.CONF_WAIT_AFTER_POLL_MS, "1000"));
		consumerGroup = getServiceProperties().getProperty(ConfigurationProperties.CONF_CONSUMER_GROUP_ID,
				"azTestConsumerGroup");
		printMessages =Boolean.parseBoolean(getServiceProperties().getProperty(ConfigurationProperties.CONF_CONSUMER_PRINT_MESSAGE,"false"));

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
				log.info(String.format(LogMessageConstants.MESSAGE_CONSUMED_LOG_DATA, pollIntervalMs, polledRecords,
						overallCount));
				kafkaConsumer.commitAsync();
				if (printMessages) {
					consumerRecords.forEach((record) -> {
						log.info(String.format("read message partition=%d key=%s value=%s", record.partition(),record.key(),record.value()));
					});
				}
				log.info(String.format(LogMessageConstants.MESSAGES_WAIT, waitAfterPollMs));
				Thread.sleep(waitAfterPollMs);
			}
		} finally {
			kafkaConsumer.close();
		}
	}

}