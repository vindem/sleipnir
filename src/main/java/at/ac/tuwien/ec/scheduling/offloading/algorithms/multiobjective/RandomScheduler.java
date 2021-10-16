package at.ac.tuwien.ec.scheduling.offloading.algorithms.multiobjective;

import java.util.ArrayList;
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

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import scala.Tuple2;

public class RandomScheduler extends OffloadScheduler {
	
	public RandomScheduler(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		setMobileApplication(A);
		setInfrastructure(I);
	}
	
	
	public RandomScheduler(Tuple2<MobileApplication, MobileCloudInfrastructure> inputValues) {
		super();
		setMobileApplication(inputValues._1());
		setInfrastructure(inputValues._2());
	}


	public ComputationalNode findTarget(OffloadScheduling deployment, MobileSoftwareComponent msc) {
		ComputationalNode target = null;
		if(!msc.isOffloadable())
		{
			if(isValid(deployment,msc,currentInfrastructure.getMobileDevices().get(msc.getUserId())))
				return currentInfrastructure.getMobileDevices().get(msc.getUserId());
			else
				return null;
		}
		else
		{
			int idx = RandomUtils.nextInt(currentInfrastructure.getCloudNodes().size());
			ArrayList<ComputationalNode> nodes = new ArrayList<ComputationalNode>();
			nodes.addAll(currentInfrastructure.getCloudNodes().values());
			target = nodes.get(idx);
			return target;
		}
	}

	@Override
	public ArrayList<OffloadScheduling> findScheduling() {
		ArrayList<MobileSoftwareComponent> taskList = new ArrayList<MobileSoftwareComponent>();
		DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> deps = this.getMobileApplication().getTaskDependencies();
		ArrayList<OffloadScheduling> schedulings = new ArrayList<OffloadScheduling>();
		OffloadScheduling scheduling = new OffloadScheduling();
		Iterator<MobileSoftwareComponent> it = deps.iterator();
		while(it.hasNext())
			taskList.add(it.next());
		
		for(MobileSoftwareComponent msc : taskList)
		{
			if(!msc.isOffloadable())
				deploy(scheduling,msc,currentInfrastructure.getMobileDevices().get(msc.getUserId()));
			else
			{
				ArrayList<ComputationalNode> possibleTargets = currentInfrastructure.getAllNodes();
				possibleTargets.add(currentInfrastructure.getMobileDevices().get(msc.getUserId()));
				UniformIntegerDistribution uid = new UniformIntegerDistribution(0,possibleTargets.size()-1);
				ComputationalNode target = (ComputationalNode) possibleTargets.toArray()[uid.sample()];
				deploy(scheduling,msc,target);
			}
		}
			
		
		schedulings.add(scheduling);
		return schedulings;
	}

}
