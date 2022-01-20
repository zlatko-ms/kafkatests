package org.zlatko.testing.spring.azsptest.kafka.partition;

import java.math.BigInteger;
import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;

abstract class BaseAbstractPartitioner implements Partitioner {
	
	protected boolean isEven(int number) {
		return ( (number & 1) == 0 );
	}
	
	protected boolean isPrime(int number) {
	    BigInteger bigInt = BigInteger.valueOf(number);
	    return bigInt.isProbablePrime(100);
	}
	
	@Override
	public void configure(Map<String, ?> configs) {}
	
	@Override
	public void close() {}
}