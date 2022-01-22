package org.zlatko.testing.spring.azsptest.services.base.pubsub;

import java.util.List;

import org.zlatko.testing.spring.azsptest.services.api.ServiceType;
import org.zlatko.testing.spring.azsptest.services.api.pubsub.PubSubConsumer;
import org.zlatko.testing.spring.azsptest.services.api.pubsub.PubSubMessage;
import org.zlatko.testing.spring.azsptest.services.base.AbstractBaseService;
import org.zlatko.testing.spring.azsptest.util.Configuration.ServiceConfiguration;

import lombok.extern.java.Log;


@Log
public abstract class AbstractBaseConsumer extends AbstractBaseService implements PubSubConsumer {

	private int readMessagesCount = 0;
	
	protected AbstractBaseConsumer(ServiceType serviceType, ServiceConfiguration appConfig) {
		super(serviceType, appConfig);
	}
	
	@Override
	public int getMessagesCount() {
		return readMessagesCount;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		
		subscribeToTopic();
		
		while (true) {
			List<PubSubMessage> messages = pollMessages();
			log.info(String.format("polling messages for %d ms",getPollTimeMs()));
			readMessagesCount+=messages.size();
			log.info(String.format("polled %d messages , total messages read %d",messages.size(),getMessagesCount()));
			if ( dumpMessageDetails()) {
				messages.forEach( m -> {
					log.info(String.format("message key=%s value=%s", m.getKey(),m.getValueAsJson()));
				});
			}
			if (getIdleAfterPollMs().isPresent()) {
				log.info(String.format("sleeping %d ms",getIdleAfterPollMs().get()));
				try {
					Thread.currentThread().sleep(getIdleAfterPollMs().get());
				} catch (Throwable t ) {
					String.format("wait interrupted cause=", t.getMessage());
				}
			}			
		}
	}

}
