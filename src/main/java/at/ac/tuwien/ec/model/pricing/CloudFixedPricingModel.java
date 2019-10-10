package at.ac.tuwien.ec.model.pricing;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class CloudFixedPricingModel implements PricingModel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6544891264417198511L;

	public double computeCost(SoftwareComponent sc, ComputationalNode cn0 ,ComputationalNode cn, MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		return sc.getRuntimeOnNode(cn0, cn, i) * sc.getHardwareRequirements().getCores() * 0.03 + sc.getHardwareRequirements().getRam() * 0.0
				+ sc.getHardwareRequirements().getStorage() * 0.01;
	}


}
