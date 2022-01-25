package org.zlatko.testing.spring.azsptest.providers.kafka.impl;

import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

public final class EvenOddPrimePartioner  extends BaseAbstractPartitioner implements Partitioner {

	@Override
	public void configure(Map<String, ?> configs) {}

	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
		
		String keyAsString = key.toString();
		int max = keyAsString.length() -1;
		int start = max /2 ;
		String keyExtract = keyAsString.substring(start, max);
		int hashIntValue = Integer.parseInt(keyExtract);
		
		if (isPrime(hashIntValue))
			return 2;
		if (isEven(hashIntValue))
			return 1;
		
		return 0;
	}
}