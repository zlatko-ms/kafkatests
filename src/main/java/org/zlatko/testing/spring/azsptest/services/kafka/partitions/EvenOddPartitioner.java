package org.zlatko.testing.spring.azsptest.services.kafka.partitions;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

public class EvenOddPartitioner extends BaseAbstractPartitioner implements Partitioner {

	@Override
	public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes,
			Cluster cluster) {
				
		if (isEven(key.toString().hashCode()))
			return 1;
		
		return 0;
	}
}