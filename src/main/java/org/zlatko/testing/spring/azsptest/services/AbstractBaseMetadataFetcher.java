package org.zlatko.testing.spring.azsptest.services;

import java.util.List;
import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.Services.ServiceType;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.collect.Lists;

import lombok.extern.java.Log;

@Log
public abstract class AbstractBaseMetadataFetcher extends AbstractBaseService implements Services.MetadataTestService {

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

	protected void addToReport(List<String> report,String header,String operation,String item,Optional<List<String>> explored) {
		report.add(header);
		if (explored.isEmpty()) {
			log.warning(String.format("%s is not supported for service %s",operation,getServiceType().name().toLowerCase()));
		} else {
			explored.get().forEach( desc -> {
				report.add(String.format("%s %s", item,desc));
			});
		}
	}
	
	public void run() {
		List<String> descLines = Lists.newArrayList();
		
		log.info(OP_LIST_NODES);
		addToReport(descLines,ITEM_NODES,OP_LIST_NODES,ITEM_NODE,getNodesDescriptionDescLines());

		log.info(OP_LIST_TOPICS);
		addToReport(descLines,ITEM_TOPICS,OP_LIST_TOPICS,ITEM_TOPIC,getTopicsDescriptionDescLines());

		log.info(OP_LIST_CGS);
		addToReport(descLines,ITEM_CGS,OP_LIST_CGS,ITEM_CG,getConsumerGroupDescLines());
		
		descLines.forEach( l -> { System.out.println(l);} );

		
	}
	

}
