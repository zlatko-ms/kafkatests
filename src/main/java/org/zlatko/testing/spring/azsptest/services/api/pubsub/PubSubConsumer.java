package org.zlatko.testing.spring.azsptest.services.api.pubsub;

import java.util.List;
import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.ConfigurableService;

public interface PubSubConsumer extends ConfigurableService {
	
	// subscribes to topic
	public void subscribeToTopic();
	// return the poll time in ms
	public long getPollTimeMs();
	// return the wait time after each poll if defined, Optional.empty() instead
	public Optional<Long> getIdleAfterPollMs();
	// return the topic we joined
	public String getTopicName();
	// returns true if the message details should be dumped
	public boolean dumpMessageDetails();
	// returns the group id if set, Optional.empty() if not
	public Optional<String> getGroupId();
	// poll the messages and return them as a list
	public List<PubSubMessage> pollMessages();
	// return the number of messages read since the start of the processing
	public int getMessagesCount();

}
