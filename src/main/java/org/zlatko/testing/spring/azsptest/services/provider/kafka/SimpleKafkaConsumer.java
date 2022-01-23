package org.zlatko.testing.spring.azsptest.services.provider.kafka;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.zlatko.testing.spring.azsptest.services.api.PubSub;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.SimplePubSubMessage;
import org.zlatko.testing.spring.azsptest.services.base.pubsub.AbstractBaseConsumer;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import lombok.extern.java.Log;

/** simple Kafka consumer test service */
@Log
public class SimpleKafkaConsumer extends AbstractBaseConsumer {

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

		super(Service.ServiceType.CONSUMER, configuration);

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

	@Override
	public long getPollTimeMs() {
		return pollIntervalMs;
	}

	@Override
	public Optional<Long> getIdleAfterPollMs() {
		if (waitAfterPollMs>0)
			return Optional.of(waitAfterPollMs);
		return Optional.empty();
	}

	@Override
	public String getTopicName() {
		return topicName;
	}

	@Override
	public boolean dumpEventDetails() {
		return printMessages;
	}

	@Override
	public Optional<String> getGroupId() {
		if (Strings.isNullOrEmpty(consumerGroup))
			return Optional.empty();
		return Optional.of(consumerGroup);
	}

	@Override
	public List<PubSub.Event> pollEvents() {
		List<PubSub.Event> res = Lists.newArrayList();
		@SuppressWarnings("deprecation")
		final ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(getPollTimeMs());
		consumerRecords.forEach( cr -> {
			res.add(new SimplePubSubMessage(cr.key(), cr.value()));
		});
		return res;
	}

	@Override
	public void subscribeToTopic() {
		log.info(String.format("subscribing to topic %s",topicName));
		kafkaConsumer.subscribe(Collections.singletonList(topicName));
		log.info("subscribed to topic");
	}
}