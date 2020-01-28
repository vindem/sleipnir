package at.ac.tuwien.ec.model.infrastructure.provisioning.edge;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.pricing.EdgePricingModel;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class EdgePlanner {
	
	protected static int MAP_M = SimulationSetup.MAP_M;
	protected static int MAP_N = SimulationSetup.MAP_N;
	protected static HardwareCapabilities defaultHardwareCapabilities = SimulationSetup.defaultEdgeNodeCapabilities.clone();
	protected static EdgePricingModel defaultEdgePricingModel = SimulationSetup.edgePricingModel;
	protected static CPUEnergyModel defaultCPUEnergyModel = SimulationSetup.edgeCPUEnergyModel;

}
