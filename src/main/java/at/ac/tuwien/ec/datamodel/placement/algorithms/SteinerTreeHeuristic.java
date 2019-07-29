package at.ac.tuwien.ec.datamodel.placement.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner.FirstFitDecreasingSizeVMPlanner;
import at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner.VMPlanner;
import at.ac.tuwien.ec.model.Scheduling;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
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
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;

public class SteinerTreeHeuristic extends DataPlacementAlgorithm{

	/**
	 * 
	 */
	private static final long serialVersionUID = -896252400024798173L;
	private MobileDataDistributionInfrastructure mddi;
		

	public SteinerTreeHeuristic(VMPlanner planner,ArrayList<DataEntry> dataEntries, MobileDataDistributionInfrastructure inf)
	{
		super(planner);
		setInfrastructure(inf);
		this.dataEntries = dataEntries;		
	}
	
	public SteinerTreeHeuristic(VMPlanner planner, Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure> arg)
	{
		super(planner);
		setInfrastructure(arg._2);
		this.dataEntries = arg._1;
		
		//computeBestTargets();	
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<DataPlacement> dataPlacements = new ArrayList<DataPlacement>();
		DataPlacement dp = new DataPlacement();
		dp.setCurrentInfrastructure((MobileDataDistributionInfrastructure) this.currentInfrastructure);
		
		for(MobileDevice dev: currentInfrastructure.getMobileDevices().values())
		{
			ArrayList<DataEntry> dataEntriesForDev = filterByDevice(dataEntries, dev);
			ArrayList<VMInstance> instancesPerUser = this.vmPlanner.performVMAllocation(dataEntriesForDev, dev, (MobileDataDistributionInfrastructure) this.currentInfrastructure);
			double timeStep = 0.0;
			int j = 0;
			ArrayList<ComputationalNode> bestTargets = computeBestTargets(dev);
			for(DataEntry de : dataEntriesForDev)
			{
				if(j%(SimulationSetup.dataEntryNum/10)==0) 
					bestTargets = computeBestTargets(dev);
				double minRT = Double.MAX_VALUE;
				ComputationalNode target = null;
				for(ComputationalNode cn : bestTargets)
				{
					IoTDevice iotD = (IoTDevice) mddi.getNodeById(de.getIotDeviceId());
					if(mddi.getConnectionMap().getEdge(iotD,cn) == null)
						continue;
					if(mddi.getConnectionMap().getEdge(iotD, cn).getBandwidth() == 0 ||
							!Double.isFinite(mddi.getConnectionMap().getEdge(iotD, cn).getLatency()))
						continue;
					if(mddi.getConnectionMap().getEdge(cn, dev).getBandwidth() == 0 ||
							!Double.isFinite(mddi.getConnectionMap().getEdge(cn, dev).getLatency()))
						continue;
					if(cn.getCapabilities().supports(de.getVMInstance().getCapabilities().getHardware())) {
						double tmp = de.getTotalProcessingTime(iotD, cn, dev, (MobileDataDistributionInfrastructure) currentInfrastructure);
						if(Double.compare(tmp,minRT) < 0 )
						{
							minRT = tmp;
							target = cn;
						}
					}
				}
				if(target == null) 
				{
					for(ComputationalNode cn : mddi.getAllNodes())
					{
						IoTDevice iotD = (IoTDevice) mddi.getNodeById(de.getIotDeviceId());
						if(mddi.getConnectionMap().getEdge(iotD,cn) == null)
							continue;
						if(mddi.getConnectionMap().getEdge(iotD, cn).getBandwidth() == 0 ||
								!Double.isFinite(mddi.getConnectionMap().getEdge(iotD, cn).getLatency()))
							continue;
						if(mddi.getConnectionMap().getEdge(cn, dev).getBandwidth() == 0 ||
								!Double.isFinite(mddi.getConnectionMap().getEdge(cn, dev).getLatency()))
							continue;
						if(cn.getCapabilities().supports(de.getVMInstance().getCapabilities().getHardware())) {
							double tmp = de.getTotalProcessingTime(iotD, cn, dev, (MobileDataDistributionInfrastructure) currentInfrastructure);
							if(Double.compare(tmp,minRT) < 0 )
							{
								minRT = tmp;
								target = cn;
							}
						}

					}
					
				}
				if( target == null)
					break;
				else 
					deployVM(dp, de, dataEntriesForDev.size() ,(IoTDevice) mddi.getNodeById(de.getIotDeviceId()), target, dev, de.getVMInstance());
				j++;
				if(j%10 == 0) 
				{
					timeStep++;
					dev.updateCoordsWithMobility(timeStep);
				}
				
								
			}
			double vmCost = 0.0;
			for(VMInstance vm : instancesPerUser)
				vmCost += vm.getPricePerSecond(); 
			dev.setCost(vmCost);
		}
		
		
		if(dp != null)
		{
			double avgLat = 0.0,avgCost=0.0,avgMaxLat=0.0;
			for(MobileDevice dev: currentInfrastructure.getMobileDevices().values()) 
			{
				avgMaxLat += dev.getMaxLatency();
				avgLat += dev.getAverageLatency();
				avgCost += dev.getCost();
			}
			dp.setAverageLatency(avgLat / currentInfrastructure.getMobileDevices().size());
			dp.setAverageMaxLatency(avgMaxLat / currentInfrastructure.getMobileDevices().size());
			dp.setCost(avgCost / currentInfrastructure.getMobileDevices().size());
			dataPlacements.add(dp);
		}
			
			

		return dataPlacements;		
	}

	

