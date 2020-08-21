package at.ac.tuwien.ec.datamodel.algorithms.placement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.traverse.TopologicalOrderIterator;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.algorithms.placement.FFDCPUPlacement.CPUComparator;
import at.ac.tuwien.ec.datamodel.algorithms.selection.ContainerPlanner;
import at.ac.tuwien.ec.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.network.ConnectionMap;
import at.ac.tuwien.ec.model.infrastructure.provisioning.MobilityBasedNetworkPlanner;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;
import at.ac.tuwien.ec.workflow.faas.placement.FaaSPlacementAlgorithm;
import scala.Tuple2;

public class FFDPRODPlacement extends FaaSPlacementAlgorithm {

	public FFDPRODPlacement(FaaSWorkflow wf, MobileDataDistributionInfrastructure inf)
	{
		super();
		setInfrastructure(inf);
		setCurrentWorkflow(wf);	
	}
	
	public FFDPRODPlacement(Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure> arg)
	{
		super();
		setInfrastructure(arg._2);
		setCurrentWorkflow(arg._1());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4207485325424140303L;

	class ProdComparator implements Comparator<ComputationalNode>
	{

		@Override
		public int compare(ComputationalNode o1, ComputationalNode o2) {
			return Double.compare(o2.getMipsPerCore() * o2.getCapabilities().getAvailableCores() * o2.getCapabilities().getMaxRam() * o2.getCapabilities().getMaxStorage(),
					o1.getMipsPerCore() * o1.getCapabilities().getAvailableCores() * o1.getCapabilities().getMaxRam() * o1.getCapabilities().getMaxStorage());
		}
		
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		double startTime = System.currentTimeMillis();
		int currentTimestamp = 0;
		ArrayList<FaaSWorkflowPlacement> schedulings = new ArrayList<FaaSWorkflowPlacement>();
		FaaSWorkflowPlacement scheduling = new FaaSWorkflowPlacement(this.getCurrentWorkflow(),this.getInfrastructure());
		
		ArrayList<ComputationalNode> sortedTargets = new ArrayList<ComputationalNode>();
		sortedTargets.addAll(getInfrastructure().getAllNodes());
		Collections.sort(sortedTargets,new ProdComparator());
		
		TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> workflowIterator 
		= new TopologicalOrderIterator<MobileSoftwareComponent,ComponentLink>(getCurrentWorkflow().getTaskDependencies());	
		
		FaaSWorkflow faasW = this.getCurrentWorkflow();
		
		String[] sourceTopics = faasW.getPublisherTopics();
		String[] trgTopics = faasW.getSubscribersTopic();
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
				if(cn.isCompatible(msc))
				{
					trg=cn;
					break;
				}
			}
			deploy(scheduling,msc,trg, publisherDevices, subscriberDevices);
			currentTimestamp = (int) Math.round(getCurrentTime());
			for(MobileDevice d : this.getInfrastructure().getMobileDevices().values())
				d.updateCoordsWithMobility((double)currentTimestamp);
			MobilityBasedNetworkPlanner.setupMobileConnections(getInfrastructure());
		}
		double endTime = System.currentTimeMillis();
		double time = endTime - startTime;
		scheduling.setExecutionTime(time);
		schedulings.add(scheduling);		
		return schedulings;
		
	}
	
}
