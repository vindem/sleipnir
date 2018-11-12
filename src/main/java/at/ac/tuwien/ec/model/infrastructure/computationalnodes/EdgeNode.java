package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.pricing.PricingModel;

public class EdgeNode extends ComputationalNode {

	public EdgeNode(String id, HardwareCapabilities capabilities) {
		super(id, capabilities);
		// TODO Auto-generated constructor stub
	}

	public EdgeNode(String id, HardwareCapabilities capabilities, PricingModel priceModel) {
		super(id, capabilities);
		this.priceModel = priceModel;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void sampleNode() {
		// TODO Auto-generated method stub

	}

}
