package at.ac.tuwien.ec.workflow.faas.placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.FloydWarshallShortestPaths;
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
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;
import scala.Tuple2;

public class FaaSCostlessPlacement extends FaaSPlacementAlgorithm {
	
	private ArrayList<IoTDevice> publisherDevices;
	private ArrayList<MobileDevice> subscriberDevices;
	private ArrayList<ComputationalNode> candidateCenters;
	private double updateTime;
	
	public FaaSCostlessPlacement(Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure> arg)
	{
		super();
		setCurrentWorkflow(arg._1());
		setInfrastructure(arg._2());
		
		FaaSWorkflow faasW = this.getCurrentWorkflow();
		MobileDataDistributionInfrastructure currInf = this.getInfrastructure();
		String[] sourceTopics = faasW.getPublisherTopics();
		String[] trgTopics = faasW.getSubscribersTopic();
		publisherDevices = new ArrayList<IoTDevice>();
		subscriberDevices = new ArrayList<MobileDevice>();
		
		Set<String> srcTopicSet = new HashSet<String>(Arrays.asList(sourceTopics));
		
		
		for(IoTDevice iot : currInf.getIotDevices().values())
		{
			Set<String> iotTopics = new HashSet<String>(Arrays.asList(iot.getTopics()));
			iotTopics.retainAll(srcTopicSet);
			if(!iotTopics.isEmpty())
				publisherDevices.add(iot);
		}
		
		for(String t : trgTopics)
		{
			ArrayList<MobileDevice> subscribers = currInf.getSubscribedDevices(t);
			if(subscribers != null)
				subscriberDevices.addAll(subscribers);
		}
		
		ConnectionMap infrastructureMap = getInfrastructure().getConnectionMap();
		extractSubgraph(infrastructureMap,publisherDevices,subscriberDevices);
		candidateCenters = findCenters(infrastructureMap, 5);
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
		ArrayList<FaaSWorkflowPlacement> schedulings = new ArrayList<FaaSWorkflowPlacement>();
		
		FaaSWorkflowPlacement scheduling = new FaaSWorkflowPlacement(this.getCurrentWorkflow(),this.getInfrastructure());
				
		//Extract subgraph
		if(updateCondition()) 
		{
			ConnectionMap infrastructureMap = getInfrastructure().getConnectionMap();
			extractSubgraph(infrastructureMap,publisherDevices,subscriberDevices);
			candidateCenters = findCenters(infrastructureMap, 5);
		}
		//for(int i = 0; i < candidateCenters.size(); i++)
			//System.out.println(candidateCenters.get(i).getId()+" ");
		//all of this before should be moved in the constructor
		
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> workflowIterator 
			= new TopologicalOrderIterator<MobileSoftwareComponent,ComponentLink>(getCurrentWorkflow().getTaskDependencies());
		
		while(workflowIterator.hasNext())
		{
			MobileSoftwareComponent msc = workflowIterator.next();
			double minAvgCost = Double.MAX_VALUE;
			ComputationalNode trg = null;
			for(ComputationalNode cn : candidateCenters)
			{
				double avgCost = computeAverageCost(msc, cn, subscriberDevices);
				if(avgCost < minAvgCost)
				{
					minAvgCost = avgCost;
					trg = cn;
				}
			}
			deploy(scheduling,msc,trg, publisherDevices, subscriberDevices);
			updateTime = scheduling.getRunTime();
		}
		double endTime = System.currentTimeMillis();
		double time = endTime - startTime;
		scheduling.setExecutionTime(time);
		schedulings.add(scheduling);		
		return schedulings;
	}
	
	private boolean updateCondition() {
		return updateTime % 144.0 == 0;
	}

	private double computeAverageCost(MobileSoftwareComponent msc, ComputationalNode cn,
			ArrayList<MobileDevice> subscriberDevices) {
		double nDevs = subscriberDevices.size();
		double cost = 0.0;
		for(MobileDevice dev : subscriberDevices)
			cost += cn.computeCost(msc, getInfrastructure());
		
		return cost / nDevs;
		
	}

	protected void deploy(FaaSWorkflowPlacement placement, MobileSoftwareComponent msc, ComputationalNode trg, ArrayList<IoTDevice> publisherDevices, ArrayList<MobileDevice> subscriberDevices)
	{
		super.deploy(placement, msc, trg);
		addAverageLatency(placement,msc,trg,publisherDevices,subscriberDevices);
		addCost(placement,msc,trg);
	}
	
