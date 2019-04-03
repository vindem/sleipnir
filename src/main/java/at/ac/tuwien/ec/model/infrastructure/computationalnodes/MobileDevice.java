package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.math.RandomUtils;

import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;


public class MobileDevice extends ComputationalNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6426397070719700535L;
	private double energyBudget = 0.0, cost = 0.0;
	private double lifetime = 24.0;
	private double averageLatency = 0.0;
	private double maxLatency = 0.0;
	
	public double getAverageLatency() {
		return averageLatency / (SimulationSetup.iotDevicesNum * SimulationSetup.dataRate * SimulationSetup.mobileNum);
	}

	
	public MobileDevice(String id, HardwareCapabilities capabilities, double energyBudget) {
		super(id, capabilities);
		this.energyBudget = energyBudget;
	}
	
	public double getEnergyBudget() {
		return energyBudget;
	}

	public void setEnergyBudget(double energyBudget) {
		this.energyBudget = energyBudget;
	}

	public void removeFromBudget(double computeCPUEnergyConsumption) {
		this.energyBudget -= computeCPUEnergyConsumption;
	}

	public void addToBudget(double computeCPUEnergyConsumption) {
		this.energyBudget += computeCPUEnergyConsumption;
		
	}
	
	public String toString(){
		return id + ":" + capabilities;
	}
					
	@Override
	public void sampleNode() {
		int x = RandomUtils.nextInt(SimulationSetup.MAP_M);
		int y = RandomUtils.nextInt(SimulationSetup.MAP_N);;
		this.setCoords(x, y);
	}

	public double computeCost(SoftwareComponent sc, MobileCloudInfrastructure i)
	{
		return 0.0;		
	}

	public void setCost(double tmpCost) {
		this.cost = tmpCost * (averageLatency / (SimulationSetup.iotDevicesNum * SimulationSetup.dataRate * SimulationSetup.mobileNum));		
	}
	
	public double getCost() 
	{
		return this.cost;
	}

	public double getLifetime() {
		// TODO Auto-generated method stub
		return this.lifetime ;
	}

	public void addEntryLatency(DataEntry de, int dataEntries, IoTDevice id, ComputationalNode cn, MobileDevice dev,
			MobileDataDistributionInfrastructure currentInfrastructure) {
		double entryLatency = de.getTotalProcessingTime(id, cn, dev, currentInfrastructure);
		if(entryLatency > maxLatency)
			maxLatency = entryLatency;
		averageLatency += entryLatency; 
	}

	public double getMaxLatency() {
		return maxLatency;
	}
	
	
}
