package at.ac.tuwien.ec.scheduling.algorithms.heftbased;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.algorithms.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import scala.Tuple2;



public class HEFTResearch extends OffloadScheduler {
	
	public HEFTResearch(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		setMobileApplication(A);
		setInfrastructure(I);
		setRank(A,I);
	}
	
	public HEFTResearch(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super();
		setMobileApplication(t._1());
		setInfrastructure(t._2());
		setRank(this.currentApp,this.currentInfrastructure);
	}
	
	
	/*public ArrayList<Deployment> searchOnDAG(Deployment deployment){
		ArrayList<MobileSoftwareComponent> scheduledNodes 
		= new ArrayList<MobileSoftwareComponent>();
		PriorityQueue<MobileSoftwareComponent> tasks = 
				new PriorityQueue<MobileSoftwareComponent>(A.S.size(), new NodeRankComparator(A, I));
		ArrayList<Deployment> deployments = new ArrayList<Deployment>();
		
		for(SoftwareComponent sc : A.S)
			tasks.add((MobileSoftwareComponent) sc);
		double currentRuntime = 0.0;
		int scheduledTasks = 0;
		//deploying root node
		//MobileSoftwareComponent root = tasks.poll();
		//deploy(deployment,root,I.getMobileDevice("mobile_0"));
		scheduledTasks++;
		MobileSoftwareComponent currTask;
		while(!(currTask = tasks.poll()).getId().equals("sink")){
			if(!scheduledNodes.isEmpty())
			{
				MobileSoftwareComponent firstTaskToTerminate = getFirstToTerminate(scheduledNodes);
				currentRuntime = firstTaskToTerminate.getRuntime();
				deployment.get(firstTaskToTerminate).getHardware().undeploy(firstTaskToTerminate.getHardwareRequirements());
				scheduledNodes.remove(firstTaskToTerminate);
				scheduledTasks++;
				//System.out.println("Scheduled nodes: "+scheduledTasks+" out of "+A.S.size());
			}
			double tMin = Double.MAX_VALUE;
			ComputationalNode target = null;
			//boolean canSchedule = true;
			//while(canSchedule && !tasks.isEmpty())
			//{
				//MobileSoftwareComponent currTask = tasks.poll();
				if(currTask.getId().equals("root"))
				//{
					//deploy(deployment,currTask,I.getMobileDevice("mobile_0"));
					continue;
				//}
					
				if(!currTask.isOffloadable())
					if(isValid(deployment, currTask, I.getMobileDevice(currTask.getUid())))
					{
						target = I.getMobileDevice(currTask.getUid());
						scheduledNodes.add(currTask);
						//tasks.remove();
					}
					else
					{
						if(scheduledNodes.isEmpty())
							return null;
					}
				else{
					double maxP = Double.MIN_VALUE;
					for(MobileSoftwareComponent cmp : A.getPredecessors(currTask))
						if(cmp.getRuntime() > maxP)
							maxP = cmp.getRuntime();
					for(CloudDatacentre cdc : I.C.values())
						if(maxP + currTask.getRuntimeOnNode(cdc, I) < tMin
								&& isValid(deployment, currTask, cdc))
						{
							tMin = maxP + currTask.getRuntimeOnNode(cdc, I);
							target = cdc;
						}
					for(FogNode fn : I.F.values())
						if(maxP + currTask.getRuntimeOnNode(fn, I) < tMin
								&& isValid(deployment, currTask, fn))
						{
							tMin = maxP + currTask.getRuntimeOnNode(fn, I);
							target = fn;
						}
					if(maxP + currTask.getRuntimeOnNode(I.getMobileDevice(currTask.getUid()), I) < tMin
							&& isValid(deployment, currTask, I.getMobileDevice(currTask.getUid())))
						target = I.getMobileDevice(currTask.getUid());
				}
				if(target!=null)
				{
					
					deploy(deployment,currTask,target);
					setRunningTime(deployment, (MobileSoftwareComponent)currTask, target);
					scheduledNodes.add(currTask);
					
				}
				else
				{
					if(scheduledNodes.isEmpty())
						return null;
					//else
						//canSchedule = false;
				}
			//}
		}
		//deployment.runTime += currentRuntime;
		while(!scheduledNodes.isEmpty())
		{
			MobileSoftwareComponent firstTaskToTerminate = getFirstToTerminate(scheduledNodes);
			currentRuntime = firstTaskToTerminate.getRuntime();
			deployment.get(firstTaskToTerminate).getHardware().undeploy(firstTaskToTerminate.getHardwareRequirements());
			scheduledNodes.remove(firstTaskToTerminate);
		}
		SimulationConstants.logger.info("Deployment data: runtime=" + deployment.runTime 
				+ " battery lifetime: " + deployment.mobileEnergyBudget
				+ " cost: " + deployment.deploymentMonthlyCost.getCost());
		deployments.add(deployment);
		return deployments;
	}*/
	
	

