package org.zlatko.testing.spring.azsptest.services.base.pubsub;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.zlatko.testing.spring.azsptest.services.api.ServiceType;
import org.zlatko.testing.spring.azsptest.services.api.pubsub.PubSubMessage;
import org.zlatko.testing.spring.azsptest.services.api.pubsub.PubSubProducer;
import org.zlatko.testing.spring.azsptest.services.base.AbstractBaseService;
import org.zlatko.testing.spring.azsptest.services.base.SimplePubSubMessage;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import lombok.extern.java.Log;

@Log
public abstract class AbstractBaseProducer extends AbstractBaseService implements PubSubProducer {
	
	protected AbstractBaseProducer(ServiceType serviceType, ServiceConfiguration appConfig) {
		super(serviceType, appConfig);
	}

	private int producedMessageCount = 0;
	private int producedMessageSizeBytes= 0;
	private long producingPassedTimeMs = 0;
	
	@Override
	public int getMessageCount() {
		return producedMessageCount;
	}
	
	@Override
	public void addMessagesProducedCount(int messages) {
		producedMessageCount+=messages;
	}
	
	@Override
	public void addProcessingTimeMs(long timeSpentMs) {
		producingPassedTimeMs+=timeSpentMs;
	}
	
	@Override
	public void addMessagesProducedSizeBytes(long sizeInBytes) {
		producedMessageSizeBytes+=sizeInBytes;	
	}
	
	
	@Override
	public double getMessagesPerSecond() {
		double seconds = producingPassedTimeMs / 1000;
		return producedMessageCount/seconds;
	}
	
	@Override
	public double getMessagesTroughput() {
		double seconds = producingPassedTimeMs / 1000;
		return producedMessageSizeBytes / seconds;
	}
	
	protected List<PubSubMessage> buildBatch() {
		List<PubSubMessage> batch = new ArrayList<PubSubMessage>();
		for (int i = 0; i < getMessageBatchSize(); i++) {
			Properties p = new Properties();
			p.put("message", "hello-" + (getMessageCount() + i));
			PubSubMessage msg = new SimplePubSubMessage(p);
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
			
			log.info(String.format("bulding batch of %d messages", getMessageBatchSize()));
			List<PubSubMessage> messages = buildBatch();
			
			log.info("sending batch");
			long startTime = System.currentTimeMillis();
			sendMessages(messages);
			long endTime = System.currentTimeMillis();
			log.info("batch sent");
			messages.forEach( m -> {
				addMessagesProducedSizeBytes(m.getValueAsJson().length());
			});
			addMessagesProducedCount(messages.size());
			addProcessingTimeMs(endTime-startTime);
			log.info(String.format("current stats thoughput=%s [b/s] speed=%s [messages/s] sent=%s limit=%s",
					getMessagesTroughput(),
					getMessagesPerSecond(),
					getMessageCount(),
					getMaxMessagesToProduce().isPresent() ? getMaxMessagesToProduce().get() : "unlimited"));
			
			long waitTime = getPostBatchWaitTimeMs();
			if (waitTime>0) {
				log.info(String.format("Wating %s millisecs before next batch", waitTime));
				try {
					Thread.currentThread().sleep(waitTime);
				} catch (Throwable t) {
					log.severe("error while waiting cause="+t.getMessage());
				}
			}
			
			if ( getMaxMessagesToProduce().isPresent() ) {
				keepProcessing = getMessageCount() <  getMaxMessagesToProduce().get();
				if (!keepProcessing) {
					log.info("all messages sent, finishing processing");
				}
			}
		}
	}
	
	
}
