package at.ac.tuwien.ec.workflow.faas.placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
import org.jgrapht.alg.shortestpath.GraphMeasurer;
import org.jgrapht.alg.shortestpath.JohnsonShortestPaths;
import org.jgrapht.traverse.TopologicalOrderIterator;

import at.ac.tuwien.ec.datamodel.algorithms.selection.ContainerPlanner;
import at.ac.tuwien.ec.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.network.ConnectionMap;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.provisioning.MobilityBasedNetworkPlanner;
import at.ac.tuwien.ec.provisioning.mobile.MobileDevicePlannerWithMobility;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;
import scala.Tuple2;

public class DealFWPPlacement extends FaaSPlacementAlgorithm {
	
	private ArrayList<IoTDevice> publisherDevices;
	private ArrayList<MobileDevice> subscriberDevices;
	private ArrayList<ComputationalNode> candidateCenters;
	private double currentTime = 0.0;
	TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> workflowIterator;
		
	public DealFWPPlacement(Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure> arg)
	{
		super();
		setCurrentWorkflow(arg._1());
		setInfrastructure(arg._2());
		
		FaaSWorkflow faasW = this.getCurrentWorkflow();
		MobileDataDistributionInfrastructure currInf = this.getInfrastructure();
		ArrayList<String> activeTopics = new ArrayList<String>();
		for(String topic : currInf.getRegistry().keySet())
		{
			if(!currInf.getRegistry().get(topic).isEmpty())
				activeTopics.add(topic);
		}
		String[] sourceTopics = activeTopics.toArray(new String[0]);
		publisherDevices = new ArrayList<IoTDevice>();
		subscriberDevices = new ArrayList<MobileDevice>();
		
		Set<String> srcTopicSet = new HashSet<String>(Arrays.asList(sourceTopics));
		this.workflowIterator = new TopologicalOrderIterator<MobileSoftwareComponent,ComponentLink>(getCurrentWorkflow().getTaskDependencies());
		
		for(IoTDevice iot : currInf.getIotDevices().values())
		{
			Set<String> iotTopics = new HashSet<String>(Arrays.asList(iot.getTopics()));
			iotTopics.retainAll(srcTopicSet);
			if(!iotTopics.isEmpty())
				publisherDevices.add(iot);
		}
		
		for(String t : activeTopics)
		{
			ArrayList<MobileDevice> subscribers = currInf.getSubscribedDevices(t);
			if(subscribers != null)
				subscriberDevices.addAll(subscribers);
		}
		
		ConnectionMap infrastructureMap = (ConnectionMap) getInfrastructure().getConnectionMap().clone();
		extractSubgraph(infrastructureMap,publisherDevices,subscriberDevices);
		candidateCenters = findCenters(infrastructureMap, SimulationSetup.nCenters);
	}
	
	
	class MaxDistanceComparator implements Comparator<ComputationalNode>
	{

		@Override
		public int compare(ComputationalNode o1, ComputationalNode o2) {
			// TODO Auto-generated method stub
			return Double.compare(o1.getMaxDistance(), o2.getMaxDistance());
		}
	}

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1302954506550171766L;

		
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		double startTime = System.currentTimeMillis();
		int currentTimestamp = 0;
		ArrayList<FaaSWorkflowPlacement> schedulings = new ArrayList<FaaSWorkflowPlacement>();
		
