package at.ac.tuwien.ec.model.pricing;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class CloudFixedPricingModel implements PricingModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6544891264417198511L;

	public double computeCost(SoftwareComponent sc, ComputationalNode cn0 , MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		return sc.getLocalRuntimeOnNode(cn0, i) * sc.getHardwareRequirements().getCores() * 0.5 + sc.getHardwareRequirements().getRam() * 0.3
				+ sc.getHardwareRequirements().getStorage() * 0.2;
	}

	@Override
	public double computeCost(SoftwareComponent sc, ComputationalNode src, ComputationalNode trg,
			MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		System.out.println("No!");
		return 0;
	}


}
