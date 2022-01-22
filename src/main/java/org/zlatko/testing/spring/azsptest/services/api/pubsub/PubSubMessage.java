package org.zlatko.testing.spring.azsptest.services.api.pubsub;

/** simple kafka message abstraction */
public interface PubSubMessage {
	// message key
	String getKey();
	// message value (as object)
	Object getValue();
	// message value as Json String
	String getValueAsJson();
}