	@Override
	public ArrayList<OffloadScheduling> findScheduling() {
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue<MobileSoftwareComponent>(new RuntimeComparator());
		PriorityQueue<MobileSoftwareComponent> tasks = 
				new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator());
		ArrayList<OffloadScheduling> deployments = new ArrayList<OffloadScheduling>();
		
		tasks.addAll(currentApp.getTaskDependencies().vertexSet());
		double currentRuntime;
		MobileSoftwareComponent currTask;
		OffloadScheduling scheduling = new OffloadScheduling(); 
		while((currTask = tasks.poll())!=null)
		{
			if(!scheduledNodes.isEmpty())
			{
				MobileSoftwareComponent firstTaskToTerminate = scheduledNodes.remove();
				currentRuntime = firstTaskToTerminate.getRunTime();
				//currentApp.removeEdgesFrom(firstTaskToTerminate);
				//currentApp.removeTask(firstTaskToTerminate);
				scheduling.get(firstTaskToTerminate).undeploy(firstTaskToTerminate);
				//scheduledNodes.remove(firstTaskToTerminate);
			}
			double tMin = Double.MAX_VALUE;
			ComputationalNode target = null;
			if(!currTask.isOffloadable())
				if(isValid(scheduling,currTask,currentInfrastructure.getNodeById(currTask.getUserId())))
				{
					target = currentInfrastructure.getNodeById(currTask.getUserId());
					scheduledNodes.add(currTask);
				}
				else
				{
					if(scheduledNodes.isEmpty())
						return null;
				}
			else
			{
				double maxP = Double.MIN_VALUE;
				for(MobileSoftwareComponent cmp : currentApp.getPredecessors(currTask))
					if(cmp.getRunTime()>maxP)
						maxP = cmp.getRunTime();
				
				for(ComputationalNode cn : currentInfrastructure.getAllNodes())
					if(maxP + currTask.getRuntimeOnNode(cn, currentInfrastructure) < tMin &&
							isValid(scheduling,currTask,cn))
					{
						tMin = maxP + currTask.getRuntimeOnNode(cn, currentInfrastructure);
						target = cn;
					}
				if(maxP + currTask.getRuntimeOnNode(currentInfrastructure.getNodeById(currTask.getUserId()), currentInfrastructure) < tMin
						&& isValid(scheduling,currTask,currentInfrastructure.getNodeById(currTask.getUserId())))
					target = currentInfrastructure.getNodeById(currTask.getUserId());
			}
			if(target != null)
			{
				deploy(scheduling,currTask,target);
				scheduledNodes.add(currTask);
			}
			else
			{
				if(scheduledNodes.isEmpty())
					return null;
			}
								
		}
		deployments.add(scheduling);
		return deployments;
	}

	private void setRank(MobileApplication A, MobileCloudInfrastructure I)
	{
		Graph<MobileSoftwareComponent, ComponentLink> dag = A.getTaskDependencies();
		ArrayList<MobileSoftwareComponent> cmps = new ArrayList<MobileSoftwareComponent>();
		for(MobileSoftwareComponent msc : dag.vertexSet())
			if(dag.outgoingEdgesOf(msc).isEmpty())
				cmps.add(msc);
		
		 for(MobileSoftwareComponent msc : cmps)
			 upRank(msc,dag,I);
	}

	private void upRank(MobileSoftwareComponent msc, Graph<MobileSoftwareComponent, ComponentLink> dag,
			MobileCloudInfrastructure I) {
		double w_cmp = 0.0;
		if(msc.getRank() == Double.MAX_VALUE)
		{
			int numberOfNodes = I.getAllNodes().size() + 1;
			for(ComputationalNode cn : I.getAllNodes())
				w_cmp += msc.getRuntimeOnNode(cn, I);
			w_cmp += msc.getRuntimeOnNode(I.getNodeById(msc.getUserId()), I);
			w_cmp = w_cmp / numberOfNodes;
			
			ArrayList<MobileSoftwareComponent> neighbors = currentApp.getNeighbors(msc);
			if(neighbors.isEmpty())
				msc.setRank(w_cmp);
			else
			{
				for(MobileSoftwareComponent neigh : neighbors)
					upRank(neigh,dag,I);
				
				double tmpWRank;
				double maxSRank = Double.MIN_VALUE;
				for(MobileSoftwareComponent neigh : neighbors)
				{
					tmpWRank = neigh.getRank();
					double tmpCRank = 1.0;
					if(neigh.isOffloadable())
					{
						for(ComputationalNode cn : I.getAllNodes())
							tmpCRank += I.getTransmissionTime(neigh, I.getNodeById(msc.getUserId()), cn);
						tmpCRank = tmpCRank / (I.getAllNodes().size() + 1);
					}
					double tmpRank = tmpWRank + tmpCRank;
					maxSRank = (tmpRank > maxSRank)? tmpCRank : maxSRank;
				}
				msc.setRank(w_cmp + maxSRank);
			}
		}
		
	}
	
}
