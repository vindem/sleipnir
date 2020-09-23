package at.ac.tuwien.ec.workflow.faas.placement.mo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflow;
import at.ac.tuwien.ec.workflow.faas.FaaSWorkflowPlacement;
import at.ac.tuwien.ec.workflow.faas.placement.FaaSPlacementAlgorithm;
import scala.Tuple2;

public class RandomFaaSScheduler extends FaaSPlacementAlgorithm {
	
	private ArrayList<IoTDevice> publisherDevices;
	private ArrayList<MobileDevice> subscriberDevices;
	TopologicalOrderIterator<MobileSoftwareComponent, ComponentLink> workflowIterator;
	
	public RandomFaaSScheduler(Tuple2<FaaSWorkflow,MobileDataDistributionInfrastructure> arg) {
		setCurrentWorkflow(arg._1());
		setInfrastructure(arg._2());
		
		FaaSWorkflow faasW = this.getCurrentWorkflow();
		MobileDataDistributionInfrastructure currInf = this.getInfrastructure();
		String[] sourceTopics = {};
		String[] trgTopics = {};
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
		
		for(String t : trgTopics)
		{
			ArrayList<MobileDevice> subscribers = currInf.getSubscribedDevices(t);
			if(subscribers != null)
				subscriberDevices.addAll(subscribers);
		}
	}


	protected ComputationalNode findTarget(FaaSWorkflowPlacement deployment, MobileSoftwareComponent msc) {
		ComputationalNode target = null;
		if(!msc.isOffloadable())
		{
			int idx = RandomUtils.nextInt(getInfrastructure().getCloudNodes().size());
			ArrayList<ComputationalNode> nodes = new ArrayList<ComputationalNode>();
			nodes.addAll(getInfrastructure().getCloudNodes().values());
			target = getInfrastructure().getCloudNodes().get(idx);
			return target;
		}
		else
		{
			int idx = RandomUtils.nextInt(getInfrastructure().getAllNodes().size());
			target = getInfrastructure().getAllNodes().get(idx);
			return target;
		}
	}

	
	
	@Override
	public ArrayList<FaaSWorkflowPlacement> findScheduling() {
		ArrayList<MobileSoftwareComponent> taskList = new ArrayList<MobileSoftwareComponent>();
		DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> deps = this.getCurrentWorkflow().getTaskDependencies();
		ArrayList<FaaSWorkflowPlacement> schedulings = new ArrayList<FaaSWorkflowPlacement>();
		FaaSWorkflowPlacement scheduling = new FaaSWorkflowPlacement(this.getCurrentWorkflow(), this.getInfrastructure());
		
		while(workflowIterator.hasNext())
		{
			MobileSoftwareComponent msc = workflowIterator.next();
			ComputationalNode target = findTarget(scheduling,msc);
			deploy(scheduling, msc, target);
		}
		
		schedulings.add(scheduling);
		return schedulings;
	}

}
