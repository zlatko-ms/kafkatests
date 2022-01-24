package org.zlatko.testing.spring.azsptest.services.base;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.zlatko.testing.spring.azsptest.services.api.PubSub;

import com.google.common.collect.Maps;

import lombok.Getter;

@Getter
public class SizedPubSubEvent extends AbstractPubSubEvent implements PubSub.Event {

	private String key;
	private Map<String,String> value = Maps.newHashMap();
	
	public SizedPubSubEvent(int index, String keyPrefix,PubSub.EventSize size) {
		this(keyPrefix+index,size);
	}
	
	public SizedPubSubEvent(String key,PubSub.EventSize size) {
		this.key=key;
		int totalBytes = size.getSize() *1024;
		value.put("key", key);
		
		int remainingBytes = totalBytes-key.getBytes(StandardCharsets.UTF_8).length;
		int iterations = remainingBytes / 52; 
		
		for (int i=0;i<iterations;i++) {
			value.put("attr-"+i, UUID.randomUUID().toString());
		}
	}

}
