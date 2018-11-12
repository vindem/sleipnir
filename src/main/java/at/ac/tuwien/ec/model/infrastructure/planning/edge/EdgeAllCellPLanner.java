package at.ac.tuwien.ec.model.infrastructure.planning.edge;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;

public class EdgeAllCellPLanner extends EdgePlanner {
	
	
	public static void setupEdgeNodes(MobileCloudInfrastructure inf) 
	{
		for(int i = 0; i < MAP_M; i++)
			for(int j = 0; j < MAP_N; j++)
			{
				Coordinates edgeNodeCoordinates = null;
				if(i % 2 == 0 && j%2 == 0)
					edgeNodeCoordinates = new Coordinates(i,j);
				if(i%2==1 && j%2==1)
					edgeNodeCoordinates = new Coordinates(i,j);
				if(edgeNodeCoordinates != null)
				{
					EdgeNode edge = new EdgeNode("edge("+i+","+j+")", defaultHardwareCapabilities, defaultEdgePricingModel);
					edge.setCoords(edgeNodeCoordinates);
					edge.setCPUEnergyModel(defaultCPUEnergyModel);
					inf.addEdgeNode(edge);
				}
			}
	}

}
