package at.ac.tuwien.ec.model.infrastructure.planning.edge;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class EdgeAllCellPlanner extends EdgePlanner {
	
	
	public static void setupEdgeNodes(MobileCloudInfrastructure inf) 
	{
		double size_x = SimulationSetup.x_max/MAP_M;
		double size_y = SimulationSetup.y_max/(MAP_N*2);
		
		for(int i = 0; i < MAP_M; i++)
			for(int j = 0; j < MAP_N*2; j++)
			{
				Coordinates edgeNodeCoordinates = null;
				if(i % 2 == 0 && j%2 == 0 || i%2==1 && j%2==1)
				{
					double x = i*size_x + size_x/2.0;
					double y = j*size_y + size_y/2.0;
					edgeNodeCoordinates = new Coordinates(x,y);
					EdgeNode edge = new EdgeNode("edge("+i+","+j+")", defaultHardwareCapabilities.clone(), defaultEdgePricingModel);
					edge.setCoords(edgeNodeCoordinates);
					edge.setCPUEnergyModel(defaultCPUEnergyModel);
					inf.addEdgeNode(edge);
				}
			}
	}

}
