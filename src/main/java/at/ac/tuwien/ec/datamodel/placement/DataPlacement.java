package at.ac.tuwien.ec.datamodel.placement;

import java.util.ArrayList;

import javax.swing.plaf.IconUIResource;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.model.Scheduling;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class DataPlacement extends Scheduling {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2269545400797069724L;
	private double maxLatency ,averageLatency, cost;
	public double getCost() {
		return cost;
	}

	private MobileDataDistributionInfrastructure currentInfrastructure;
	
	public MobileDataDistributionInfrastructure getCurrentInfrastructure() {
		return currentInfrastructure;
	}

	public void setCurrentInfrastructure(MobileDataDistributionInfrastructure currentInfrastructure) {
		this.currentInfrastructure = currentInfrastructure;
	}

	public DataPlacement()
	{
		averageLatency = 0.0;
		cost = 0.0;
	}
	
	public void setCost(double cost) {
		this.cost = cost;
	}

	public void addEntryLatency(DataEntry entry, int dataEntries,IoTDevice dev, ComputationalNode n, MobileDevice mobile, MobileDataDistributionInfrastructure inf)
	{
		double entryLatency = entry.getTotalProcessingTime(dev, n, mobile, inf);
		averageLatency += entryLatency / dataEntries;
	}
	
	public void removeEntryLatency(DataEntry entry, int dataEntries, IoTDevice dev, ComputationalNode n, MobileDevice mobile, MobileDataDistributionInfrastructure inf) 
	{
		double entryLatency = entry.getTotalProcessingTime(dev, n, mobile, inf);
		averageLatency -= entryLatency / dataEntries;
	}

	public double getAverageLatency() {
		return averageLatency;
	}

	public void setAverageLatency(double averageLatency) {
		this.averageLatency = averageLatency;
	}

	public void addCost(DataEntry de, ComputationalNode cn) {
		cost += cn.computeCost(de, currentInfrastructure);
	}
	
	public void addVMCost(double lifeTime, String uid){
		double tmpCost = 0.0;
		ArrayList<VMInstance> vmList = currentInfrastructure.getVMAssignment(uid);
		for(VMInstance vm : vmList)
			tmpCost += vm.getPricePerSecond(); 
		MobileDevice dev = (MobileDevice) currentInfrastructure.getNodeById(uid);
		dev.setCost(tmpCost);
		this.cost += tmpCost;
			
	}

	public void setAverageMaxLatency(double d) {
		maxLatency = d;
	}

	public double getMaxLatency() {
		return maxLatency;
	}
	
	

}
