package org.zlatko.testing.spring.azsptest.kafka;

/** centralizes all log messages for the kafka services */
class LogMessageConstants {

	public static final String MESSAGE_TOPIC_CREATING = "creating topic %s";
	public static final String MESSAGE_TOPIC_EXISTS = "topic %s already exists";
	public static final String MESSAGE_TOPIC_CREATED = "topic %s created, partitions=%d replicas=%d";
	public static final String MESSAGE_TOPIC_CHECKING = "checking existance of topic %s";

	public static final String MESSAGES_SEND = "messages send";
	public static final String MESSAGES_SEND_COUNTERS = "batchSize=%d;topicOffset=%d;totalMessages=%d";

	public static final String MESSAGES_READ = "messages polled";
	public static final String MESSAGES_READ_COUNTERS = "pollTime=%d;polledMessages=%d;totalMessages=%d";

	public static final String MESSAGE_STARTING_SERVICE = "starting service %s on topic %s";
	public static final String MESSAGES_WAIT = "pausing for %d ms";

	public static final String MESSAGE_PRODUCED_LOG_DATA = MESSAGES_SEND + ";" + MESSAGES_SEND_COUNTERS;
	public static final String MESSAGE_CONSUMED_LOG_DATA = MESSAGES_READ + ";" + MESSAGES_READ_COUNTERS;

	public static final String MESSAGE_LIMIT_REACHED = "all messages have been sent, limit of %d reached";
	public static final String MESSAGE_METADATA_SEPARATOR = "------------------------------------------";
	public static final String MESSAGE_METADATA_FETCHING_NODES = "fetching cluster nodes";
	public static final String MESSAGE_METADATA_FETCHED_NODES = "discovered nodes :";
	public static final String MESSAGE_METADATA_FETCHING_TOPICS = "fetching topics";
	public static final String MESSAGE_METADATA_FETCHED_TOPICS = "fetched topics :";
}