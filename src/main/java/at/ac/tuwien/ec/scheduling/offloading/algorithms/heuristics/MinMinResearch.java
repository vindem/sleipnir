package at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import scala.Tuple2;

public class MinMinResearch extends OffloadScheduler {
	
	public MinMinResearch(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		setMobileApplication(A);
		setInfrastructure(I);
	}
	
	public MinMinResearch(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super();
		setMobileApplication(t._1);
		setInfrastructure(t._2);
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
			double currRuntime = Double.MAX_VALUE;
			for(ComputationalNode cn : currentInfrastructure.getAllNodes())
			{
				if(!isValid(deployment,msc,cn))
					continue;
				else
				{
					if(msc.getRuntimeOnNode(cn, currentInfrastructure) < currRuntime)
					{
						target = cn;
						currRuntime = msc.getRuntimeOnNode(cn, currentInfrastructure);
					}
				}
			}
		}
		return target;
	}

	@Override
	public ArrayList<OffloadScheduling> findScheduling() {
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue<MobileSoftwareComponent>(new RuntimeComparator());
		HashMap<ComputationalNode,MobileSoftwareComponent> partialDeploy 
		= new HashMap<ComputationalNode,MobileSoftwareComponent>();
		OffloadScheduling scheduling = new OffloadScheduling();
		ArrayList<OffloadScheduling> schedules = new ArrayList<OffloadScheduling>();
		
		
		double currentRuntime = 0;
		int totalTaskNum = currentApp.getComponentNum();
		boolean progress = true;
		while(scheduling.size() < totalTaskNum && progress){
			
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
					scheduling = null;
					progress = false;
				}
			else
				continue;
		
			while(!readyTasks.isEmpty())
			{
				MobileSoftwareComponent toSchedule = (MobileSoftwareComponent) readyTasks.get(0);
				ComputationalNode bestTarget = findTarget(scheduling,toSchedule);
				double minminruntime = (bestTarget == null)? Double.MAX_VALUE : toSchedule.getRuntimeOnNode(bestTarget, currentInfrastructure);
				for(int i = 1; i < readyTasks.size(); i++) {
					MobileSoftwareComponent msc = (MobileSoftwareComponent) readyTasks.get(i);
					ComputationalNode target = findTarget(scheduling,msc);
					if(target == null)
						continue;
					double tmpRuntime = toSchedule.getRuntimeOnNode(bestTarget, currentInfrastructure);
					if(tmpRuntime < minminruntime)
					{
						bestTarget = target;
						toSchedule = msc;
					}

				}
				if(bestTarget == null)
					break;
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
