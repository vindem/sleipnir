package at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased;


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



public class HEFTResearch extends OffloadScheduler {
	
	public HEFTResearch(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		setMobileApplication(A);
		setInfrastructure(I);
		setRank(this.currentApp,this.currentInfrastructure);
	}
	
	public HEFTResearch(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super();
		setMobileApplication(t._1());
		setInfrastructure(t._2());
		setRank(this.currentApp,this.currentInfrastructure);
	}
	
	
	@Override
	public ArrayList<OffloadScheduling> findScheduling() {
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue<MobileSoftwareComponent>(new RuntimeComparator());
		//ArrayList<MobileSoftwareComponent> tasks = new ArrayList<MobileSoftwareComponent>();
		PriorityQueue<MobileSoftwareComponent> tasks = new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator());
		tasks.addAll(currentApp.getTaskDependencies().vertexSet());
		//Collections.sort(tasks, new NodeRankComparator());
		ArrayList<OffloadScheduling> deployments = new ArrayList<OffloadScheduling>();
		
		tasks.addAll(currentApp.getTaskDependencies().vertexSet());
		
		double currentRuntime;
		MobileSoftwareComponent currTask;
		OffloadScheduling scheduling = new OffloadScheduling(); 
		while((currTask = tasks.poll())!=null)
		{
			//currTask = tasks.remove(0);
			
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
			{
				if(isValid(scheduling,currTask,currentInfrastructure.getNodeById(currTask.getUserId())))
				{
					target = currentInfrastructure.getNodeById(currTask.getUserId());
					scheduledNodes.add(currTask);
				}
				else
				{
					if(scheduledNodes.isEmpty())
						target = null;
				}
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
					target = null;
			}
								
		}
		deployments.add(scheduling);
		return deployments;
	}

	private void setRank(MobileApplication A, MobileCloudInfrastructure I)
	{
		for(MobileSoftwareComponent msc : A.getTaskDependencies().vertexSet())
			msc.setVisited(false);
				
		for(MobileSoftwareComponent msc : A.getTaskDependencies().vertexSet())		
			upRank(msc,A.getTaskDependencies(),I);
				
	}

	private double upRank(MobileSoftwareComponent msc, DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> dag,
			MobileCloudInfrastructure I) {
		double w_cmp = 0.0;
		if(!msc.isVisited())
		{
			msc.setVisited(true);
			int numberOfNodes = I.getAllNodes().size() + 1;
			for(ComputationalNode cn : I.getAllNodes())
				w_cmp += msc.getLocalRuntimeOnNode(cn, I);
			w_cmp += msc.getLocalRuntimeOnNode(I.getNodeById(msc.getUserId()), I);
			w_cmp = w_cmp / numberOfNodes;
			
			if(dag.outgoingEdgesOf(msc).isEmpty())
				msc.setRank(w_cmp);
			else
			{
								
				double tmpWRank;
				double maxSRank = 0;
				for(ComponentLink neigh : dag.outgoingEdgesOf(msc))
				{
					tmpWRank = upRank(neigh.getTarget(),dag,I);
					double tmpCRank = 0;
					if(neigh.getTarget().isOffloadable())
					{
						for(ComputationalNode cn : I.getAllNodes())
							tmpCRank += I.getTransmissionTime(neigh.getTarget(), I.getNodeById(msc.getUserId()), cn);
						tmpCRank = tmpCRank / (I.getAllNodes().size());
					}
					double tmpRank = tmpWRank + tmpCRank;
					maxSRank = (tmpRank > maxSRank)? tmpRank : maxSRank;
				}
				msc.setRank(w_cmp + maxSRank);
			}
		}
		return msc.getRank();
	}
	
}
