package at.ac.tuwien.ec.scheduling.offloading.algorithms.heuristics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileApplication;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;
import at.ac.tuwien.ec.scheduling.simulation.SimIteration;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import scala.Tuple2;


public class WeightedFunctionResearch extends HeuristicResearch {
		
	private static final double gamma = SimulationSetup.EchoGamma;
	private static final double alpha = SimulationSetup.EchoAlpha;
	private static final double beta = SimulationSetup.EchoBeta;

	public WeightedFunctionResearch(MobileApplication A, MobileCloudInfrastructure I) {
		super();
		setMobileApplication(A);
		setInfrastructure(I);
		// TODO Auto-generated constructor stub
	}
	
	public WeightedFunctionResearch(Tuple2<MobileApplication,MobileCloudInfrastructure> t) {
		super();
		setMobileApplication(t._1);
		setInfrastructure(t._2);
		// TODO Auto-generated constructor stub
	}

	

	private double computeScore(SoftwareComponent s, ComputationalNode cn, MobileCloudInfrastructure i, double minRuntime, double minCost, double maxBattery) {
		//if(cn.isMobile())
		//	return Double.MAX_VALUE;
		double currRuntime = s.getRuntimeOnNode(cn, currentInfrastructure);
		double currCost = cn.computeCost(s, null, currentInfrastructure);
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

	@Override
	public ComputationalNode findTarget(OffloadScheduling deployment, MobileSoftwareComponent msc) {
		//if (K.get(msc.getId()) != null) 
		//{
			if(!msc.isOffloadable())
				if(isValid(deployment,msc,currentInfrastructure.getMobileDevices().get(msc.getUserId())))
					return currentInfrastructure.getMobileDevices().get(msc.getUserId());
				else
					return null;
			
			
			ArrayList<ComputationalNode> validNodes = new ArrayList<ComputationalNode>();
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
					double tmpCost = cn.computeCost(msc, null, currentInfrastructure);
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

}
