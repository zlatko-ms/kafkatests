package org.zlatko.testing.spring.azsptest.services.api.metadata;

import java.util.List;
import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.ConfigurableService;

/** basic interface for getting metadata */
public interface MetadataFetcher extends ConfigurableService {
	// returns one description per node if node fetching is supported, Optional.Empty if not supported
	Optional<List<MetadataNode>> getNodesDescriptionDescLines();
	// returns one description per topic if topc fetching is supported, Optional.Empty if not supported
	Optional<List<MetadataTopic>> getTopicsDescriptionDescLines();
	// returns one description per consumer groups if cg fetching is supported, Optional.Empty if not supported
	Optional<List<MetadataConsumerGroup>> getConsumerGroupDescLines();
}