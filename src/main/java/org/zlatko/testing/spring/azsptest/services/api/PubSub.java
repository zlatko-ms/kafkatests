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
		public boolean dumpEventDetails();
		// returns the group id if set, Optional.empty() if not
		public Optional<String> getGroupId();
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
		
		// ensures the target topic is created
		public void ensureTopicCreated();

		// send the messages in a batch mode and return resulting offset
		public long sendEvents(List<Event> messages);
		
		// returns the configured message payload size
		public EventSize getEventSize();

	}
	
	/** event payload sizes */
	public enum EventSize { 

		XS(1), 		// extra small = ~1k message
		S(16), 		// small = ~ 16k message
		M(32), 		// medium ~ 32k message
		L(64), 	 	// large ~ 64k message
		XL(128), 	// extra large ~128k message
		XXL(256);	// extra extra large ~256k message
		
		private int ksize;
		
		EventSize(int size) {
			this.ksize=size;
		}
		
		public int getSize() {
			return ksize;
		}
		
	};
}
