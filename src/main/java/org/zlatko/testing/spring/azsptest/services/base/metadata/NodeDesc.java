package org.zlatko.testing.spring.azsptest.services.base.metadata;

import java.util.Optional;

import org.zlatko.testing.spring.azsptest.services.api.Metadata;

import lombok.Getter;

@Getter
public class NodeDesc implements Metadata.Node {
	
	private String name;
	private Optional<String> rack=Optional.empty();
	private Optional<Integer> port=Optional.empty();
	private Optional<String> id=Optional.empty();
	
	public NodeDesc(String name) {
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
	
	public static NodeDesc builder(String name) {
		return new NodeDesc(name);
	}
	
	public NodeDesc withRack(String rack) {
		if (rack!=null)
			setRack(rack);
		return this;
	}
	
	public NodeDesc withPort(Integer port) {
		setPort(port);
		return this;
	}
	
	public NodeDesc withId(String id) {
		setId(id);
		return this;
	}

}
