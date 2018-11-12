package at.ac.tuwien.ec.model.infrastructure.energy;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;

public class AMDCPUEnergyModel implements CPUEnergyModel {

	final double LOAD_LEVEL = 0.12;
	final double ALPHA = 5.29;
	final double BETA = 0.68;
	final double Pidle = 501;
	final double Pmax = 840;
	final double Pr = Pmax - Pidle;
	
	public double computeCPUEnergy(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		double load = (double) s.getHardwareRequirements().getCores() / (double) n.getCapabilities().getMaxCores();
		double power = ((load <= LOAD_LEVEL)? ALPHA * Pr * load : 
			BETA * Pr + (1-BETA) * Pr) + Pidle;
		double runtime = s.getRuntimeOnNode(n, i);
		return power*s.getHardwareRequirements().getCores()*runtime 
				+ Pidle * (n.getCapabilities().getAvailableCores() - s.getHardwareRequirements().getCores()) * runtime;
	}

	public double getIdlePower(SoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		return s.getRuntimeOnNode(n, i) * n.getCapabilities().getAvailableCores() * Pidle;
	}

}