	private void addCost(FaaSWorkflowPlacement placement, MobileSoftwareComponent msc, ComputationalNode trg) {
		double predTime = Double.MIN_VALUE;
		ComputationalNode maxTrg = null;
		for(MobileSoftwareComponent pred : getCurrentWorkflow().getPredecessors(msc))
		{
			ComputationalNode prevTarget = (ComputationalNode) placement.get(pred);
			double currTime = computeTransmissionTime(prevTarget,trg) + pred.getRunTime();
			if(currTime > predTime) 
			{
				predTime = currTime;
				maxTrg = prevTarget;
			}
		}
		placement.addCost(msc, maxTrg, trg, getInfrastructure());
	}

	private void addAverageLatency(FaaSWorkflowPlacement placement, MobileSoftwareComponent msc,
			ComputationalNode trg, ArrayList<IoTDevice> publishers, ArrayList<MobileDevice> subscribers) {
		if(isSource(msc))
		{
			double maxLatency = Double.MIN_VALUE;
			for(IoTDevice publisher : publishers) 
			{
				double currTTime = computeTransmissionTime(publisher,trg);
				if(currTTime > maxLatency)
					maxLatency = currTTime;
				msc.addInData(publisher.getOutData());
			}
			placement.addAverageLatency(maxLatency + msc.getLocalRuntimeOnNode(trg, getInfrastructure()));
			msc.setRunTime(maxLatency + msc.getLocalRuntimeOnNode(trg, getInfrastructure()));
			trg.setOutData(msc.getOutData());
		}
		else if(isSink(msc))
		{
			double maxLatency = Double.MIN_VALUE;
			trg.setOutData(msc.getOutData());
			for(MobileDevice subscriber : subscribers) 
			{
				double currTTime = computeTransmissionTime(trg,subscriber);
				if(currTTime > maxLatency)
					maxLatency = currTTime;
			}
			placement.addAverageLatency(msc.getLocalRuntimeOnNode(trg, getInfrastructure()) + maxLatency);
			msc.setRunTime(msc.getLocalRuntimeOnNode(trg, getInfrastructure()) + maxLatency);
			trg.setOutData(msc.getOutData());
		}
		else
		{
			double predTime = Double.MIN_VALUE;
			NetworkedNode maxTrg = null;
			for(MobileSoftwareComponent pred : getCurrentWorkflow().getPredecessors(msc))
			{
				NetworkedNode prevTarget = placement.get(pred);
				double currTime = computeTransmissionTime(prevTarget,trg) + pred.getRunTime();
				if(currTime > predTime) 
				{
					predTime = currTime;
					maxTrg = prevTarget;
				}
			}
			placement.addAverageLatency(computeTransmissionTime(maxTrg,trg) 
					+ msc.getLocalRuntimeOnNode(trg, getInfrastructure()));
			msc.setRunTime(computeTransmissionTime(maxTrg,trg) 
					+ msc.getLocalRuntimeOnNode(trg, getInfrastructure()));
			trg.setOutData(msc.getOutData());
			
		}
		
	}

	private double computeTransmissionTime(NetworkedNode src, ComputationalNode trg) {
		ConnectionMap connections = getInfrastructure().getConnectionMap();
		return connections.getDataTransmissionTime(src.getOutData(), src, trg);
	}

		
	private void extractSubgraph(ConnectionMap infrastructureMap, ArrayList<IoTDevice> publishers, ArrayList<MobileDevice> subscribers) 
	{
		MobileDataDistributionInfrastructure currInf = this.getInfrastructure();
		for(NetworkedNode n : infrastructureMap.vertexSet())
		{
			if(!subscribers.contains(n) && !publishers.contains(n) 
					&& !(currInf.getCloudNodes().containsKey(n.getId()) 
							|| currInf.getEdgeNodes().containsKey(n.getId()) ) )
			{
				infrastructureMap.removeVertex(n);
				infrastructureMap.removeAllEdges(infrastructureMap.outgoingEdgesOf(n));
			}	
		}
	}

	private ArrayList<ComputationalNode> findCenters(ConnectionMap infrastructureMap, int nCenters) {
		ArrayList<ComputationalNode> centers = new ArrayList<ComputationalNode>();
		infrastructureMap.setCostlessWeights(getInfrastructure());
		
		FloydWarshallShortestPaths<NetworkedNode, NetworkConnection> paths 
			= new FloydWarshallShortestPaths<>(infrastructureMap);
		
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
				if(getInfrastructure().getEdgeNodes().containsValue(currNode))
					centers.add((ComputationalNode) currNode);
			}
		}
		
		Collections.sort(centers, new MaxDistanceComparator());
		ArrayList<ComputationalNode> toReturn = new ArrayList<ComputationalNode>();
		toReturn.addAll(centers.subList(0, nCenters));
		return toReturn;				
	}

	
	
}
