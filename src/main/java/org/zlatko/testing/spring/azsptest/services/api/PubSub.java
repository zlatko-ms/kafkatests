package org.zlatko.testing.spring.azsptest.services.api;

import java.util.List;
import java.util.Optional;

public final class PubSub {
	
	/** event abstraction */
	public interface Event {
		String getKey();
		Object getValue();
		String getValueAsJson();
	}

	/** processing performance tracker */
	public interface PerformanceTracker {
		// increases the number of processed messages
		public void increaseMessageCount(int messages);
		// increases the time spent in event processing
		public void increaseProcessingTimeMillisecs(long millisecs);
		// increases the size of the processed payloads
		public void increaseProcessingPayloadSizeBytes(long bytes);
		// returns the throughput in kb/s
		public double getThroughputKbs();
		// returns the throughput in events/s
		public double getThroughputEps();
		// returns a human readable form of the throughput in kb/s 
		public String getReadbleThroughputKBs();
		// returns a human readable form of the throughput in events/s
		public String getReadableThroughputEps();
		// returns a human readable form of the time passed in processing (basically minute level)
		public String getReadableProcessingTimeMinutes();
		// returns the size (bytes) of the string passed as payload
		public  int getBytesInString(String payload);
		// format decimals for spreadsheet
		public String formatDecimal(double d);
	}
	
	/** consumer service */
	public interface ConsumerService extends Service.ConfigurableService {
		// subscribes to topic
		public void subscribeToTopic();
		// returns the configured tpoll time in ms
		public long getPollTimeMs();
		// return the wait time after each poll if defined, Optional.empty() instead
		public Optional<Long> getIdleAfterPollMs();
		// return the topic we joined
		public String getTopicName();
		// returns true if the message details should be dumped
		public boolean isDumpEventsConfigured();
		// returns the group id if set, Optional.empty() if not
		public Optional<String> getConsumerGroup();
		// poll the messages and return them as a list
		public List<Event> pollEvents();

	}
	
	/** producer service */
	public interface ProducerService extends Service.ConfigurableService {
		// returns the configured max number of messages to producer before exiting processing
		public Optional<Long> getMaxMessagesToProduce();
		// returns the configured size of the message batch to send
		public int getMessageBatchSize();
		// returns the configured idle time between batches in ms
		public long getPostBatchWaitTimeMs();
		// the topic on which messages are sent
		public String getTopicName();
		// the number of partitions to create for the topic
		public int getTopicPartitionCount();
		// the number of replicas to create for the partitions
		public Optional<Short> getTopicReplicaCount();
		// ensures the target topic is created
		public void ensureTopicCreated();
		// send the messages in a batch mode and return resulting offset
		public long sendEvents(List<Event> messages);
		// returns the configured message payload size
		public EventSize getEventSize();
		// clean shutdown (if needed)
		public void shutdown();
		
	}
	
	/** event payload sizes */
	public enum EventSize { 

		XS(1), 		// extra small = ~1k event
		S(16), 		// small = ~ 16k event
		M(32), 		// medium ~ 32k event
		L(64), 	 	// large ~ 64k event
		XL(128), 	// extra large ~128k event
		XXL(256),   // extra extra large ~256k event
		H(1024),    // huge ~ 1M event
		XH(2048); 	// extra huge ~2M event
		
		private int ksize;
		
		EventSize(int size) {
			this.ksize=size;
		}
		
		public int getSize() {
			return ksize;
		}
		
	};
}
