package org.zlatko.testing.spring.azsptest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.zlatko.testing.spring.azsptest.services.api.PubSub;

class PubSubEventSizeTest {
	
	@Test
	void testFromStringValue() {
		String name = "m";
		PubSub.EventSize size = PubSub.EventSize.valueOf(name.toUpperCase());
		assertEquals(size,PubSub.EventSize.M);
		assertEquals(32,size.getSize());
	}
	
	

}
