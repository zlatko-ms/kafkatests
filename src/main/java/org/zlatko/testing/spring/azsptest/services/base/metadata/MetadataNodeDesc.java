package org.zlatko.testing.spring.azsptest.services.base.metadata;

import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.metadata.MetadataNode;

import lombok.Getter;

@Getter
public class MetadataNodeDesc implements MetadataNode {
	
	private String name;
	private Optional<String> rack=Optional.empty();
	private Optional<Integer> port=Optional.empty();
	private Optional<String> id=Optional.empty();
	
	public MetadataNodeDesc(String name) {
		this.name = name;
	}
	
	private void setRack(String rack) {
		this.rack=Optional.of(rack);
	}
	
	private void setPort(Integer port) {
		this.port=Optional.of(port);
	}
	
	private void setId(String id) {
		this.id=Optional.of(id);
	}
	
	@Override
	public String getDescription() {
		return String.format("[>] node name=%s rack=%s", getName() , getRack().isEmpty() ? "n/a" : getRack().get());
	}
	
	public static MetadataNodeDesc builder(String name) {
		return new MetadataNodeDesc(name);
	}
	
	public MetadataNodeDesc withRack(String rack) {
		if (rack!=null)
			setRack(rack);
		return this;
	}
	
	public MetadataNodeDesc withPort(Integer port) {
		setPort(port);
		return this;
	}
	
	public MetadataNodeDesc withId(String id) {
		setId(id);
		return this;
	}

}
