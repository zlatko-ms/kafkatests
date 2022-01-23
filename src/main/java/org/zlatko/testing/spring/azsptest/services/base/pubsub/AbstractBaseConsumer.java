package org.zlatko.testing.spring.azsptest.services.base.pubsub;

import java.util.List;

import org.zlatko.testing.spring.azsptest.services.api.PubSub;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.AbstractBaseService;
import org.zlatko.testing.spring.azsptest.services.base.PubSubPerformanceTracker;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import lombok.extern.java.Log;


@Log
public abstract class AbstractBaseConsumer extends AbstractBaseService implements PubSub.ConsumerService {

	private PubSubPerformanceTracker perfTracker = new PubSubPerformanceTracker();
	
	protected AbstractBaseConsumer(Service.ServiceType serviceType, ServiceConfiguration appConfig) {
		super(serviceType, appConfig);
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
	
			perfTracker.increaseMessageCount(messages.size());
			perfTracker.increaseProcessingTimeMillisecs(pollTime);
			
			messages.forEach( m -> {
				perfTracker.increaseProcessingPayloadSizeBytes(PubSubPerformanceTracker.getBytesInString(m.getValueAsJson()));
			});
	
			
			if ( dumpEventDetails()) {
				messages.forEach( m -> {
					log.info(String.format("<event_dump> message key=%s value=%s", m.getKey(),m.getValueAsJson()));
				});
			}
			
			log.info(String.format("stats messages=%d throughput=%s kb/s speed=%s evts/s",
					perfTracker.getTotalMessagesCount(),
					perfTracker.getReadbleThroughputKBs(),
					perfTracker.getReadableThroughputEps()));
			
			log.info(String.format(";CSVSTATS;%s;%s;%s",perfTracker.getReadbleThroughputKBs(),perfTracker.getReadableThroughputEps(),perfTracker.getTotalMessagesCount()));
			
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
