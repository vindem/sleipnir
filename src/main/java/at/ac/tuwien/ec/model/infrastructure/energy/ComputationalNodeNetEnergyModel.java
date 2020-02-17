package at.ac.tuwien.ec.model.infrastructure.energy;

import java.io.Serializable;

import at.ac.tuwien.ec.blockchain.Transaction;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class ComputationalNodeNetEnergyModel implements NETEnergyModel, Serializable {

	@Override
	public double computeNETEnergy(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		return 10.0;
	}

	@Override
	public double computeQuantileNETEnergy(Transaction s, ComputationalNode n, MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		return 0;
	}

}
