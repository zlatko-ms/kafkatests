package org.zlatko.testing.spring.azsptest.services.kafka;

import java.util.UUID;

import org.zlatko.testing.spring.azsptest.services.Services.PubSubMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.Getter;
import lombok.SneakyThrows;

/** Implementation of a simple kafka , string transported, key/value message */
@Getter
public class SimpleKafkaMessage implements PubSubMessage {

	static ObjectWriter jsonObjectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

	private String key = UUID.randomUUID().toString();
	private Object value;

	public SimpleKafkaMessage(Object value) {
		this.value = value;
	}

	public SimpleKafkaMessage(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	@SneakyThrows
	@Override
	public String getValueAsJson() {
		return jsonObjectWriter.writeValueAsString(value);
	}
}