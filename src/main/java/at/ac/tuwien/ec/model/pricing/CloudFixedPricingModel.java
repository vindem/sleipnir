package at.ac.tuwien.ec.model.pricing;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class CloudFixedPricingModel implements PricingModel{

	public double computeCost(SoftwareComponent sc, ComputationalNode cn, MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		return sc.getHardwareRequirements().getCores() * 0.03 + sc.getHardwareRequirements().getRam() * 0.02
				+ sc.getHardwareRequirements().getStorage() * 0.01;
	}

}
