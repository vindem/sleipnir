package at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.heftbased.HEFTResearch;
import at.ac.tuwien.ec.scheduling.simulation.SimIteration;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;


public class WeightedFunctionResearch extends OffloadScheduler {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 8344010701580886805L;
	private static final double gamma = OffloadingSetup.EchoGamma;
	private static final double alpha = OffloadingSetup.EchoAlpha;
	private static final double beta = OffloadingSetup.EchoBeta;

	public WeightedFunctionResearch(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		setMobileApplication(A);
		setInfrastructure(I);
		setRank(this.currentApp,this.currentInfrastructure);
	}
	
	public WeightedFunctionResearch(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super();
		setMobileApplication(t._1());
		setInfrastructure(t._2());
		setRank(this.currentApp,this.currentInfrastructure);
	}

	

	private double computeScore(SoftwareComponent s, ComputationalNode cn, MobileCloudInfrastructure i, double minRuntime, double minCost, double maxBattery) {
		//if(cn.isMobile())
		//	return Double.MAX_VALUE;
		double currRuntime = s.getRuntimeOnNode(cn, currentInfrastructure);
		double currCost = cn.computeCost(s, cn, currentInfrastructure);
		double currBattery = currentInfrastructure.getMobileDevices().get(s.getUserId()).getEnergyBudget() - 
				(currentInfrastructure.getMobileDevices().containsValue(cn)? cn.getCPUEnergyModel().computeCPUEnergy(s, cn, currentInfrastructure) 
				: currentInfrastructure.getMobileDevices().get(s.getUserId()).getNetEnergyModel().computeNETEnergy(s, cn, currentInfrastructure));
		
		double runtimeDiff = currRuntime - minRuntime;
		double costDiff = currCost - minCost;
		double batteryDiff = maxBattery - currBattery;
		double minRange,maxRange;
				
		if(runtimeDiff < costDiff && runtimeDiff < batteryDiff)
			minRange = runtimeDiff;
		
		return alpha * adjust(runtimeDiff,1)  + beta * adjust(costDiff,1.0) + gamma * adjust(batteryDiff,1.0);
	}

	private double normalized(double x, double minRange, double maxRange) {
		return (x - minRange) / (maxRange - minRange) ;
	}
	
	private double adjust(double x, double factor){
		return x * factor;
	}
	
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
				target = findTarget(scheduling,currTask);
				if(target != null)
					tMin = target.getESTforTask(currTask) + currTask.getRuntimeOnNode(target, currentInfrastructure); // Earliest Finish Time  EFT = wij + EST


			}
			/*
			 * We need this check, because there are cases where, even if the task is offloadable, 
			 * local execution is the best option
			 */
			ComputationalNode localDevice = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
			if(localDevice.getESTforTask(currTask) + currTask.getRuntimeOnNode(localDevice, currentInfrastructure) < tMin &&
					isValid(scheduling,currTask,localDevice))
			{
				tMin = localDevice.getESTforTask(currTask) + currTask.getRuntimeOnNode(localDevice, currentInfrastructure); // Earliest Finish Time  EFT = wij + EST
				target = localDevice;

			}
			//if scheduling found a target node for the task, it allocates it to the target node
			if(target != null)
			{
				deploy(scheduling,currTask,target);
				scheduledNodes.add(currTask);
				tasks.remove(currTask);
			}
			else if(!scheduledNodes.isEmpty())
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

	
	public ComputationalNode findTarget(OffloadScheduling deployment, MobileSoftwareComponent msc) {
		//if (K.get(msc.getId()) != null) 
		//{
			if(!msc.isOffloadable())
				if(isValid(deployment,msc,currentInfrastructure.getMobileDevices().get(msc.getUserId())))
					return currentInfrastructure.getMobileDevices().get(msc.getUserId());
				else
					return null;
			
			
			ComputationalNode target = null;
			double minRuntime = Double.MAX_VALUE;
			double minCost = Double.MAX_VALUE;
			double maxBattery = Double.MIN_VALUE;
			
			for(ComputationalNode cn : currentInfrastructure.getAllNodes())
			{
				if(!isValid(deployment,msc,cn))
					continue;
				else
				{
					double tmpRuntime = msc.getRuntimeOnNode(cn, currentInfrastructure);
					double tmpCost = cn.computeCost(msc, cn, currentInfrastructure);
					double tmpBattery = currentInfrastructure.getMobileDevices().get(msc.getUserId()).getEnergyBudget() - 
							((currentInfrastructure.getMobileDevices().containsValue(cn))? cn.getCPUEnergyModel().computeCPUEnergy(msc, cn, currentInfrastructure) 
									: currentInfrastructure.getMobileDevices().get(msc.getUserId()).getNetEnergyModel().computeNETEnergy(msc, cn, currentInfrastructure));

					if(tmpRuntime < minRuntime)
						minRuntime = tmpRuntime;
					if(tmpCost < minCost)
						minCost = tmpCost;
					if(tmpBattery > maxBattery)
						maxBattery = tmpBattery;
				}
			}
			double minScore = Double.MAX_VALUE;
			for(ComputationalNode n : currentInfrastructure.getAllNodes()){
				double tmpScore = computeScore(msc,n,currentInfrastructure,minRuntime,minCost,maxBattery);
				if(minScore > tmpScore)
				{
					target = n;
					minScore = tmpScore;
				}
			}
			return target;
		
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
