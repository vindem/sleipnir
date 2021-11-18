package at.ac.tuwien.ec.workflow.faas.placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.network.ConnectionMap;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.provisioning.MobilityBasedNetworkPlanner;
import at.ac.tuwien.ec.provisioning.mobile.MobileDevicePlannerWithIoTMobility;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.sleipnir.configurations.IoTFaaSSetup;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;
import scala.Tuple2;

public class OracleFaaSPlacement extends FaaSPlacementAlgorithm {
	
	private ArrayList<IoTDevice> publisherDevices;
	private ArrayList<MobileDevice> subscriberDevices;
	private TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> workflowIterator;

	public OracleFaaSPlacement(FaaSWorkflow A, MobileDataDistributionInfrastructure I)
	{
		super();
		setCurrentWorkflow(A);
		setInfrastructure(I);
		publisherDevices = new ArrayList<IoTDevice>();
		subscriberDevices = new ArrayList<MobileDevice>();
		setupPubSubRegistry();
		this.workflowIterator = new TopologicalOrderIterator<MobileSoftwareComponent,ComponentLink>(getCurrentWorkflow().getTaskDependencies());
	}
	
	public OracleFaaSPlacement(Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure> arg)
	{
		super();
		setCurrentWorkflow(arg._1());
		setInfrastructure(arg._2());
		publisherDevices = new ArrayList<IoTDevice>();
		subscriberDevices = new ArrayList<MobileDevice>();
		setupPubSubRegistry();
		this.workflowIterator = new TopologicalOrderIterator<MobileSoftwareComponent,ComponentLink>(getCurrentWorkflow().getTaskDependencies());
	}

		
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		double startTime = System.currentTimeMillis();
		int currentTimestamp = 0;
		HashMap<String,Tuple2<ComputationalNode,Double>> preComputedSchedule = new HashMap<String,Tuple2<ComputationalNode,Double>>();
		ArrayList<FaaSWorkflowPlacement> schedulings = new ArrayList<FaaSWorkflowPlacement>();

