package org.zlatko.testing.spring.azsptest.services.base.metadata;

import java.util.List;
import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.Metadata;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.AbstractConfigurableService;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.collect.Lists;

import lombok.extern.java.Log;

@Log
public abstract class AbstractMetadataFetcherService extends AbstractConfigurableService implements Metadata.FetcherService {

	protected final static String OP_LIST_NODES = "listing nodes";
	protected final static String OP_LIST_CGS = "listing consumer groups";
	protected final static String OP_LIST_TOPICS = "listing topics";
	
	protected final static String ITEM_NODES  = "### [ Nodes           ] ######################################";
	protected final static String ITEM_TOPICS = "### [ Topics          ] #######################################";
	protected final static String ITEM_CGS    = "### [ Consumer Groups ] ##########################################";
	
	protected AbstractMetadataFetcherService(Service.ServiceType serviceType, ServiceConfiguration appConfig) {
		super(serviceType, appConfig);
	}
	
	private void logUnsupportedOperation(String operation) {
		log.warning(String.format("%s is not supported by service %s",operation, getServiceType().name().toLowerCase()));
	}
	
	public void run() {
		List<String> descLines = Lists.newArrayList();
		
		log.info(OP_LIST_NODES);
		Optional<List<Metadata.Node>> nodes = getNodesDescriptionDescLines();
		if (nodes.isEmpty()) {
			logUnsupportedOperation(OP_LIST_NODES);
		} else {
			descLines.add(ITEM_NODES);
			nodes.get().forEach( d -> { descLines.add(d.getDescription()); });
		}
		
		log.info(OP_LIST_TOPICS);
		Optional<List<Metadata.Topic>> topics= getTopicsDescriptionDescLines();
		if (topics.isEmpty()) {
			logUnsupportedOperation(OP_LIST_TOPICS);
		} else {
			descLines.add(ITEM_TOPICS);
			topics.get().forEach( d -> { descLines.add(d.getDescription()); });
		}
		
		log.info(OP_LIST_CGS);
		Optional<List<Metadata.ConsumerGroup>> cgroups = getConsumerGroupDescLines();
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
