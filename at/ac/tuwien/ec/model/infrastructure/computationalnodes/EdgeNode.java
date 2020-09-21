package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.availability.AvailabilityModel;
import at.ac.tuwien.ec.model.pricing.PricingModel;

public class EdgeNode extends ComputationalNode {

	private AvailabilityModel availabilityModel;
	
	public EdgeNode(String id, HardwareCapabilities capabilities) {
		super(id, capabilities);
		// TODO Auto-generated constructor stub
	}

	public EdgeNode(String id, HardwareCapabilities capabilities, PricingModel priceModel) {
		super(id, capabilities);
		this.priceModel = priceModel;
		// TODO Auto-generated constructor stub
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
