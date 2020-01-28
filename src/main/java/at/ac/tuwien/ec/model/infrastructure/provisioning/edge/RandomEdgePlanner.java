package at.ac.tuwien.ec.model.infrastructure.provisioning.edge;

import org.apache.commons.lang.math.RandomUtils;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;

public class RandomEdgePlanner extends EdgePlanner{
		
	public static void setupEdgeNodes(MobileCloudInfrastructure inf) 
	{
		for(int i = 0; i < MAP_M; i++)
			for(int j = 0; j < MAP_N; j++)
			{
				Coordinates currentEdgeNodeCoordinates = null;
				if(RandomUtils.nextBoolean())
				{
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
			}
	}

}
