package at.ac.tuwien.ec.scheduling.algorithms.heuristics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.algorithms.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;


public abstract class HeuristicResearch extends OffloadScheduler {
	
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

	public abstract ComputationalNode findTarget(OffloadScheduling scheduling, MobileSoftwareComponent toSchedule);
	
	

}
