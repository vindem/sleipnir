package at.ac.tuwien.ec.scheduling.workflow.algorithms;


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
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.scheduling.workflow.WorkflowScheduling;
import scala.Tuple2;



public class HEFTWorkflowScheduler extends WorkflowScheduler {
	
	
	public HEFTWorkflowScheduler(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		setMobileApplication(A);
		setInfrastructure(I);
		setRank(this.currentApp,this.currentInfrastructure);
		//to be changed
	}
	
	public HEFTWorkflowScheduler(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super();
		setMobileApplication(t._1());
		setInfrastructure(t._2());
		setRank(this.currentApp,this.currentInfrastructure);
	}
	
		
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue<MobileSoftwareComponent>(new RuntimeComparator());
		//ArrayList<MobileSoftwareComponent> tasks = new ArrayList<MobileSoftwareComponent>();
		PriorityQueue<MobileSoftwareComponent> tasks = new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator());
		tasks.addAll(currentApp.getTaskDependencies().vertexSet());
		//Collections.sort(tasks, new NodeRankComparator());
		ArrayList<WorkflowScheduling> deployments = new ArrayList<WorkflowScheduling>();

		tasks.addAll(currentApp.getTaskDependencies().vertexSet());

		double currentRuntime = 0.0;
		MobileSoftwareComponent currTask;
		WorkflowScheduling scheduling = new WorkflowScheduling(); 
		while((currTask = tasks.poll())!=null)
		{
			if(!scheduledNodes.isEmpty())
			{
				MobileSoftwareComponent firstTaskToTerminate = scheduledNodes.remove();
				//currentRuntime = firstTaskToTerminate.getRunTime();
				//currentApp.removeEdgesFrom(firstTaskToTerminate);
				//currentApp.removeTask(firstTaskToTerminate);
				scheduling.get(firstTaskToTerminate).undeploy(firstTaskToTerminate);
				//scheduledNodes.remove(firstTaskToTerminate);
			}
			double tMin = Double.MAX_VALUE;
			ComputationalNode pred = currentInfrastructure.getNodeById("entry0"),target = null;
			
			double maxP = 0.0;
			for(MobileSoftwareComponent cmp : currentApp.getPredecessors(currTask))
				if(cmp.getRunTime()>maxP) 
				{
					maxP = cmp.getRunTime();
					//pred = scheduling.get(cmp);
				}					
			if(currTask.getId().startsWith("SOURCE") || currTask.getId().startsWith("SINK"))
			{
				target = currentInfrastructure.getNodeById("entry0");
				currentRuntime = maxP;
				
			}
			else if(currTask.getId().startsWith("BARRIER"))
			{
				for(MobileSoftwareComponent cmp : currentApp.getPredecessors(currTask))
				{
					if(cmp.getRunTime() >= maxP) 
					{
						maxP = cmp.getRunTime() ;
						target = scheduling.get(cmp);
					}
				}
				currentRuntime = maxP;
			}
			else {
				for(ComputationalNode cn : currentInfrastructure.getAllNodes())
					if(maxP + currTask.getRuntimeOnNode(pred, cn,currentInfrastructure) < tMin &&
							isValid(scheduling,currTask,cn))
					{
						tMin = maxP + currTask.getRuntimeOnNode(pred, cn, currentInfrastructure);
						target = cn;
					}
				currentRuntime = tMin;
			}
			if(target != null)
			{
				currTask.setRunTime(currentRuntime);
				deploy(scheduling,currTask,target);
				scheduledNodes.add(currTask);				
			}
			
			if(scheduledNodes.isEmpty())
				target = null;
			
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
					for(ComputationalNode cn0 : I.getAllNodes())
						for(ComputationalNode cn1 : I.getAllNodes())
							tmpCRank += I.getTransmissionTime(neigh.getTarget(),cn0, cn1);
							
					tmpCRank = tmpCRank / (I.getAllNodes().size());
					
					double tmpRank = tmpWRank + tmpCRank;
					maxSRank = (tmpRank > maxSRank)? tmpRank : maxSRank;
				}
				msc.setRank(w_cmp + maxSRank);
			}
		}
		return msc.getRank();
	}
	
}
