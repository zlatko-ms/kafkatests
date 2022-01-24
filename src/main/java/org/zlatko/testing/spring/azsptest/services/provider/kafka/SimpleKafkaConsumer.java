package org.zlatko.testing.spring.azsptest.services.provider.kafka;

import java.util.Collections;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.zlatko.testing.spring.azsptest.services.api.PubSub;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.SimplePubSubEvent;
import org.zlatko.testing.spring.azsptest.services.base.pubsub.AbstractConsumerService;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.collect.Lists;

import lombok.extern.java.Log;

/** simple Kafka consumer test service */
@Log
public class SimpleKafkaConsumer extends AbstractConsumerService {

	private Consumer<String, String> kafkaConsumer;
	
	public SimpleKafkaConsumer(ServiceConfiguration configuration) {

		super(Service.ServiceType.CONSUMER, configuration);
		if (getConsumerGroup().isPresent())
			addSpecificKafkaProp(ConsumerConfig.GROUP_ID_CONFIG, getConsumerGroup().get());
		addSpecificKafkaProp(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		addSpecificKafkaProp(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

		kafkaConsumer = new KafkaConsumer<String, String>(getKafkaProperties());
	}

	@Override
	public List<PubSub.Event> pollEvents() {
		List<PubSub.Event> res = Lists.newArrayList();
		@SuppressWarnings("deprecation")
		final ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(getPollTimeMs());
		consumerRecords.forEach( cr -> {
			res.add(new SimplePubSubEvent(cr.key(), cr.value()));
		});
		return res;
	}

	@Override
	public void subscribeToTopic() {
		log.info(String.format("subscribing to topic %s",getTopicName()));
		kafkaConsumer.subscribe(Collections.singletonList(getTopicName()));
		log.info("subscribed to topic");
	}

}