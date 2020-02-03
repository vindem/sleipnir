package at.ac.tuwien.ec.model.infrastructure.provisioning.edge;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.pricing.EdgePricingModel;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class EdgePlanner {
	
	protected static int MAP_M = SimulationSetup.MAP_M;
	protected static int MAP_N = SimulationSetup.MAP_N;
	protected static HardwareCapabilities defaultHardwareCapabilities = SimulationSetup.defaultEdgeNodeCapabilities.clone();
	protected static EdgePricingModel defaultEdgePricingModel = SimulationSetup.edgePricingModel;
	protected static CPUEnergyModel defaultCPUEnergyModel = SimulationSetup.edgeCPUEnergyModel;
	
	public static void addEdgeNodeAt(MobileCloudInfrastructure inf, int i, int j)
	{
		Coordinates currentEdgeNodeCoordinates = null;
		if(i % 2 == 0 && j%2 == 0)
			currentEdgeNodeCoordinates =  new Coordinates(i,j);
		if(i%2==1 && j%2==1)
			currentEdgeNodeCoordinates =  new Coordinates(i,j);
		if(currentEdgeNodeCoordinates != null)
		{
			EdgeNode edge = new EdgeNode("edge("+i+","+j+")", defaultHardwareCapabilities.clone(), defaultEdgePricingModel);
			edge.setCoords(currentEdgeNodeCoordinates);
			edge.setCPUEnergyModel(defaultCPUEnergyModel);
			inf.addEdgeNode(edge);
		}
	}
	
	public static void removeEdgeNodeAt(MobileCloudInfrastructure inf, int i, int j)
	{
		inf.removeEdgeNodeAt(i, j);
	}

}
