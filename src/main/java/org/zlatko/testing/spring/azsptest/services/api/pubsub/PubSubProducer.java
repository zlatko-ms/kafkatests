package org.zlatko.testing.spring.azsptest.services.api.pubsub;

import java.util.List;
import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.ConfigurableService;

public interface PubSubProducer extends ConfigurableService {

	// max number of messages to producer before exiting processing
	public Optional<Long> getMaxMessagesToProduce();

	// size of the message batch to send
	public int getMessageBatchSize();

	// idle time between batches in ms
	public long getPostBatchWaitTimeMs();

	// the topic on which messages are sent
	public String getTopicName();
	
	// ensures the target topic is created
	public void ensureTopicCreated();

	// send the messages in a batch mode and return resulting offset
	public long sendMessages(List<PubSubMessage> messages);
	
	// track processing time in millisecs
	public void addProcessingTimeMs(long timeSpentMs);
	
	// track produced messages count
	public void addMessagesProducedCount(int messages);
	
	// track produced messages size
	public void addMessagesProducedSizeBytes(long sizeInBytes);
	
	// get number of messages produced
	public int getMessageCount();
	
	// get average message/sec indicator
	public double getMessagesPerSecond();
	
	// get average throughput (bytes/sec) indicator
	public double getMessagesTroughput();

}
