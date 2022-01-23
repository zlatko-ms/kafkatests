package org.zlatko.testing.spring.azsptest.services.base;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

import lombok.Getter;

@Getter
public class PubSubPerformanceTracker {
	
	private int totalMessagesCount=0;
	private long totalProcessingTimeMs=0;
	private long totalMessagesSizeBytes=0;
	
	private static final DecimalFormat df = new DecimalFormat("0,00");
	
	public void increaseMessageCount(int messages) {
		totalMessagesCount+=messages;
	}
	
	public void increaseProcessingTimeMillisecs(long millisecs) {
		totalProcessingTimeMs+=millisecs;
	}
	
	public void increaseProcessingPayloadSizeBytes(long bytes) {
		totalMessagesSizeBytes+=bytes;
	}
	
	public double getThroughputKbs() {
		return (totalMessagesSizeBytes/1024)/(totalProcessingTimeMs/1000);
	}
	
	public double getThroughputEps() {
		return (totalMessagesCount/(totalProcessingTimeMs/1000));
	}
	
	public String getReadbleThroughputKBs() {
		return df.format(getThroughputKbs());
	}
	
	public String getReadableThroughputEps() {
		return df.format(getThroughputEps());
	}
	
	public String getReadableProcessingTimeMinutes() {
		double minutes = (getTotalProcessingTimeMs()/1000)/60;
		return df.format(minutes);
	}
	
	public static final int getBytesInString(String payload) {
		byte[] b = payload.getBytes(StandardCharsets.UTF_8);
		return b.length;
	}

	
	
}
