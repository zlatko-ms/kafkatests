package org.zlatko.testing.spring.azsptest.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.zlatko.testing.spring.azsptest.services.api.PubSub.EventSize;
import org.zlatko.testing.spring.azsptest.services.base.PubSubPerformanceTracker;
import org.zlatko.testing.spring.azsptest.services.base.SizedPubSubEvent;

class PubSubPerformanceTrackerTest {

	@Test
	void testEventPerSecond() {
		PubSubPerformanceTracker tracker = new PubSubPerformanceTracker();
		tracker.increaseMessageCount(100);
		tracker.increaseProcessingTimeMillisecs(1000);
		assertEquals(100,tracker.getThroughputEps());
	}
	
	@Test
	void testKilobytesPerSecond() {
		PubSubPerformanceTracker tracker = new PubSubPerformanceTracker();
		tracker.increaseMessageCount(100);
		tracker.increaseProcessingTimeMillisecs(1000);
		tracker.increaseProcessingPayloadSizeBytes(100*1024);
		assertEquals(100,tracker.getThroughputKbs());
	}
	
	
	private void assertMessageSize(EventSize size) {
		SizedPubSubEvent event = new SizedPubSubEvent("test",size);
		long min = size.getSize() * 1024;
		PubSubPerformanceTracker tracker = new PubSubPerformanceTracker();
		long eventSizeBytes = tracker.getBytesInString(event.getValueAsJson());
		assertTrue ( (eventSizeBytes>=min) , "event of size "+size.name()+" is at least "+min+" bytes long, current len ="+eventSizeBytes);
		
	}
	
	@Test
	void testEventSizeMedium() {
		for (EventSize z : EventSize.values() ) {
			assertMessageSize(z);
		}
		
	}

}