		FaaSWorkflowPlacement scheduling = new FaaSWorkflowPlacement(this.getCurrentWorkflow(),this.getInfrastructure());
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue<MobileSoftwareComponent>(new RuntimeComparator());
		DirectedAcyclicGraph<MobileSoftwareComponent,ComponentLink> schedulingGraph 
		= (DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink>) this.getCurrentWorkflow().getTaskDependencies().clone();
		/*
		 * readyTasks contains tasks that have to be scheduled for execution.
		 * Tasks are selected according to their upRank (at least in HEFT)
		 */
		PriorityQueue<MobileSoftwareComponent> readyTasks = new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator());
		// All tasks in the workflow
		ArrayList<MobileSoftwareComponent> applicationTasks = new ArrayList<MobileSoftwareComponent>(getCurrentWorkflow().getTaskDependencies().vertexSet());
		
		MobileSoftwareComponent currTask = workflowIterator.next();
		readyTasks.addAll(readyTasks(schedulingGraph));
		
		while(!applicationTasks.isEmpty()) {
			
			while((currTask = readyTasks.poll()) !=null)
			{
				
				String nodeIdPrefix = currTask.getId().substring(currTask.getId().indexOf("_"));
				ComputationalNode target = null;
				if(!preComputedSchedule.containsKey(nodeIdPrefix))
				{
					//We did not find a good target for this node type yet, so we compute it now
					Tuple2<ComputationalNode, Double> nodeAndLatency = findTarget(currTask, getInfrastructure(), scheduling);
					preComputedSchedule.put(nodeIdPrefix, nodeAndLatency);
					target = nodeAndLatency._1();
				}
				else 
				{ 
					target = preComputedSchedule.get(nodeIdPrefix)._1();
					//Has the node enough resources?
					if(!isValid(scheduling, currTask, target))
					{
						Tuple2<ComputationalNode, Double> nodeAndLatency = findTarget(currTask, getInfrastructure(), scheduling);
						preComputedSchedule.put(nodeIdPrefix, nodeAndLatency);
						target = nodeAndLatency._1();
					}
				}
				if(updateCondition())
				{
					//We have a node, but will it still be good in a while?
					Tuple2<ComputationalNode,Double> preComputed = preComputedSchedule.get(nodeIdPrefix);
					double currentLatency = preComputed._2();
					ComputationalNode previousTarget = preComputed._1();
					double predictedLatency = predictLatencyAtTimeStep(currTask, previousTarget, getInfrastructure(), scheduling , getCurrentTime() + 2.0);
					if(predictedLatency > currentLatency)
					{
						Tuple2<ComputationalNode, Double> newPair = findTargetForTimestep(currTask, getInfrastructure(), scheduling, getCurrentTime() + 2.0);
						preComputedSchedule.remove(nodeIdPrefix);
						preComputedSchedule.put(nodeIdPrefix,newPair);
					}
				}
				if(target!=null) 
				{
					//System.out.println("Timestep: "+currentTimestamp+ ", target:"+target.getId()+" Utilization: "+target.getChannelUtilization());
					deploy(scheduling,currTask,target,publisherDevices,subscriberDevices);
					applicationTasks.remove(currTask);
					schedulingGraph.removeVertex(currTask);
					readyTasks.remove(currTask);
					scheduledNodes.add(currTask);
					if(!scheduledNodes.isEmpty())
					{
						MobileSoftwareComponent firstScheduled = scheduledNodes.peek();
						while(firstScheduled != null && target.getESTforTask(currTask)>=firstScheduled.getRunTime())
						{
							scheduledNodes.remove(firstScheduled);
							((ComputationalNode) scheduling.get(firstScheduled)).undeploy(firstScheduled);
							firstScheduled = scheduledNodes.peek();
						}
					}
					else if(!scheduledNodes.isEmpty())
					{
						MobileSoftwareComponent firstScheduled = scheduledNodes.peek();
						scheduledNodes.remove(firstScheduled);
						((ComputationalNode) scheduling.get(firstScheduled)).undeploy(firstScheduled);
						firstScheduled = scheduledNodes.peek();
					}
					readyTasks.addAll(readyTasks(schedulingGraph));
					currentTimestamp = (int) Math.round(getCurrentTime());
					mobilityManagement();
				}
		}
		}
		double endTime = System.currentTimeMillis();
		scheduling.setExecutionTime(endTime - startTime);
		schedulings.add(scheduling);
		return schedulings;
	}
	
	private double computeAverageLatency(MobileSoftwareComponent msc, ComputationalNode target, 
			MobileDataDistributionInfrastructure targetInfrastructure, FaaSWorkflowPlacement placement)
	{
		double averageLatency;
		double maxLatency = Double.MIN_VALUE;
		ConnectionMap map = targetInfrastructure.getConnectionMap();
		if(isSource(msc))
		{
			for(IoTDevice publisher : this.publisherDevices)
			{
				double currTTime = map.getDataTransmissionTime(msc.getOutData(),publisher,target);
				if(Double.isFinite(currTTime) && currTTime > maxLatency)
					maxLatency = currTTime;
				msc.addInData(publisher.getOutData());
			}
			
			averageLatency = maxLatency + msc.getLocalRuntimeOnNode(target, targetInfrastructure);
		}
		else if(isSink(msc))
		{
			maxLatency = Double.MIN_VALUE;
			
			for(MobileDevice subscriber : this.subscriberDevices) 
			{
				double currTTime = map.getDataTransmissionTime(msc.getOutData(),target, subscriber);
				if(Double.isFinite(currTTime) && currTTime > maxLatency)
					maxLatency = currTTime;
			}
			averageLatency = maxLatency + msc.getLocalRuntimeOnNode(target, targetInfrastructure);
		}
		else
		{
			double predTime = Double.MIN_VALUE;
			NetworkedNode maxTrg = null;
			for(MobileSoftwareComponent pred : getCurrentWorkflow().getPredecessors(msc))
			{
				NetworkedNode prevTarget = placement.get(pred);
				double currTime = map.getDataTransmissionTime(msc.getOutData(),prevTarget,target) + pred.getRunTime();
				if(Double.isFinite(currTime) && currTime > predTime) 
				{
					predTime = currTime;
					maxTrg = prevTarget;
				}
			}
			averageLatency = 
					predTime + msc.getLocalRuntimeOnNode(target, targetInfrastructure);
		}
		return averageLatency;
	}

	private double predictLatencyAtTimeStep(MobileSoftwareComponent msc, ComputationalNode previousTarget,
			MobileDataDistributionInfrastructure infrastructure, FaaSWorkflowPlacement placement, double d) {
		MobileDataDistributionInfrastructure future = infrastructure.lookupAtTimestamp(d);
		double predictedLatency = computeAverageLatency(msc, previousTarget, future, placement);
		return predictedLatency;
	}

	private Tuple2<ComputationalNode, Double> findTarget(MobileSoftwareComponent msc,
			MobileDataDistributionInfrastructure infrastructure, FaaSWorkflowPlacement placement) {
		// TODO Auto-generated method stub
		return findTargetForTimestep(msc, infrastructure, placement, getCurrentTime());
	}

	private Tuple2<ComputationalNode, Double> findTargetForTimestep(MobileSoftwareComponent msc,
			MobileDataDistributionInfrastructure infrastructure, FaaSWorkflowPlacement placement, double d) {
		Tuple2<ComputationalNode, Double> out = null; 
		double avgLatency = Double.MAX_VALUE, tmp;
		
		MobileDataDistributionInfrastructure future = infrastructure.lookupAtTimestamp(d);
		for(ComputationalNode c : future.getAllNodes())
		{
			if((tmp = computeAverageLatency(msc, c, future, placement)) < avgLatency && isValid(placement, msc, c))
			{
				out = new Tuple2<ComputationalNode, Double>(c,tmp);
				avgLatency = tmp;
			}
		}
		return out;
	}

	private void setupPubSubRegistry() {
		MobileDataDistributionInfrastructure currInf = this.getInfrastructure();
		ArrayList<String> activeTopics = new ArrayList<String>();
		for(String topic : currInf.getRegistry().keySet())
		{
			if(!currInf.getRegistry().get(topic).isEmpty())
				activeTopics.add(topic);
		}
		String[] sourceTopics = activeTopics.toArray(new String[0]);
		
		
		Set<String> srcTopicSet = new HashSet<String>(Arrays.asList(sourceTopics));
		
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
	}
	
}
