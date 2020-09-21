package at.ac.tuwien.ec.datamodel.algorithms.placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.algorithms.selection.ContainerPlanner;
import at.ac.tuwien.ec.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.network.ConnectionMap;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;
import at.ac.tuwien.ec.workflow.faas.placement.FaaSPlacementAlgorithm;
import scala.Tuple2;

public class FFDCPUPlacement extends FaaSPlacementAlgorithm {

	public FFDCPUPlacement(FaaSWorkflow wf, MobileDataDistributionInfrastructure inf)
	{
		setInfrastructure(inf);
		setCurrentWorkflow(wf);	
	}
	
	public FFDCPUPlacement(Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure> arg)
	{
		super();
		setInfrastructure(arg._2);
		setCurrentWorkflow(arg._1());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4207485325424140303L;

	class CPUComparator implements Comparator<ComputationalNode>
	{

		@Override
		public int compare(ComputationalNode o1, ComputationalNode o2) {
			return Double.compare(o2.getMipsPerCore() * o2.getCapabilities().getAvailableCores(),
					o1.getMipsPerCore() * o1.getCapabilities().getAvailableCores());
		}
		
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<FaaSWorkflowPlacement> schedulings = new ArrayList<FaaSWorkflowPlacement>();
		FaaSWorkflowPlacement scheduling = new FaaSWorkflowPlacement(this.getCurrentWorkflow(),this.getInfrastructure());
		
		ArrayList<ComputationalNode> sortedTargets = getInfrastructure().getAllNodes();
		Collections.sort(sortedTargets,new CPUComparator());
		
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> workflowIterator 
		= new TopologicalOrderIterator<MobileSoftwareComponent,ComponentLink>(getCurrentWorkflow().getTaskDependencies());	
		
		FaaSWorkflow faasW = this.getCurrentWorkflow();
		
		String[] sourceTopics = {};
		String[] trgTopics = {};
		ArrayList<IoTDevice> publisherDevices = new ArrayList<IoTDevice>();
		ArrayList<MobileDevice> subscriberDevices = new ArrayList<MobileDevice>();
		Set<String> srcTopicSet = new HashSet<String>(Arrays.asList(sourceTopics));
		
		
		for(IoTDevice iot : getInfrastructure().getIotDevices().values())
		{
			Set<String> iotTopics = new HashSet<String>(Arrays.asList(iot.getTopics()));
			iotTopics.retainAll(srcTopicSet);
			if(!iotTopics.isEmpty())
				publisherDevices.add(iot);
		}
		
		for(String t : trgTopics)
		{
			ArrayList<MobileDevice> subscribers = getInfrastructure().getSubscribedDevices(t);
			if(subscribers != null)
				subscriberDevices.addAll(subscribers);
		}
		
		
		while(workflowIterator.hasNext())
		{
			MobileSoftwareComponent msc = workflowIterator.next();
			double minAvgCost = Double.MAX_VALUE;
			ComputationalNode trg = null;
			for(ComputationalNode cn : sortedTargets)
			{
				if(cn.getCapabilities().supports(msc.getHardwareRequirements()))
				{
					trg = cn;
					break;
				}
				//double avgCost = computeAverageCost(msc, cn, subscriberDevices);
				//if(avgCost < minAvgCost)
				//{
					//minAvgCost = avgCost;
					//trg = cn;
				//}
			}
			deploy(scheduling,msc,trg, publisherDevices, subscriberDevices);
		}
		schedulings.add(scheduling);		
		return schedulings;
		
	}
	
	protected void deploy(FaaSWorkflowPlacement placement, MobileSoftwareComponent msc, ComputationalNode trg, ArrayList<IoTDevice> publisherDevices, ArrayList<MobileDevice> subscriberDevices)
	{
		super.deploy(placement, msc, trg);
		addAverageLatency(placement,msc,trg,publisherDevices,subscriberDevices);
		addCost(placement,msc,trg);
	}
	
	protected void addCost(FaaSWorkflowPlacement placement, MobileSoftwareComponent msc, ComputationalNode trg) {
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
		placement.addCost(msc, trg, getInfrastructure());
	}

	protected void addAverageLatency(FaaSWorkflowPlacement placement, MobileSoftwareComponent msc,
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
	protected double computeTransmissionTime(NetworkedNode src, ComputationalNode trg) {
		ConnectionMap connections = getInfrastructure().getConnectionMap();
		return connections.getDataTransmissionTime(src.getOutData(), src, trg);
	}
	
	private double computeAverageCost(MobileSoftwareComponent msc, ComputationalNode cn,
			ArrayList<MobileDevice> subscriberDevices) {
		double nDevs = subscriberDevices.size();
		double cost = 0.0;
		for(MobileDevice dev : subscriberDevices)
			cost += cn.computeCost(msc, getInfrastructure());
		
		return cost / nDevs;
		
	}

}
