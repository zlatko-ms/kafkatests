package org.zlatko.testing.spring.azsptest.services.base.pubsub;

import java.util.List;

import org.zlatko.testing.spring.azsptest.services.api.PubSub;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.AbstractConfigurableService;
import org.zlatko.testing.spring.azsptest.services.base.PubSubPerformanceTracker;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import lombok.extern.java.Log;


@Log
public abstract class AbstractConsumerService extends AbstractConfigurableService implements PubSub.ConsumerService {

	private PubSubPerformanceTracker perfTracker = new PubSubPerformanceTracker();
	
	protected AbstractConsumerService(Service.ServiceType serviceType, ServiceConfiguration appConfig) {
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
	

			long bytesRecv = 0;
			for (PubSub.Event e : messages) {
				bytesRecv+=PubSubPerformanceTracker.getBytesInString(e.getValueAsJson());
			}
			
			perfTracker.increaseMessageCount(messages.size());
			perfTracker.increaseProcessingTimeMillisecs(pollTime);
			perfTracker.increaseProcessingPayloadSizeBytes(bytesRecv);
			
			if ( dumpEventDetails()) {
				messages.forEach( m -> {
					log.info(String.format("<event_dump> message key=%s value=%s", m.getKey(),m.getValueAsJson()));
				});
			}
			
			double durationSeconds = pollTime / 1000;
			double throughputKbs = (bytesRecv/1024)/durationSeconds;
			double throughputEps = messages.size()/durationSeconds;
			
			
			log.info(String.format("instant stats messages=%d throughput=%s kb/s speed=%s evts/s",
					messages.size(),
					PubSubPerformanceTracker.formatDecimal(throughputKbs),
					PubSubPerformanceTracker.formatDecimal(throughputEps)));
			
			log.info(String.format("aggregated stats messages=%d throughput=%s kb/s speed=%s evts/s",
					perfTracker.getTotalMessagesCount(),
					perfTracker.getReadbleThroughputKBs(),
					perfTracker.getReadableThroughputEps()));
			
			log.info(String.format(";CSVSTATS;%s;%s;%s",
					perfTracker.getReadbleThroughputKBs(),
					perfTracker.getReadableThroughputEps(),
					perfTracker.getTotalMessagesCount()));
			
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
