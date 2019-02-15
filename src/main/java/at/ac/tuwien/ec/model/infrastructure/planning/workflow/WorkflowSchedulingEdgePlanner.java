package at.ac.tuwien.ec.model.infrastructure.planning.workflow;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.availability.AvailabilityModel;
import at.ac.tuwien.ec.model.availability.ConstantAvailabilityModel;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.pricing.EdgePricingModel;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.sleipnir.fgcs.FGCSSetup;

public class WorkflowSchedulingEdgePlanner {

	protected static int MAP_M = FGCSSetup.MAP_M;
	protected static int MAP_N = FGCSSetup.MAP_N;
	protected static HardwareCapabilities defaultHardwareCapabilities = FGCSSetup.defaultEdgeNodeCapabilities.clone();
	protected static EdgePricingModel defaultEdgePricingModel = FGCSSetup.edgePricingModel;
	protected static CPUEnergyModel defaultCPUEnergyModel = FGCSSetup.edgeCPUEnergyModel;
	protected static double[] edgeMips = {15000, 10000, 12000, 16000, 13000, 8000};
	protected static double[] edgeNodeAvail = {0.9795, 0.9995, 0.9799, 0.9843, 0.9595, 0.99191};
	public static void setupEdgeNodes(MobileCloudInfrastructure inf) 
	{
		int k = 0;
		//for(int i = 0; i < MAP_M; i++)
			//for(int j = 0; j < MAP_N*2; j++)
			{
				Coordinates edgeNodeCoordinates = null;
				//if(i % 2 == 0 && j%2 == 0)
					//edgeNodeCoordinates = new Coordinates(i,j);
				//else if(i%2==1 && j%2==1)
					//edgeNodeCoordinates = new Coordinates(i,j);
				//if(edgeNodeCoordinates != null)
				//{
				for(int i = 0; i < 6; i++) 
				{
					int j = i;
					edgeNodeCoordinates = new Coordinates(i,j);
					HardwareCapabilities nodeCapabilities = defaultHardwareCapabilities.clone();
					nodeCapabilities.setMipsPerCore(edgeMips[k]);
					EdgeNode edge = new EdgeNode("edge("+i+","+j+")", defaultHardwareCapabilities.clone(), defaultEdgePricingModel);
					edge.setCoords(edgeNodeCoordinates);
					edge.setCPUEnergyModel(defaultCPUEnergyModel);
					AvailabilityModel model = new ConstantAvailabilityModel(edgeNodeAvail[k]);
					edge.setAvailabilityModel(model);
					inf.addEdgeNode(edge);
					k++;
				}
			}
	}
}
