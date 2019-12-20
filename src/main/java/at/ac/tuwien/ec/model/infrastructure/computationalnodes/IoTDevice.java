package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import at.ac.tuwien.ec.model.HardwareCapabilities;

public class IoTDevice extends NetworkedNode {

	private String[] topics;
	
	public IoTDevice(String id, HardwareCapabilities capabilities) {
		super(id, capabilities);
		// TODO Auto-generated constructor stub
	}

	public String[] getTopics() {
		return topics;
	}

	public void setTopics(String[] topics) {
		this.topics = topics;
	}
		

}
