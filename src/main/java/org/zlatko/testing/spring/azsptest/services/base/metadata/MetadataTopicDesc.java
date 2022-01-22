package org.zlatko.testing.spring.azsptest.services.base.metadata;

import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataTopic;

import lombok.Getter;

@Getter
public class MetadataTopicDesc implements MetadataTopic {
	
	private String name;
	private Optional<String> id = Optional.empty();
	private Optional<Integer> replicaCount = Optional.empty();
	private Optional<Boolean> isInternal = Optional.empty();
	private int partitionCount=1;

	public MetadataTopicDesc(String name) {
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
	
	public static MetadataTopicDesc builder(String name) {
		return new MetadataTopicDesc(name);
	}
	
	public MetadataTopicDesc withPartitions(int parts) {
		setPartitions(parts);
		return this;
	}
	
	public MetadataTopicDesc withReplication(int repl) {
		setReplicaCont(repl);
		return this;
	}
	
	public MetadataTopicDesc withId(String id) {
		setId(id);
		return this;
	}
	
	public MetadataTopicDesc asInternal(boolean internal) {
		setInternal(internal);
		return this;
	}
	
}
