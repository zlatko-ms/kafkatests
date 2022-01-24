package org.zlatko.testing.spring.azsptest.services.base.metadata;

import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.Metadata;

import lombok.Getter;

@Getter
public class ConsumerGroupDesc implements Metadata.ConsumerGroup {
	
	private String name;
	private Optional<Boolean> internal=Optional.empty();
	private Optional<Boolean> simple=Optional.empty();
	private Optional<String> topic = Optional.empty();
	private Optional<String> id = Optional.empty();
	
	public ConsumerGroupDesc(String name) {
		this.name=name;
	}
	
	private void setInternal(boolean isInternal) {
		this.internal=Optional.of(isInternal);
	}
	
	private void setSimple(boolean isSimple) {
		this.simple=Optional.of(isSimple);
	}
	
	private void setTopic(String topic) {
		this.topic=Optional.of(topic);
	}
	
	private void setId(String id) {
		this.id=Optional.of(id);
	}

	@Override
	public Optional<Boolean> isInternal() {
		return getInternal();
	}

	@Override
	public Optional<Boolean> isSimple() {
		return getSimple();
	}

	@Override
	public String getDescription() {
		return String.format("[>] consumer_group name=%s internal=%s simple=%s topic=%s id=%s", 
				getName(), 
				isInternal().isEmpty() ? "n/a" : ""+isInternal().get(), 
				isSimple().isEmpty() ? "n/a" : ""+isSimple().get(), 
				getTopic().isEmpty() ? "n/a" : ""+getTopic().get(),
				getId().isEmpty() ? "n/a" : getId().get()
				);
	}
	
	public static ConsumerGroupDesc builder(String name) {
		return new ConsumerGroupDesc(name);
	}
	
	public ConsumerGroupDesc asInternal(boolean internal) {
		setInternal(internal);
		return this;
	}
	
	public ConsumerGroupDesc asSimple(boolean simple) {
		setSimple(simple);
		return this;
	}
	
	public ConsumerGroupDesc forTopic(String topicName) {
		if (topicName!=null)
			setTopic(topicName);
		return this;
	}
	
	public ConsumerGroupDesc withId(String id) {
		if (id!=null) {
			setId(id);
		}
		return this;
	}
	
	
}