		FaaSWorkflowPlacement scheduling = new FaaSWorkflowPlacement(this.getCurrentWorkflow(),this.getInfrastructure());
				
				
		while(workflowIterator.hasNext())
		{
			//Extract subgraph
			if(updateCondition()) 
			{
				MobileDataDistributionInfrastructure currInf = this.getInfrastructure();
				ArrayList<String> activeTopics = new ArrayList<String>();
				for(String topic : currInf.getRegistry().keySet())
				{
					if(!currInf.getRegistry().get(topic).isEmpty())
						activeTopics.add(topic);
				}
				String[] sourceTopics = activeTopics.toArray(new String[0]);
				publisherDevices = new ArrayList<IoTDevice>();
				subscriberDevices = new ArrayList<MobileDevice>();
				
				Set<String> srcTopicSet = new HashSet<String>(Arrays.asList(sourceTopics));
				this.workflowIterator = new TopologicalOrderIterator<MobileSoftwareComponent,ComponentLink>(getCurrentWorkflow().getTaskDependencies());
				
				for(IoTDevice iot : currInf.getIotDevices().values())
				{
					Set<String> iotTopics = new HashSet<String>(Arrays.asList(iot.getTopics()));
					iotTopics.retainAll(srcTopicSet);
					if(!iotTopics.isEmpty())
						publisherDevices.add(iot);
				}
				
				for(String t : activeTopics)
				{
					ArrayList<MobileDevice> subscribers = currInf.getSubscribedDevices(t);
					if(subscribers != null)
						subscriberDevices.addAll(subscribers);
				}
				
				ConnectionMap infrastructureMap = (ConnectionMap) getInfrastructure().getConnectionMap().clone();
				extractSubgraph(infrastructureMap,publisherDevices,subscriberDevices);
				candidateCenters = findCenters(infrastructureMap, SimulationSetup.nCenters);
			}
			//all of this before should be moved in the constructor
			MobileSoftwareComponent msc = workflowIterator.next();
			double minAvgCost = Double.MAX_VALUE;
			ComputationalNode trg = null;
			if(msc.isOffloadable()) 
			{
				for(ComputationalNode cn : candidateCenters)
				{
					double avgCost = computeAverageCost(msc, cn, subscriberDevices);
					if(avgCost < minAvgCost && cn.isCompatible(msc))
					//if(avgCost < minAvgCost)
					{
						minAvgCost = avgCost;
						trg = cn;
					}
				}
			}
			else
			{
				for(ComputationalNode cn : this.getInfrastructure().getCloudNodes().values())
				{
					double avgCost = computeAverageCost(msc, cn, subscriberDevices);
					if(avgCost < minAvgCost && cn.isCompatible(msc))
					//if(avgCost < minAvgCost)
					{
						minAvgCost = avgCost;
						trg = cn;
					}
				}
			}
			if(trg==null)
				return schedulings;
			deploy(scheduling,msc,trg, publisherDevices, subscriberDevices);
			
			currentTimestamp = (int) Math.round(getCurrentTime());
			for(MobileDevice d : this.getInfrastructure().getMobileDevices().values())
				d.updateCoordsWithMobility((double)currentTimestamp);
			MobilityBasedNetworkPlanner.setupMobileConnections(getInfrastructure());
			MobileDevicePlannerWithMobility.updateDeviceSubscriptions(getInfrastructure(),
					SimulationSetup.selectedWorkflow);			
		}
		double endTime = System.currentTimeMillis();
		double time = endTime - startTime;
		scheduling.setExecutionTime(time);
		schedulings.add(scheduling);		
		return schedulings;
	}
	
	private double computeAverageCost(MobileSoftwareComponent msc, ComputationalNode cn,
			ArrayList<MobileDevice> subscriberDevices) {
		double nDevs = subscriberDevices.size();
		double cost = 0.0;
		for(MobileDevice dev : subscriberDevices)
			cost += cn.computeCost(msc, getInfrastructure()) * computeTransmissionTime(dev, cn);
		
		return cost / nDevs;
		
	}

	
		
	private void extractSubgraph(ConnectionMap infrastructureMap, ArrayList<IoTDevice> publishers, ArrayList<MobileDevice> subscribers) 
	{
		MobileDataDistributionInfrastructure currInf = this.getInfrastructure();
		ArrayList<NetworkedNode> vertexList = new ArrayList<NetworkedNode>(infrastructureMap.vertexSet());
		for(NetworkedNode n : vertexList)
		{
			if(!subscribers.contains(n) && !publishers.contains(n) 
					&& !(currInf.getCloudNodes().containsKey(n.getId()) 
							|| currInf.getEdgeNodes().containsKey(n.getId()) ) )
			{
				ArrayList<NetworkConnection> connList = new ArrayList<NetworkConnection>(infrastructureMap.outgoingEdgesOf(n));
				for(NetworkConnection nc : connList) 
					infrastructureMap.removeEdge(nc);
				infrastructureMap.removeVertex(n);
			}	
		}
	}

	private ArrayList<ComputationalNode> findCenters(ConnectionMap infrastructureMap, int nCenters) {
		ArrayList<ComputationalNode> centers = new ArrayList<ComputationalNode>();
		infrastructureMap.setEdgeWeights();
		
		FloydWarshallShortestPaths<NetworkedNode, NetworkConnection> paths 
			= new FloydWarshallShortestPaths<>(infrastructureMap);
		
		//JohnsonShortestPaths<NetworkedNode, NetworkConnection> paths =
			//	new JohnsonShortestPaths<>(infrastructureMap);
		
		ArrayList<NetworkedNode> vertices = new ArrayList<NetworkedNode>();
		vertices.addAll(infrastructureMap.vertexSet());
				
		for(int i = 0; i < vertices.size(); i++)
		{
			NetworkedNode currNode = vertices.get(i);
			if(
					getInfrastructure().getCloudNodes().containsKey(currNode.getId())
					||
					getInfrastructure().getEdgeNodes().containsKey(currNode.getId())
					)
			{	
				double maxDistance = Double.MIN_VALUE;
				for(int j = 0; j < vertices.size(); j++)
				{
					if(i == j)
						continue;
					double dist = paths.getPathWeight(currNode, vertices.get(j));
					if(dist > maxDistance)
						currNode.setMaxDistance(dist);
				}
				centers.add((ComputationalNode) currNode);
			}
		}
		
		Collections.sort(centers, new MaxDistanceComparator());
		ArrayList<ComputationalNode> toReturn = new ArrayList<ComputationalNode>();
		toReturn.addAll(centers.subList(0, Math.min(centers.size(), nCenters)));
		return toReturn;				
	}

	
	
}
