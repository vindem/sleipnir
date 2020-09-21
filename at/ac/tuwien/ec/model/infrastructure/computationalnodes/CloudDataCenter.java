package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.availability.AvailabilityModel;
import at.ac.tuwien.ec.model.pricing.PricingModel;

public class CloudDataCenter extends ComputationalNode{
	
	private AvailabilityModel availabilityModel;
	
	public CloudDataCenter(String id, HardwareCapabilities capabilities) {
		super(id, capabilities);
		// TODO Auto-generated constructor stub
	}

	public CloudDataCenter(String id, HardwareCapabilities capabilities, PricingModel pricingModel)
	{
		super(id,capabilities);
		this.priceModel = pricingModel;
	}
	
	public CloudDataCenter(String id, HardwareCapabilities capabilities, PricingModel pricingModel, AvailabilityModel avModel)
	{
		super(id,capabilities);
		this.priceModel = pricingModel;
		this.availabilityModel = avModel;
	}
	
	public void setAvailabilityModel(AvailabilityModel model)
	{
		this.availabilityModel = model;
	}
	
	public double getAvailabilityAt(double runtime)
	{
		return availabilityModel.availabilityAt(runtime);
	}
	
	public boolean isAvailableAt(double runtime)
	{
		return availabilityModel.isAvailableAt(runtime);
	}
	
	@Override
	public void sampleNode() {
		// TODO Auto-generated method stub
		
	}

}
