package org.zlatko.testing.spring.azsptest.services.api.metadata;

import java.util.Optional;

public interface MetadataConsumerGroup extends MetadataStringDescriptible {
	public String getName();
	public Optional<Boolean> isInternal();
	public Optional<Boolean> isSimple();
	public Optional<String> getTopic();
	public Optional<String> getId();
}
