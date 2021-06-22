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
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import scala.Tuple2;

/**
 * OffloadScheduler class that implements the
 * Heterogeneous Earliest-Finish-Time (HEFT) algorithm
 * , a static scheduling heuristic, for efficient application scheduling
 *
 * H. Topcuoglu, S. Hariri and Min-You Wu,
 * "Performance-effective and low-complexity task scheduling for heterogeneous computing,"
 * in IEEE Transactions on Parallel and Distributed Systems, vol. 13, no. 3, pp. 260-274, March 2002, doi: 10.1109/71.993206.
 */

public class HEFTResearch extends OffloadScheduler {
    /**
     *
     * @param A MobileApplication property from  SimIteration
     * @param I MobileCloudInfrastructure property from  SimIteration
     * Constructors set the parameters and calls setRank() to nodes' ranks
     */
	
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

    /**
     * Processor selection phase:
     * select the tasks in order of their priorities and schedule them on its "best" processor,
     * which minimizes task's finish time
     * @return
     */
	@Override
	public ArrayList<? extends OffloadScheduling> findScheduling() {
		double start = System.nanoTime();
		/*scheduledNodes contains the nodes that have been scheduled for execution.
		 * Once nodes are scheduled, they are taken from the PriorityQueue according to their runtime
		 */
		PriorityQueue<MobileSoftwareComponent> scheduledNodes 
		= new PriorityQueue<MobileSoftwareComponent>(new RuntimeComparator());
		/*
		 * tasks contains tasks that have to be scheduled for execution.
		 * Tasks are selected according to their upRank (at least in HEFT)
		 */
		PriorityQueue<MobileSoftwareComponent> tasks = new PriorityQueue<MobileSoftwareComponent>(new NodeRankComparator());
		//To start, we add all nodes in the workflow
		tasks.addAll(currentApp.getTaskDependencies().vertexSet());
		ArrayList<OffloadScheduling> deployments = new ArrayList<OffloadScheduling>();
				
		MobileSoftwareComponent currTask;
		//We initialize a new OffloadScheduling object, modelling the scheduling computer with this algorithm
		OffloadScheduling scheduling = new OffloadScheduling(); 
		//We check until there are nodes available for scheduling
		while((currTask = tasks.peek()) != null)
		{
			double tMin = Double.MAX_VALUE; //Minimum execution time for next task
			ComputationalNode target = null;
			
			/*while(!currentApp.getIncomingEdgesIn(currTask).isEmpty() && !scheduledNodes.isEmpty())
			{
				MobileSoftwareComponent terminated = scheduledNodes.remove();
				((ComputationalNode) scheduling.get(terminated)).undeploy(terminated);
				this.currentApp.removeTask(terminated);
			}*/			
			if(!currTask.isOffloadable())
			{
			    // If task is not offloadable, deploy it in the mobile device (if enough resources are available)
                if(isValid(scheduling,currTask,(ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId())))
                	target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()); 
				
			}
			else
			{	
				//Check for all available Cloud/Edge nodes
				for(ComputationalNode cn : currentInfrastructure.getAllNodes())
					if(currTask.getRuntimeOnNode(cn, currentInfrastructure) < tMin &&
							isValid(scheduling,currTask,cn))
					{
						tMin = cn.getESTforTask(currTask) + currTask.getRuntimeOnNode(cn, currentInfrastructure); // Earliest Finish Time  EFT = wij + EST
						target = cn;
						
					}
				/*
				 * We need this check, because there are cases where, even if the task is offloadable, 
				 * local execution is the best option
				 */
				ComputationalNode localDevice = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
				if(currTask.getRuntimeOnNode(localDevice, currentInfrastructure) < tMin &&
						isValid(scheduling,currTask,localDevice))
				{
					tMin = localDevice.getESTforTask(currTask) + currTask.getRuntimeOnNode(localDevice, currentInfrastructure); // Earliest Finish Time  EFT = wij + EST
					target = localDevice;
					
				}
				
			}
			//if scheduling found a target node for the task, it allocates it to the target node
			if(target != null)
			{
				deploy(scheduling,currTask,target);
				scheduledNodes.add(currTask);
				tasks.remove(currTask);
			}
			else if(!scheduledNodes.isEmpty());
			{
				MobileSoftwareComponent terminated = scheduledNodes.remove();
				((ComputationalNode) scheduling.get(terminated)).undeploy(terminated);
			}
			/*
			 * if simulation considers mobility, perform post-scheduling operations
			 * (default is to update coordinates of mobile devices)
			 */
			if(OffloadingSetup.mobility)
				postTaskScheduling(scheduling);					
		}
		double end = System.nanoTime();
		scheduling.setExecutionTime(end-start);
		deployments.add(scheduling);
		return deployments;
	}

	protected void setRank(MobileApplication A, MobileCloudInfrastructure I)
	{
		for(MobileSoftwareComponent msc : A.getTaskDependencies().vertexSet())
			msc.setVisited(false);
				
		for(MobileSoftwareComponent msc : A.getTaskDependencies().vertexSet())		
			upRank(msc,A.getTaskDependencies(),I);

	}

    /**
     * upRank is the task prioritizing phase of HEFT
     * rank is computed recuversively by traversing the task graph upward
     * @param msc
     * @param dag Mobile Application's DAG
     * @param infrastructure
     * @return the upward rank of msc
     * (which is also the lenght of the critical path (CP) of this task to the exit task)
     */
	private double upRank(MobileSoftwareComponent msc, DirectedAcyclicGraph<MobileSoftwareComponent, ComponentLink> dag,
			MobileCloudInfrastructure infrastructure) {
		double w_cmp = 0.0; // average execution time of task on each processor / node of this component
		if(!msc.isVisited())
        /*  since upward Rank is defined recursively, visited makes sure no extra unnecessary computations are done when
		    calling upRank on all nodes during initialization */
        {
			msc.setVisited(true);
			int numberOfNodes = infrastructure.getAllNodes().size() + 1;
			for(ComputationalNode cn : infrastructure.getAllNodes())
				w_cmp += msc.getLocalRuntimeOnNode(cn, infrastructure);
			
			w_cmp = w_cmp / numberOfNodes;

            double tmpWRank;
            double maxSRank = 0; // max successor rank
            for(ComponentLink neigh : dag.outgoingEdgesOf(msc)) // for the exit task rank=w_cmp
            {
                // rank = w_Cmp +  max(cij + rank(j)    for all j in succ(i)
                // where cij is the average commmunication cost of edge (i, j)
                tmpWRank = upRank(neigh.getTarget(),dag,infrastructure); // succesor's rank
                double tmpCRank = 0;  // this component's average Communication rank
                //We consider only offloadable successors. If a successor is not offloadable, communication cost is 0
                if(neigh.getTarget().isOffloadable()) 
                {
                    for(ComputationalNode cn : infrastructure.getAllNodes())
                        tmpCRank += infrastructure.getTransmissionTime(neigh.getTarget(), infrastructure.getNodeById(msc.getUserId()), cn);
                    tmpCRank = tmpCRank / (infrastructure.getAllNodes().size());
                }
                double tmpRank = tmpWRank + tmpCRank;
                maxSRank = (tmpRank > maxSRank)? tmpRank : maxSRank;
            }
            msc.setRank(w_cmp + maxSRank);
		}
		return msc.getRank();
	}
	
}
