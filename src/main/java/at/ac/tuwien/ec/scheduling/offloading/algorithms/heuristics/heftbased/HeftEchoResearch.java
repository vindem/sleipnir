package at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics.heftbased;

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
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduler;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.offloading.algorithms.heftbased.utils.NodeRankComparator;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;
import at.ac.tuwien.ec.sleipnir.configurations.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;
import scala.Tuple2;

public class HeftEchoResearch extends HEFTResearch {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1418005931931897471L;
	private static final double gamma = OffloadingSetup.EchoGamma;
	private static final double alpha = OffloadingSetup.EchoAlpha;
	private static final double beta = OffloadingSetup.EchoBeta;
	
	public HeftEchoResearch(MobileApplication A, MobileCloudInfrastructure I) {
		super(A,I);
		
	}
	
	public HeftEchoResearch(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super(t);
	}

	private double computeScore(MobileSoftwareComponent s, ComputationalNode cn, double minRuntime, double minCost, double maxBattery) {
		//if(cn.isMobile())
		//	return Double.MAX_VALUE;
		double currRuntime = s.getRuntimeOnNode(cn, currentInfrastructure);
		double currCost = cn.computeCost(s, currentInfrastructure.getMobileDevices().get(s.getUserId()), currentInfrastructure);
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
				double tmpScore = computeScore(msc,n,minRuntime,minCost,maxBattery);
				if(minScore > tmpScore)
				{
					target = n;
					minScore = tmpScore;
				}
			}
			return target;
		
	}
	
}
