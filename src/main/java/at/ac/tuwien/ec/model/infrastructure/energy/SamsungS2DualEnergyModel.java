package at.ac.tuwien.ec.model.infrastructure.energy;

import java.io.Serializable;

import at.ac.tuwien.ec.blockchain.Transaction;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class SamsungS2DualEnergyModel implements CPUEnergyModel,Serializable {

	private final double BETA_1CORE = 6.93205;
	private final double BETA_2CORE = 11.248;
	//PLEASE REMEMBER: THOSE ARE MICROWATT!!!!
	private final double BETAFREQBASE_1CORE = 625.25e-6;
	private final double BETAFREQBASE_2CORE = 597.79e-6;
	public double computeCPUEnergy(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		//In this version, we assume that mobile is at maximum frequency per core, 1200MHz
		double utilization = s.getHardwareRequirements().getCores() / n.getCapabilities().getMaxCores();
		double power = BETA_1CORE * utilization + BETAFREQBASE_1CORE;
		double runtime = s.getRuntimeOnNode(n, i);
		return power * runtime;
	}
	
	public double computeCPUEnergy(SoftwareComponent s,ComputationalNode m, ComputationalNode n, MobileCloudInfrastructure i) {
		//In this version, we assume that mobile is at maximum frequency per core, 1200MHz
		double utilization = s.getHardwareRequirements().getCores() / n.getCapabilities().getMaxCores();
		double power = BETA_1CORE * utilization + BETAFREQBASE_1CORE;
		double runtime = s.getRuntimeOnNode(m,n,i);
		return power * runtime;
	}
	public double getIdlePower(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double computeQuantileCPUEnergy(Transaction s, ComputationalNode m, ComputationalNode n,
			MobileCloudInfrastructure i) {
		double utilization = s.getHardwareRequirements().getCores() / n.getCapabilities().getMaxCores();
		double power = BETA_1CORE * utilization + BETAFREQBASE_1CORE;
		double runtime = s.getQuantileRuntimeOnNode(m,n,i);
		return power * runtime;
	}

}
