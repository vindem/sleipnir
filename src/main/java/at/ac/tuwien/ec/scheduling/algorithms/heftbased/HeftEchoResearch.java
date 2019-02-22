package at.ac.tuwien.ec.scheduling.algorithms.heftbased;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.jgrapht.graph.DirectedAcyclicGraph;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.ComponentLink;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.algorithms.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;

public class HeftEchoResearch extends OffloadScheduler {

	private static final double gamma = SimulationSetup.EchoGamma;
	private static final double alpha = SimulationSetup.EchoAlpha;
	private static final double beta = SimulationSetup.EchoBeta;
	
	public HeftEchoResearch(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		setMobileApplication(A);
		setInfrastructure(I);
		setRank(this.currentApp,this.currentInfrastructure);
	}
	
	public HeftEchoResearch(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super();
		setMobileApplication(t._1());
		setInfrastructure(t._2());
		setRank(this.currentApp,this.currentInfrastructure);
	}

	private double computeScore(MobileSoftwareComponent s, ComputationalNode cn, double minRuntime, double minCost, double maxBattery) {
		//if(cn.isMobile())
		//	return Double.MAX_VALUE;
		double currRuntime = s.getRuntimeOnNode(cn, currentInfrastructure);
		double currCost = cn.computeCost(s, currentInfrastructure);
		double currBattery = ((MobileDevice)currentInfrastructure.getNodeById(s.getUserId())).getEnergyBudget() - 
				((currentInfrastructure.getMobileDevices().containsValue(cn))? 
						cn.getCPUEnergyModel().computeCPUEnergy(s, cn, currentInfrastructure) :
						currentInfrastructure.getNodeById(s.getUserId()).
							getNetEnergyModel().computeNETEnergy(s, cn, currentInfrastructure));
		
		double runtimeDiff = Math.pow(currRuntime - minRuntime,2.0);
		double costDiff = Math.pow(currCost - minCost,2.0);
		double batteryDiff = Math.pow(maxBattery - currBattery,2.0);
				
		return alpha * adjust(runtimeDiff,1.0)  + beta * adjust(costDiff,1.0) + gamma * adjust(batteryDiff,1.0);
	}

	private double normalized(double x, double minRange, double maxRange) {
		return (x - minRange) / (maxRange - minRange) ;
	}
	
	private double adjust(double x, double factor){
		return x * factor;
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
				((ComputationalNode) scheduling.get(firstTaskToTerminate)).undeploy(firstTaskToTerminate);
				//scheduledNodes.remove(firstTaskToTerminate);
			}
			double minRuntime = Double.MAX_VALUE;
			double minCost = Double.MAX_VALUE;
			double maxBattery = Double.MIN_VALUE;
			
			ComputationalNode target = null;
			if(!currTask.isOffloadable())
			{
				if(isValid(scheduling,currTask,(ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId())))
				{
					target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
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
				// Computing reference values that will be used for score
				for(ComputationalNode cn : currentInfrastructure.getAllNodes())
					if(isValid(scheduling,currTask,cn))
					{
						double tmpRuntime = maxP + currTask.getRuntimeOnNode(cn, currentInfrastructure);
						double tmpCost = cn.computeCost(currTask, currentInfrastructure);
						double tmpBattery = currentInfrastructure.getMobileDevices().get(currTask.getUserId()).getEnergyBudget() -
								currentInfrastructure.getMobileDevices().get(currTask.getUserId()).getNetEnergyModel().computeNETEnergy(currTask, cn, currentInfrastructure);
					
						if(tmpRuntime < minRuntime)
							minRuntime = tmpRuntime;
						if(tmpCost < minCost)
							minCost = tmpCost;
						if(tmpBattery > maxBattery)
							maxBattery = tmpBattery;
					}
				
				if(isValid(scheduling,currTask, (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId())))
				{
					double tmpRuntime = maxP + currTask.getRuntimeOnNode((ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()), currentInfrastructure);
					double tmpCost = ((ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId())).computeCost(currTask, currentInfrastructure);
					double tmpBattery = currentInfrastructure.getMobileDevices().get(currTask.getUserId()).getEnergyBudget() -
							currentInfrastructure.getMobileDevices().get(currTask.getUserId()).getNetEnergyModel().computeNETEnergy(currTask, (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()), currentInfrastructure);
				
					if(tmpRuntime < minRuntime)
						minRuntime = tmpRuntime;
					if(tmpCost < minCost)
						minCost = tmpCost;
					if(tmpBattery > maxBattery)
						maxBattery = tmpBattery;
				}
				
				//Computing node with minimum score
				double minScore = Double.MAX_VALUE;
				double tmpScore;
				for(ComputationalNode cn : currentInfrastructure.getAllNodes())
					if(isValid(scheduling,currTask,cn) && (tmpScore = computeScore(currTask,cn,minRuntime,minCost,maxBattery)) < minScore )
					{
						minScore = tmpScore;
						target = cn;
					}
				if(isValid(scheduling,currTask,(ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId())) 
						&& (tmpScore = computeScore(currTask,(ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId()),minRuntime,minCost,maxBattery)) < minScore )
				{
					minScore = tmpScore;
					target = (ComputationalNode) currentInfrastructure.getNodeById(currTask.getUserId());
				}
				
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
			w_cmp += msc.getLocalRuntimeOnNode((ComputationalNode) I.getNodeById(msc.getUserId()), I);
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
