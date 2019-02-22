package at.ac.tuwien.ec.scheduling.algorithms.multiobjective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.math.RandomUtils;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.algorithms.OffloadScheduler;
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


	protected ComputationalNode findTarget(OffloadScheduling deployment, MobileSoftwareComponent msc) {
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
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue(new RuntimeComparator());
		HashMap<ComputationalNode,MobileSoftwareComponent> partialDeploy 
		= new HashMap<ComputationalNode,MobileSoftwareComponent>();
		OffloadScheduling scheduling = new OffloadScheduling();
		ArrayList<OffloadScheduling> schedules = new ArrayList<OffloadScheduling>();
		
		
		double currentRuntime = 0;
		int totalTaskNum = currentApp.getComponentNum();
		boolean progress = true;
		while(scheduling.size() < totalTaskNum && true){
			
			if(!scheduledNodes.isEmpty())
			{
				MobileSoftwareComponent firstTaskToTerminate = scheduledNodes.remove();
				currentRuntime = firstTaskToTerminate.getRunTime();
				currentApp.removeEdgesFrom(firstTaskToTerminate);
				currentApp.removeTask(firstTaskToTerminate);
				((ComputationalNode) scheduling.get(firstTaskToTerminate)).undeploy(firstTaskToTerminate);;
				scheduledNodes.remove(firstTaskToTerminate);
			}
			
			/* scheduledNodes is empty and deployment is not complete
			  implies deployment not possible*/ 
			 ArrayList<MobileSoftwareComponent> readyTasks = currentApp.readyTasks();
			if(readyTasks.isEmpty())
				if(scheduledNodes.isEmpty())
				{
					progress = false;
					scheduling = null;
				}
			else
				continue;
			
			for(int i = 0; i < readyTasks.size(); i++) 
			{
				MobileSoftwareComponent toSchedule = readyTasks.get(i);
				ComputationalNode bestTarget = findTarget(scheduling,toSchedule);
				if(bestTarget == null)
					continue;
				deploy(scheduling,toSchedule,bestTarget);
				partialDeploy.put(bestTarget,toSchedule);
				scheduledNodes.add(toSchedule);
				readyTasks.remove(toSchedule);
			}

		}
		schedules.add(scheduling);
		return schedules;
	}

}
