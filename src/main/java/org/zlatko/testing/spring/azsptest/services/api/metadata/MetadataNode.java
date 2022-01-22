package org.zlatko.testing.spring.azsptest.services.api.metadata;

import java.util.Optional;

public interface MetadataNode extends MetadataStringDescriptible {
	public String getName();
	public Optional<String> getRack();
}
