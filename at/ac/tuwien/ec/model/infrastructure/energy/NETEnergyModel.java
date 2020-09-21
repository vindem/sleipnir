package at.ac.tuwien.ec.model.infrastructure.energy;

import at.ac.tuwien.ec.blockchain.Transaction;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public interface NETEnergyModel {
	
	public double computeNETEnergy(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i);

	public double computeQuantileNETEnergy(Transaction s, ComputationalNode n, MobileCloudInfrastructure i);

}
