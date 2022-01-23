package org.zlatko.testing.spring.azsptest.services.base;

import java.util.Map;
import java.util.UUID;

import org.zlatko.testing.spring.azsptest.services.api.PubSub;

import com.google.common.collect.Maps;

import lombok.Getter;

@Getter
public class SizedPubSubMessage extends AbstractBasePubSubMessage implements PubSub.Event {

	private String key;
	private Map<String,String> value = Maps.newHashMap();
	
	public SizedPubSubMessage(int index, String keyPrefix,PubSub.EventSize size) {
		this(keyPrefix+index,size);
	}
	
	public SizedPubSubMessage(String key,PubSub.EventSize size) {
		this.key=key;
		int kbs = size.getSize();
		value.put("key", key);
		for (int i=0;i<kbs;i++) {
			value.put("attr-"+i, UUID.randomUUID().toString());
		}
	}

}
