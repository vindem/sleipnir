package at.ac.tuwien.ec.model.infrastructure.energy;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public interface NETEnergyModel {
	
	public double computeNETEnergy(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i);

}
