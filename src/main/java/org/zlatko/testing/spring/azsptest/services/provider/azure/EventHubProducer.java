package org.zlatko.testing.spring.azsptest.services.provider.azure;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.zlatko.testing.spring.azsptest.services.api.PubSub.Event;
import org.zlatko.testing.spring.azsptest.services.api.Service;
import org.zlatko.testing.spring.azsptest.services.base.pubsub.AbstractProducerService;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.EventData;
import com.microsoft.azure.eventhubs.EventDataBatch;
import com.microsoft.azure.eventhubs.EventHubClient;

import lombok.SneakyThrows;
import lombok.extern.java.Log;

@Log
public class EventHubProducer extends AbstractProducerService  {

	private EventHubClient sender;
	private Gson gson = new GsonBuilder().create();
	private ScheduledExecutorService executorService;
	
	public EventHubProducer(ServiceConfiguration configuration) {
		super(Service.ServiceType.PRODUCER_AZURE,configuration);
	
		final String namespece= getMandatoryProperty(AzureConfigurationProperties.CONF_PREFIX,AzureConfigurationProperties.CONF_NAMESPACE);
		final String hubname = getMandatoryProperty(AzureConfigurationProperties.CONF_PREFIX,AzureConfigurationProperties.CONF_HUB);
		final String keyname = getMandatoryProperty(AzureConfigurationProperties.CONF_PREFIX,AzureConfigurationProperties.CONF_SAAS_KEY_NAME);
		final String keyval = getMandatoryProperty(AzureConfigurationProperties.CONF_PREFIX,AzureConfigurationProperties.CONF_SAAS_KEY_KEY_VALUE);
		
		final ConnectionStringBuilder connStr = new ConnectionStringBuilder()
                .setNamespaceName(namespece) 
                .setEventHubName(hubname)
                .setSasKeyName(keyname)
                .setSasKey(keyval);

		executorService = Executors.newSingleThreadScheduledExecutor();
      
        try {
        	sender = EventHubClient.createFromConnectionStringSync(connStr.toString(), executorService);
        } catch(Throwable t) {
        	String error = String.format("unable to connect to the event hub cause=%s",t.getMessage());
        	log.severe(error);
        	throw new RuntimeException(error);
        } 
	}

	@Override
	public void ensureTopicCreated() {
		log.severe("this provider does not support the creation of topics, please create the topic before");
	}

	@Override
	@SneakyThrows
	public long sendEvents(List<Event> messages) {
		long eventsSent=0;
		try {
			final EventDataBatch events = sender.createBatch();
            EventData sendEvent;
            for (Event m : messages) {
            	do {
            		Map<String,String> payload = Maps.newHashMap();
            		payload.put("key",m.getKey());
            		payload.put("value",m.getValueAsJson());
            		final byte[] payloadBytes = gson.toJson(payload).getBytes(Charset.defaultCharset());
            		sendEvent = EventData.create(payloadBytes);
            		eventsSent++;
            	} while(events.tryAdd(sendEvent));          	
            }
		}
		finally {
			 sender.closeSync();
		}
		return eventsSent;
	}

	@Override
	public void shutdown() {
		executorService.shutdown();
		
	}
	
}
