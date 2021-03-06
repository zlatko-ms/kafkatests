package org.zlatko.testing.spring.azsptest.services.base;

import java.util.UUID;

import org.zlatko.testing.spring.azsptest.services.api.PubSub;

import lombok.Getter;

/** Implementation of a simple kafka , string transported, key/value message */
@Getter
public class SimplePubSubEvent extends AbstractPubSubEvent implements PubSub.Event {

	private String key = UUID.randomUUID().toString();
	private Object value;

	public SimplePubSubEvent(Object value) {
		this.value = value;
	}

	public SimplePubSubEvent(String key, Object value) {
		this.key = key;
		this.value = value;
	}

}