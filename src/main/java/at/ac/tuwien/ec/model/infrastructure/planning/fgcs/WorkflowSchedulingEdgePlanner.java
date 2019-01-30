package at.ac.tuwien.ec.model.infrastructure.planning.fgcs;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.pricing.EdgePricingModel;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class WorkflowSchedulingEdgePlanner {

	protected static int MAP_M = SimulationSetup.MAP_M;
	protected static int MAP_N = SimulationSetup.MAP_N;
	protected static HardwareCapabilities defaultHardwareCapabilities = SimulationSetup.defaultEdgeNodeCapabilities.clone();
	protected static EdgePricingModel defaultEdgePricingModel = SimulationSetup.edgePricingModel;
	protected static CPUEnergyModel defaultCPUEnergyModel = SimulationSetup.edgeCPUEnergyModel;
	
	public static void setupEdgeNodes(MobileCloudInfrastructure inf) 
	{
		for(int i = 0; i < MAP_M; i++)
			for(int j = 0; j < MAP_N*2; j++)
			{
				Coordinates edgeNodeCoordinates = null;
				if(i % 2 == 0 && j%2 == 0)
					edgeNodeCoordinates = new Coordinates(i,j);
				if(i%2==1 && j%2==1)
					edgeNodeCoordinates = new Coordinates(i,j);
				if(edgeNodeCoordinates != null)
				{
					EdgeNode edge = new EdgeNode("edge("+i+","+j+")", defaultHardwareCapabilities.clone(), defaultEdgePricingModel);
					edge.setCoords(edgeNodeCoordinates);
					edge.setCPUEnergyModel(defaultCPUEnergyModel);
					inf.addEdgeNode(edge);
				}
			}
	}
}
