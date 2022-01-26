package org.zlatko.testing.spring.azsptest.services.base.pubsub;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.zlatko.testing.spring.azsptest.services.api.PubSub;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.AbstractConfigurableService;
import org.zlatko.testing.spring.azsptest.services.base.PubSubPerformanceTracker;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.base.Strings;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;


@Log
public abstract class AbstractConsumerService extends AbstractConfigurableService implements PubSub.ConsumerService {

	private final class ConfigurationProperties {
		static final String CONF_POLL_DURATION_MS = "poll.interval.ms";
		static final String CONF_TOPIC_NAME = "topic.name";
		static final String CONF_WAIT_AFTER_POLL_MS = "wait.after.poll.ms";
		static final String CONF_CONSUMER_GROUP_ID = "group.id";
		static final String CONF_CONSUMER_PRINT_MESSAGE="message.unit.print";
	}
	
	@Getter 
	@Setter(AccessLevel.PROTECTED)
	private String topicName;
	@Getter 
	@Setter(AccessLevel.PROTECTED)
	private long pollTimeMs;
	@Getter 
	@Setter(AccessLevel.PROTECTED)
	private Optional<Long> idleAfterPollMs;
	@Getter 
	@Setter(AccessLevel.PROTECTED)	
	private Optional<String> consumerGroup;
	@Getter 
	@Setter(AccessLevel.PROTECTED)		
	private boolean dumpEventsConfigured;
	
	private PubSubPerformanceTracker perfTracker = new PubSubPerformanceTracker();
	
	
	protected AbstractConsumerService(Service.ServiceType serviceType, ServiceConfiguration appConfig) {
		super(serviceType, appConfig);
		Properties consumerConfig= appConfig.getConfiguration(Service.ServiceType.CONSUMER.name().toLowerCase());
		topicName = consumerConfig.getProperty(ConfigurationProperties.CONF_TOPIC_NAME, "");
		pollTimeMs = Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_POLL_DURATION_MS, "1000"));
		idleAfterPollMs = Optional.of(Long.parseLong(getServiceProperties().getProperty(ConfigurationProperties.CONF_WAIT_AFTER_POLL_MS, "1000")));
		String cg =  getServiceProperties().getProperty(ConfigurationProperties.CONF_CONSUMER_GROUP_ID,null);
		consumerGroup = Strings.isNullOrEmpty(cg) ? Optional.empty() : Optional.of(cg);
		dumpEventsConfigured =Boolean.parseBoolean(getServiceProperties().getProperty(ConfigurationProperties.CONF_CONSUMER_PRINT_MESSAGE,"false"));
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		
		final long pollTime = getPollTimeMs();
		
		subscribeToTopic();
		
		while (true) {
		
			log.info(String.format("polling messages for %d ms",pollTime));
			List<PubSub.Event> messages = pollEvents();
			log.info(String.format("polled %d messages", messages.size()));


			long bytesRecv = 0;
			for (PubSub.Event e : messages) {
				bytesRecv+= perfTracker.getBytesInString(e.getValueAsJson());
			}
			
			perfTracker.increaseMessageCount(messages.size());
			perfTracker.increaseProcessingTimeMillisecs(pollTime);
			perfTracker.increaseProcessingPayloadSizeBytes(bytesRecv);
			perfTracker.flushStats();
			
			if ( dumpEventsConfigured ) {
				messages.forEach( m -> {
					log.info(String.format("<event_dump> message key=%s value=%s", m.getKey(),m.getValueAsJson()));
				});
			}
			
			log.info(String.format("aggregated stats messages=%d throughput=%s kb/s speed=%s evts/s",
					perfTracker.getTotalMessagesCount(),
					perfTracker.getReadbleThroughputKBs(),
					perfTracker.getReadableThroughputEps()));
			
			if (getIdleAfterPollMs().isPresent()) {
				log.info(String.format("sleeping %d ms",getIdleAfterPollMs().get()));
				try {
					Thread.currentThread().sleep(getIdleAfterPollMs().get());
				} catch (Throwable t ) {
					String.format("wait interrupted cause=", t.getMessage());
				}
			}			
		}
	}

}