	protected ArrayList<DataEntry> filterByDevice(ArrayList<DataEntry> dataEntries, MobileDevice dev) {
		ArrayList<DataEntry> filtered = new ArrayList<DataEntry>();
		HashMap<String, ArrayList<MobileDevice>> registry 
			= ((MobileDataDistributionInfrastructure)this.getInfrastructure()).getRegistry();
		for(DataEntry de : dataEntries)
			if(registry.containsKey(de.getTopic()))
				if(registry.get(de.getTopic()).contains(dev))
					filtered.add(de);
				
		return filtered;	
	}
	
	private double norm(VMInstance vmInstance, ComputationalNode cn) {
		return Math.pow((vmInstance.getCapabilities().getAvailableCores() 
				- cn.getCapabilities().getAvailableCores()),2.0);
	}
	
	private ArrayList<ComputationalNode> computeBestTargets(MobileDevice dev)
	{
		this.mddi = (MobileDataDistributionInfrastructure) this.currentInfrastructure;
		mddi.setEdgeWeights();
		HashMap<String, ArrayList<MobileDevice>> registry 
		= ((MobileDataDistributionInfrastructure)this.getInfrastructure()).getRegistry();
		ConnectionMap subMddi = new ConnectionMap(NetworkConnection.class);
		ArrayList<ComputationalNode> bestTargets = new ArrayList<ComputationalNode>();
		for(IoTDevice d: mddi.getIotDevices().values())
		{
			//get data entry publisher
			NetworkedNode source = d;
			//get terminal nodes
			ArrayList<MobileDevice> devs = registry.get(d.getId());
			//calculate the shortest path between each publisher and each subscriber
			if(devs != null)
			{
				GraphPath<NetworkedNode, NetworkConnection> minPath 
				= DijkstraShortestPath.findPathBetween(mddi.getConnectionMap(), source, dev);
				// add all path vertices and edges, avoiding duplicates, and sets up scores for vertices
				
				for(NetworkedNode n : minPath.getVertexList())
				{
					if(!subMddi.containsVertex(n))
						subMddi.addVertex(n);
					if(n instanceof CloudDataCenter || n instanceof EdgeNode)
					{
						if(!bestTargets.contains(n))
							bestTargets.add((ComputationalNode) n);
					}
				}
				for(NetworkConnection nwConn : minPath.getEdgeList())
					if(!subMddi.containsEdge(nwConn))
						subMddi.addEdge(nwConn.getSource(), nwConn.getTarget(), nwConn);
			}
		}
		return bestTargets;
	}

}
