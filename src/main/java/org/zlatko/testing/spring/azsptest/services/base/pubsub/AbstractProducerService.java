package org.zlatko.testing.spring.azsptest.services.base.pubsub;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.zlatko.testing.spring.azsptest.services.api.PubSub;
import org.zlatko.testing.spring.azsptest.services.api.PubSub.EventSize;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.api.Service.ServiceType;
import org.zlatko.testing.spring.azsptest.services.base.AbstractConfigurableService;
import org.zlatko.testing.spring.azsptest.services.base.PubSubPerformanceTracker;
import org.zlatko.testing.spring.azsptest.services.base.SizedPubSubEvent;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

@Log
public abstract class AbstractProducerService extends AbstractConfigurableService implements PubSub.ProducerService {

	private final class ConfigurationProperties {
		static final String CONF_BATCH_SIZE = "events.per.batch";
		static final String CONF_WAIT_AFTER_BATCH = "wait.after.batch.ms";
		static final String CONF_MAX_MESSAGES = "events.max";
		static final String CONF_MESSAGE_SIZE= "events.size";
		static final String CONF_TOPIC_NAME = "topic.name";
		static final String CONF_TOPIC_CREATE_PARTITIONS = "topic.create.partitions";
		static final String CONF_TOPIC_CREATE_REPLICAS = "topics.create.replication";
	}
	
	@Getter 
	@Setter(AccessLevel.PROTECTED)
	private int messageBatchSize = 0;
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private long postBatchWaitTimeMs = 1000;
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private String topicName;
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private Optional<Long> maxMessagesToProduce = Optional.empty();
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private int topicPartitionCount=1;
	@Getter
	@Setter(AccessLevel.PROTECTED)	
	private EventSize eventSize= EventSize.S;
	@Getter
	@Setter(AccessLevel.PROTECTED)
	private Optional<Short> topicReplicaCount=Optional.empty();
	
	private PubSubPerformanceTracker perfTracker = new PubSubPerformanceTracker();

	protected AbstractProducerService(Service.ServiceType serviceType, ServiceConfiguration appConfig) {
		super(serviceType, appConfig);
		
		Properties producerProps = appConfig.getConfiguration(ServiceType.PRODUCER.name().toLowerCase());
		messageBatchSize = Integer.parseInt(producerProps.getProperty(ConfigurationProperties.CONF_BATCH_SIZE, "1"));
		postBatchWaitTimeMs = Long.parseLong(producerProps.getProperty(ConfigurationProperties.CONF_WAIT_AFTER_BATCH, "5000"));
		topicName = producerProps.getProperty(ConfigurationProperties.CONF_TOPIC_NAME, "zkafkatesttopic");
		long maxMessages = Long.parseLong(producerProps.getProperty(ConfigurationProperties.CONF_MAX_MESSAGES, "-1"));
		if (maxMessages>0)
			maxMessagesToProduce=Optional.of(maxMessages);
		topicPartitionCount = Integer.parseInt(producerProps.getProperty(ConfigurationProperties.CONF_TOPIC_CREATE_PARTITIONS, "1"));
		topicReplicaCount = Optional.of(Short.parseShort(producerProps.getProperty(ConfigurationProperties.CONF_TOPIC_CREATE_REPLICAS, "1")));
		eventSize = PubSub.EventSize.valueOf(producerProps.getProperty(ConfigurationProperties.CONF_MESSAGE_SIZE, "M").toUpperCase());
		
	}

	protected List<PubSub.Event> buildBatch() {
		List<PubSub.Event> batch = new ArrayList<PubSub.Event>();
		for (int i = 0; i < getMessageBatchSize(); i++) {
			int currentMessagesTotal = perfTracker.getTotalMessagesCount();
			Properties p = new Properties();
			p.put("message", "hello-" + (currentMessagesTotal + i));
			PubSub.Event msg = new SizedPubSubEvent(currentMessagesTotal + i, "message-", getEventSize());
			batch.add(msg);
		}
		return batch;
	}

	@SuppressWarnings("static-access")
	@Override
	public void run() {

		boolean keepProcessing = true;

		ensureTopicCreated();

		while (keepProcessing) {

			log.info(String.format("bulding batch of %d messages with %s payload [%s k]", getMessageBatchSize(),
					getEventSize().name(), getEventSize().getSize()));

			List<PubSub.Event> messages = buildBatch();
			long batchSizeInBytes = 0;
			for (PubSub.Event e : messages) {
				batchSizeInBytes += perfTracker.getBytesInString(e.getValueAsJson());
			}

			log.info("sending batch");
			long startTime = System.currentTimeMillis();
			sendEvents(messages);
			long duration = System.currentTimeMillis() - startTime;
			if (duration == 0)
				duration = 1;
				
			log.info(String.format("batch sent in %d ms total size=%s bytes", duration, batchSizeInBytes));
			
			perfTracker.increaseProcessingPayloadSizeBytes(batchSizeInBytes);
			perfTracker.increaseMessageCount(messages.size());
			perfTracker.increaseProcessingTimeMillisecs(duration);
			perfTracker.flushStats();

			log.info(String.format("aggregated stats throughput=%s kb/s speed=%s evts/s total_sent=%s limit=%s",
					perfTracker.getReadbleThroughputKBs(), perfTracker.getReadableThroughputEps(),
					perfTracker.getTotalMessagesCount(),
					getMaxMessagesToProduce().isPresent() ? getMaxMessagesToProduce().get() : "unlimited"));

			long waitTime = getPostBatchWaitTimeMs();
			if (waitTime > 0) {
				log.info(String.format("wating %s ms before next batch", waitTime));
				try {
					Thread.currentThread().sleep(waitTime);
				} catch (Throwable t) {
					log.severe("error while waiting cause=" + t.getMessage());
				}
			}

			if (getMaxMessagesToProduce().isPresent()) {
				keepProcessing = perfTracker.getTotalMessagesCount() < getMaxMessagesToProduce().get();
				if (!keepProcessing) {
					log.info("all messages sent, finishing processing");
					shutdown();
				}
			}
		}
	}

}
