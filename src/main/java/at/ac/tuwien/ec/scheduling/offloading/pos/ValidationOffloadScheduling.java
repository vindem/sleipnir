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

public class ValidationOffloadScheduling extends Scheduling{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7643405510706673949L;
	private double runTime, userCost, batteryLifetime;
	
	public ValidationOffloadScheduling()
	{
		super();
		batteryLifetime = SimulationSetup.batteryCapacity;
        runTime = 0.0;
        userCost = 0.0;
	}
	
	public void addRuntime(Transaction s, ComputationalNode n, MobileCloudInfrastructure I){
    	double tmp = s.getRuntimeOnNode(n, s.getOffloadTarget(), I);
    	s.setRunTime(tmp);
    	if(this.runTime < tmp)
    		this.runTime = tmp;
    }
    
    public void removeRuntime(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
    	this.runTime -= s.getRuntimeOnNode((ComputationalNode) super.get(s), I);
    }
    
    public void addCost(Transaction s, ComputationalNode n, MobileCloudInfrastructure I) {
        this.userCost += n.computeCost(s, I);
    }
    
    public void removeCost(MobileSoftwareComponent s, ComputationalNode n, MobileCloudInfrastructure I){
    	this.userCost -= n.computeCost(s, I);
    }

    //TODO: consider idle power
	public void addEnergyConsumption(Transaction s, ComputationalNode n, MobileCloudInfrastructure i) {
		if(i.getMobileDevices().containsKey(n.getId()))
		{
			double energy = n.getCPUEnergyModel().computeCPUEnergy(s, n, i);
			((MobileDevice)i.getNodeById(s.getUserId())).removeFromBudget(energy);
			this.batteryLifetime -= energy;
		}
		else
		{
			double offloadEnergy = i.getMobileDevices().get(s.getUserId()).getNetEnergyModel().computeNETEnergy(s, n, i);
			i.getMobileDevices().get(s.getUserId()).removeFromBudget(offloadEnergy);
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
	
	
}
