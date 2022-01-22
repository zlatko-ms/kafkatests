package org.zlatko.testing.spring.azsptest.services.base.metadata;

import java.util.List;
import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.ServiceType;
import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataConsumerGroup;
import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataFetcher;
import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataNode;
import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataStringDescriptible;
import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataTopic;
import org.zlatko.testing.spring.azsptest.services.base.AbstractBaseService;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.collect.Lists;

import lombok.extern.java.Log;

@Log
public abstract class AbstractBaseMetadataFetcher extends AbstractBaseService implements MetadataFetcher {

	protected final static String OP_LIST_NODES = "listing nodes";
	protected final static String OP_LIST_CGS = "listing consumer groups";
	protected final static String OP_LIST_TOPICS = "listing topics";
	
	protected final static String ITEM_NODES  = "### [ Nodes           ] ######################################";
	protected final static String ITEM_TOPICS = "### [ Topics          ] #######################################";
	protected final static String ITEM_CGS    = "### [ Consumer Groups ] ##########################################";
	
	protected final static String ITEM_NODE   = "[>] node";
	protected final static String ITEM_CG     = "[>] consumer_group";
	protected final static String ITEM_TOPIC  = "[>] topic";
	
	protected AbstractBaseMetadataFetcher(ServiceType serviceType, ServiceConfiguration appConfig) {
		super(serviceType, appConfig);
	}

	protected void addToReport(List<String> report,String header,String operation,String item,Optional<List<MetadataStringDescriptible>> explored) {
		report.add(header);
		if (explored.isEmpty()) {
			log.warning(String.format("%s is not supported for service %s",operation,getServiceType().name().toLowerCase()));
		} else {
			explored.get().forEach( desc -> {
				report.add(desc.getDescription());
			});
		}
	}
	
	private void logUnsupportedOperation(String operation) {
		log.warning(String.format("%s is not supported by service %s",operation, getServiceType().name().toLowerCase()));
	}
	
	public void run() {
		List<String> descLines = Lists.newArrayList();
		
		log.info(OP_LIST_NODES);
		Optional<List<MetadataNode>> nodes = getNodesDescriptionDescLines();
		if (nodes.isEmpty()) {
			logUnsupportedOperation(OP_LIST_NODES);
		} else {
			descLines.add(ITEM_NODES);
			nodes.get().forEach( d -> { descLines.add(d.getDescription()); });
		}
		
		log.info(OP_LIST_TOPICS);
		Optional<List<MetadataTopic>> topics= getTopicsDescriptionDescLines();
		if (topics.isEmpty()) {
			logUnsupportedOperation(OP_LIST_TOPICS);
		} else {
			descLines.add(ITEM_TOPICS);
			topics.get().forEach( d -> { descLines.add(d.getDescription()); });
		}
		
		log.info(OP_LIST_CGS);
		Optional<List<MetadataConsumerGroup>> cgroups = getConsumerGroupDescLines();
		if (cgroups.isEmpty()) {
			logUnsupportedOperation(OP_LIST_CGS);
		} else {
			descLines.add(ITEM_CGS);
			cgroups.get().forEach( d -> { descLines.add(d.getDescription()); });
		}
		
		System.out.println("");
		descLines.forEach( l -> { System.out.println(l);} );

		
	}
	

}
