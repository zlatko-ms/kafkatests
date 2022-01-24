package org.zlatko.testing.spring.azsptest.services.base.pubsub;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.zlatko.testing.spring.azsptest.services.api.PubSub;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.AbstractConfigurableService;
import org.zlatko.testing.spring.azsptest.services.base.PubSubPerformanceTracker;
import org.zlatko.testing.spring.azsptest.services.base.SizedPubSubEvent;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import lombok.extern.java.Log;

@Log
public abstract class AbstractProducerService extends AbstractConfigurableService implements PubSub.ProducerService {
	
	private PubSubPerformanceTracker perfTracker = new PubSubPerformanceTracker();

	protected AbstractProducerService(Service.ServiceType serviceType, ServiceConfiguration appConfig) {
		super(serviceType, appConfig);
	}

	protected List<PubSub.Event> buildBatch() {
		List<PubSub.Event> batch = new ArrayList<PubSub.Event>();
		for (int i = 0; i < getMessageBatchSize(); i++) {
			int currentMessagesTotal = perfTracker.getTotalMessagesCount() ;
			Properties p = new Properties();
			p.put("message", "hello-" + (currentMessagesTotal  + i));
			PubSub.Event msg = new SizedPubSubEvent(currentMessagesTotal + i,"message-",getEventSize());
			batch.add(msg);
		}
		return batch;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		
		boolean keepProcessing=true; 
		
		ensureTopicCreated();
	
		while (keepProcessing) {
			
			log.info(String.format("bulding batch of %d messages with %s payload [%s k]",
					getMessageBatchSize(), 
					getEventSize().name(),
					getEventSize().getSize()));
			
			List<PubSub.Event> messages = buildBatch();
			long batchSizeInBytes = 0;
			for (PubSub.Event e : messages ) {
				batchSizeInBytes+=PubSubPerformanceTracker.getBytesInString(e.getValueAsJson());
			}
				
			log.info("sending batch");
			long startTime = System.currentTimeMillis();
			sendEvents(messages);
			long duration = System.currentTimeMillis() - startTime; 
			if (duration<1)
				duration=1;
			
			log.info( String.format("batch sent in %d ms total size=%s bytes",duration,batchSizeInBytes));
			double currentStepThroughputEpS = getMessageBatchSize()/(duration/1000);
			double currentStepThroughputKbs = (batchSizeInBytes/1024)/(duration/1000);
			perfTracker.increaseProcessingPayloadSizeBytes(batchSizeInBytes);
			perfTracker.increaseMessageCount(messages.size());
			perfTracker.increaseProcessingTimeMillisecs(duration);
		
			
			log.info(String.format("last stats thoughput=%s kb/s speed=%s evts/s",
					PubSubPerformanceTracker.formatDecimal(currentStepThroughputKbs),
					PubSubPerformanceTracker.formatDecimal(currentStepThroughputEpS)));
			
			log.info(String.format("aggregated stats thoughput=%s kb/s speed=%s evts/s total_sent=%s limit=%s",
					perfTracker.getReadbleThroughputKBs(),
					perfTracker.getReadableThroughputEps(),
					perfTracker.getTotalMessagesCount(),
					getMaxMessagesToProduce().isPresent() ? getMaxMessagesToProduce().get() : "unlimited"));
			
			
			log.info(String.format(";CSVSTATS;%s;%s;%s",perfTracker.getReadbleThroughputKBs(),perfTracker.getReadableThroughputEps(),perfTracker.getTotalMessagesCount()));
			
			long waitTime = getPostBatchWaitTimeMs();
			if (waitTime>0) {
				log.info(String.format("wating %s ms before next batch", waitTime));
				try {
					Thread.currentThread().sleep(waitTime);
				} catch (Throwable t) {
					log.severe("error while waiting cause="+t.getMessage());
				}
			}
			
			if ( getMaxMessagesToProduce().isPresent() ) {
				keepProcessing = perfTracker.getTotalMessagesCount() <  getMaxMessagesToProduce().get();
				if (!keepProcessing) {
					log.info("all messages sent, finishing processing");
				}
			}
		}
	}

	
	
}
