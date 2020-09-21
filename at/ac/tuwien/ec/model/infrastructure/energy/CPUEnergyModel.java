package at.ac.tuwien.ec.model.infrastructure.energy;

import at.ac.tuwien.ec.blockchain.Transaction;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.SoftwareComponent;


public interface CPUEnergyModel {

	public double computeCPUEnergy(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i);
	public double computeCPUEnergy(SoftwareComponent s, ComputationalNode m, ComputationalNode n, MobileCloudInfrastructure i);
	public double computeQuantileCPUEnergy(Transaction s, ComputationalNode m, ComputationalNode n, MobileCloudInfrastructure i);
	public double getIdlePower(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i);
	
}
