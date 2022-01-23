package org.zlatko.testing.spring.azsptest.services.api;

import java.util.List;
import java.util.Optional;

/** Contains definitions for the metadata fetching service and elements */
public final class Metadata {

	/** Describes a consumer group */
	public interface ConsumerGroup extends MetadataStringDescriptible {
		public String getName();
		public Optional<Boolean> isInternal();
		public Optional<Boolean> isSimple();
		public Optional<String> getTopic();
		public Optional<String> getId();
	}
	
	/** Describes a cluster node */
	public interface Node extends MetadataStringDescriptible {
		public String getName();
		public Optional<String> getRack();
	}
	
	/** Describes a topic */
	public interface Topic extends MetadataStringDescriptible {
		public String getName();
		public Optional<String> getId();
		public Optional<Integer> getReplicaCount();
		public int getPartitionCount();
	}
	
	/** Human readable form of a Description */
	public interface MetadataStringDescriptible  {
		public String getDescription();
	}
	
	/** Metadata fetching service interface */
	public interface FetcherService extends Service.ConfigurableService {
		Optional<List<Node>> getNodesDescriptionDescLines();
		Optional<List<Topic>> getTopicsDescriptionDescLines();
		Optional<List<ConsumerGroup>> getConsumerGroupDescLines();
	}
	

}
