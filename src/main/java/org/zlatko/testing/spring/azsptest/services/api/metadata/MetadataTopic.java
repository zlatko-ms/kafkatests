package org.zlatko.testing.spring.azsptest.services.api.metadata;

import java.util.Optional;

public interface MetadataTopic extends MetadataStringDescriptible {
	public String getName();
	public Optional<String> getId();
	public Optional<Integer> getReplicaCount();
	public int getPartitionCount();
}
