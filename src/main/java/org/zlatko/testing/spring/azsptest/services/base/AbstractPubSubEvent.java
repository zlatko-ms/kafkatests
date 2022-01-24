package org.zlatko.testing.spring.azsptest.services.base;

import org.zlatko.testing.spring.azsptest.services.api.PubSub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.SneakyThrows;

public abstract class AbstractPubSubEvent implements PubSub.Event { 
	
	protected static ObjectWriter jsonObjectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

	@SneakyThrows
	@Override
	public String getValueAsJson() {
		return jsonObjectWriter.writeValueAsString(getValue());
	}
}
