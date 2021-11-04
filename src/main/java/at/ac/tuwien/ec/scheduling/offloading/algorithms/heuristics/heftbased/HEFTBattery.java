package at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.heftbased;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import scala.Tuple2;



public class HEFTBattery extends HEFTResearch {
	
	public HEFTBattery(MobileApplication A, MobileCloudInfrastructure I) {
		super(A,I);
	}
	
	public HEFTBattery(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super(t);
	}

	@Override
	public ComputationalNode findTarget(OffloadScheduling scheduling, MobileSoftwareComponent currTask) {
		double maxB = Double.MIN_VALUE;
		ComputationalNode target = null;
		if(!currTask.isOffloadable())
			return currentInfrastructure.getMobileDevices().get(currTask.getUserId());
		for(ComputationalNode cn : currentInfrastructure.getAllNodes())
			if( currentInfrastructure.getMobileDevices().get(currTask.getUserId()).getEnergyBudget() - 
					currentInfrastructure.getMobileDevices().get(currTask.getUserId()).
					getCPUEnergyModel().computeCPUEnergy(currTask,
					currentInfrastructure.getMobileDevices().get(currTask.getUserId()), currentInfrastructure) > maxB
					&&	isValid(scheduling,currTask,cn))
			{
				maxB = currentInfrastructure.getMobileDevices().get(currTask.getUserId()).getEnergyBudget() - 
						currentInfrastructure.getMobileDevices().get(currTask.getUserId()).
						getCPUEnergyModel().computeCPUEnergy(currTask,
						currentInfrastructure.getMobileDevices().get(currTask.getUserId()), currentInfrastructure);
				target = currentInfrastructure.getMobileDevices().get(currTask.getUserId());
			}
		return target;
	}
	
}
