package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.pricing.PricingModel;

public class CloudDataCenter extends ComputationalNode{
	
	public CloudDataCenter(String id, HardwareCapabilities capabilities) {
		super(id, capabilities);
		// TODO Auto-generated constructor stub
	}

	public CloudDataCenter(String id, HardwareCapabilities capabilities, PricingModel pricingModel)
	{
		super(id,capabilities);
		this.priceModel = pricingModel;
		
	}
	
	@Override
	public void sampleNode() {
		// TODO Auto-generated method stub
		
	}

}
