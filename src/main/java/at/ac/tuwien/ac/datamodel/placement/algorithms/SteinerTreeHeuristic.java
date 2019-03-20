package at.ac.tuwien.ac.datamodel.placement.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ac.datamodel.placement.DataPlacement;
import at.ac.tuwien.ac.datamodel.placement.algorithms.vmplanner.VMPlanner;
import at.ac.tuwien.ec.model.Scheduling;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;
import at.ac.tuwien.ec.model.infrastructure.network.ConnectionMap;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import scala.Tuple2;

public class SteinerTreeHeuristic extends DataPlacementAlgorithm{

	/**
	 * 
	 */
	private static final long serialVersionUID = -896252400024798173L;

	public SteinerTreeHeuristic(ArrayList<DataEntry> dataEntries, MobileDataDistributionInfrastructure inf)
	{
		setInfrastructure(inf);
		this.dataEntries = dataEntries;
	}
	
	public SteinerTreeHeuristic(Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure> arg)
	{
		setInfrastructure(arg._2);
		this.dataEntries = arg._1;
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<DataPlacement> dataPlacements = new ArrayList<DataPlacement>();
		DataPlacement dp = new DataPlacement();
		dp.setCurrentInfrastructure((MobileDataDistributionInfrastructure) this.currentInfrastructure);
		MobileDataDistributionInfrastructure mddi = (MobileDataDistributionInfrastructure) this.currentInfrastructure;
		mddi.setEdgeWeights();
		HashMap<String, ArrayList<MobileDevice>> registry = mddi.getRegistry();
		ConnectionMap subMddi = new ConnectionMap(NetworkConnection.class);
		LinkedHashMap<String,Integer> scoreNodeMap = new LinkedHashMap<String,Integer>();
		ArrayList<ComputationalNode> bestTargets = new ArrayList<ComputationalNode>();
		for(DataEntry d: this.dataEntries)
		{
			//get data entry publisher
			NetworkedNode source = mddi.getNodeById(d.getIotDeviceId());
			//get terminal nodes
			ArrayList<MobileDevice> devs = registry.get(d.getTopic());
			//calculate the shortest path between each publisher and each subscriber
			for(MobileDevice mDev : devs)
			{
				GraphPath<NetworkedNode, NetworkConnection> minPath 
					= DijkstraShortestPath.findPathBetween(mddi.getConnectionMap(), source, mDev);
				// add all path vertices and edges, avoiding duplicates, and sets up scores for vertices
				if(minPath == null) 
				{
					System.out.println("OH NOOOOOOOOO!");
					return dataPlacements;
				}
				for(NetworkedNode n : minPath.getVertexList())
				{
					if(!subMddi.containsVertex(n))
						subMddi.addVertex(n);
					if(n instanceof CloudDataCenter || n instanceof EdgeNode)
					{
						if(!bestTargets.contains(n))
							bestTargets.add((ComputationalNode) n);
						if(!scoreNodeMap.containsKey(n.getId()))
							scoreNodeMap.put(n.getId(), 1);
						else
							scoreNodeMap.replace(n.getId(), scoreNodeMap.get(n.getId()), scoreNodeMap.get(n.getId()) + 1);
						
					}
				}
				for(NetworkConnection nwConn : minPath.getEdgeList())
					if(!subMddi.containsEdge(nwConn))
						subMddi.addEdge(nwConn.getSource(), nwConn.getTarget(), nwConn);
			}
		}
		
		for(DataEntry d: this.dataEntries)
		{
			
			if(registry.containsKey(d.getTopic()))
			{
				ArrayList<MobileDevice> devs = registry.get(d.getTopic());
				for(MobileDevice mDev : devs)
				{
					VMInstance vm = VMPlanner.findExistingVMInstance(d,mDev,(MobileDataDistributionInfrastructure) this.currentInfrastructure);
					if(vm == null)
						vm = VMPlanner.instantiateNewVM(d,mDev,(MobileDataDistributionInfrastructure) this.currentInfrastructure);
					
					ComputationalNode target = null;
					d.setVMInstance(vm);
					double minRt = Double.MAX_VALUE;
					for(ComputationalNode cn : bestTargets)
					{
						IoTDevice iotD = (IoTDevice) mddi.getNodeById(d.getIotDeviceId());
						if(mddi.getConnectionMap().getEdge(iotD,cn) == null)
							continue;
						double tmp = d.getTotalProcessingTime(iotD,
								cn,
								mDev,
								mddi);
						if(tmp < minRt)
						{
							minRt = tmp;
							target = cn;
						}
							
					}
					if(target == null) 
					{
						dp = null;
						break;
					}	
					else 
						deployOnVM(dp, d, (IoTDevice) mddi.getNodeById(d.getIotDeviceId()), target, mDev,vm);
				}
			}
		}
		if(dp != null) {
			dataPlacements.add(dp);
			for(String mId : this.currentInfrastructure.getMobileDevices().keySet()) 
			{
				dp.addVMCost(this.currentInfrastructure.getMobileDevices().get(mId).getLifetime(), mId);
				//System.out.println("Mobile: " + mId + ": " + currentInfrastructure.getMobileDevices().get(mId).getCost());
			}
		}
					
		return dataPlacements;		
	}

}
