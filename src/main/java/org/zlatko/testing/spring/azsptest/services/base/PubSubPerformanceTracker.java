package org.zlatko.testing.spring.azsptest.services.base;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

import lombok.Getter;

@Getter
/** utility class to track event performances */
public class PubSubPerformanceTracker {
	
	private int totalMessagesCount=0;
	private long totalProcessingTimeMs=0;
	private long totalMessagesSizeBytes=0;
	
	private static final DecimalFormat df = new DecimalFormat("0.00");
	
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
	    double kbs = totalMessagesSizeBytes/1024;
	    double secs = totalProcessingTimeMs/1000;
		return  (kbs/secs);
	}
	
	public double getThroughputEps() {
		double secs = totalProcessingTimeMs/1000;
		return (totalMessagesCount/secs);
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
	
	public static final String formatDecimal(double d) {
		return df.format(d);
	}

	
	
}