package at.ac.tuwien.ec.scheduling.offloading.pos;

import at.ac.tuwien.ec.blockchain.Transaction;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.CloudDataCenter;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import weka.classifiers.bayes.net.search.global.TabuSearch;

public class ValidationOffloadScheduling extends Scheduling{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7643405510706673949L;
	private double runTime, userCost, batteryLifetime, simTime;
	
	public ValidationOffloadScheduling()
	{
		super();
		batteryLifetime = SimulationSetup.batteryCapacity;
        runTime = 0.0;
        userCost = 0.0;
        simTime = 0.0;
	}
	
	public double getSimTime()
	{
		return simTime;
	}
	
	public void setSimTime(double sTime) 
	{
		this.simTime = sTime;
	}
	
	public void addRuntime(Transaction s, MobileDevice src ,ComputationalNode n, MobileCloudInfrastructure I){
    	src.addTransaction();
		double tmp = s.getRuntimeOnNode(src, n, I);
    	src.addRuntime(tmp);
    	tmp = s.getQuantileRuntimeOnNode(src, n, I);
    	src.addQuantileRuntime(tmp);
	}
    	
    
    public void removeRuntime(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
    	this.runTime -= s.getRuntimeOnNode((ComputationalNode) super.get(s), I);
    }
    
    public void addCost(Transaction s, MobileDevice src ,ComputationalNode n, MobileCloudInfrastructure I) {
        this.userCost += n.computeCost(s, src, I);
        src.addCost(n.computeCost(s, src, I));
    }
    
    public void removeCost(MobileSoftwareComponent s, MobileDevice src, ComputationalNode n, MobileCloudInfrastructure I){
    	this.userCost -= n.computeCost(s, src, I);
    }

    //TODO: consider idle power
	public void addEnergyConsumption(Transaction s, MobileDevice src , ComputationalNode n, MobileCloudInfrastructure i) {
		if(i.getMobileDevices().containsKey(n.getId()))
		{
			double energy = src.getCPUEnergyModel().computeCPUEnergy(s, src, n, i);
			src.removeFromBudget(energy);
			this.batteryLifetime -= energy;
		}
		else
		{
			double offloadEnergy = src.getNetEnergyModel().computeNETEnergy(s, n, i);
			src.removeFromBudget(offloadEnergy);
		//	this.infEnergyConsumption += n.getCPUEnergyModel().computeCPUEnergy(s, n, i);
			this.batteryLifetime -= offloadEnergy;
		}
		
	}
	
	public void addQuantileEnergyConsumption(Transaction s, MobileDevice src , ComputationalNode n, MobileCloudInfrastructure i) {
		if(i.getMobileDevices().containsKey(n.getId()))
		{
			double energy = src.getCPUEnergyModel().computeQuantileCPUEnergy(s, src, n, i);
			src.removeFromQuantileBudget(energy);
			this.batteryLifetime -= energy;
		}
		else
		{
			double offloadEnergy = src.getNetEnergyModel().computeQuantileNETEnergy(s, n, i);
			src.removeFromQuantileBudget(offloadEnergy);
		//	this.infEnergyConsumption += n.getCPUEnergyModel().computeCPUEnergy(s, n, i);
			this.batteryLifetime -= offloadEnergy;
		}
		
	}
	
	public void removeEnergyConsumption(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure i) {
		if(i.getMobileDevices().containsKey(n.getId()))
		{
			double energy = n.getCPUEnergyModel().computeCPUEnergy(s, n, i);
			((MobileDevice)i.getNodeById(s.getUserId())).removeFromBudget(energy);
			this.batteryLifetime += energy;
		}
		else
		{
			double offloadEnergy = i.getMobileDevices().get(s.getUserId()).getNetEnergyModel().computeNETEnergy(s, n, i);
			i.getMobileDevices().get(s.getUserId()).removeFromBudget(offloadEnergy);
	//		this.infEnergyConsumption -= n.getCPUEnergyModel().computeCPUEnergy(s, n, i);
			this.batteryLifetime += offloadEnergy;
		}
		
	}

	public double getRunTime() {
		return runTime;
	}

	public void setRunTime(double runTime) {
		this.runTime = runTime;
	}

	public double getUserCost() {
		return userCost;
	}

	public void setUserCost(double userCost) {
		this.userCost = userCost;
	}

	public double getBatteryLifetime() {
		return batteryLifetime;
	}

	public void setBatteryLifetime(double batteryLifetime) {
		this.batteryLifetime = batteryLifetime;
	}

	public void addQuantileCost(Transaction curr, MobileDevice mobileDevice, ComputationalNode computationalNode,
			MobileCloudInfrastructure currentInfrastructure) {
		// TODO Auto-generated method stub
		
	}
	
	
}
