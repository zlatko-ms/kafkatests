package org.zlatko.testing.spring.azsptest.services.base.metadata;

import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.Metadata;

import lombok.Getter;

@Getter
public class TopicDesc implements Metadata.Topic {
	
	private String name;
	private Optional<String> id = Optional.empty();
	private Optional<Integer> replicaCount = Optional.empty();
	private Optional<Boolean> isInternal = Optional.empty();
	private int partitionCount=1;

	public TopicDesc(String name) {
		this.name=name;
	}
	
	private void setPartitions(int parts) {
		this.partitionCount=parts;
	}
	
	private void setReplicaCont(int count) {
		this.replicaCount=Optional.of(count);
	}
	
	private void setId(String id) {
		this.id=Optional.of(id);
	}
	
	private void setInternal(boolean internal) {
		this.isInternal=Optional.of(internal);	
	}

	@Override
	public String getDescription() {
		return String.format("[>] topic name=%s partitions=%s replicas=%s", 
				getName(),
				getPartitionCount(),
				getReplicaCount().isEmpty() ? "n/a" : getReplicaCount().get());
	}
	
	public static TopicDesc builder(String name) {
		return new TopicDesc(name);
	}
	
	public TopicDesc withPartitions(int parts) {
		setPartitions(parts);
		return this;
	}
	
	public TopicDesc withReplication(int repl) {
		setReplicaCont(repl);
		return this;
	}
	
	public TopicDesc withId(String id) {
		setId(id);
		return this;
	}
	
	public TopicDesc asInternal(boolean internal) {
		setInternal(internal);
		return this;
	}
	
}
