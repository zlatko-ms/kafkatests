package org.zlatko.testing.spring.azsptest.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.zlatko.testing.spring.azsptest.services.base.PubSubPerformanceTracker;

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

}
