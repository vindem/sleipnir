package at.ac.tuwien.ec.model.pricing;

import java.io.Serializable;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class EdgePricingModel implements PricingModel,Serializable{

	public double computeCost(SoftwareComponent sc, ComputationalNode cn, MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		return 0;
	}

}
