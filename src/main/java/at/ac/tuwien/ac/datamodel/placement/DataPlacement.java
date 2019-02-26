package at.ac.tuwien.ac.datamodel.placement;

import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ec.model.Scheduling;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;

public class DataPlacement extends Scheduling {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2269545400797069724L;
	private double averageLatency;
	int dataEntries;
	
	public DataPlacement()
	{
		averageLatency = 0;
		dataEntries = 0;
	}
	
	public void addEntryLatency(DataEntry entry, IoTDevice dev, ComputationalNode n, MobileDevice mobile, MobileDataDistributionInfrastructure inf)
	{
		dataEntries++;
		double entryLatency = entry.getTotalProcessingTime(dev, n, mobile, inf);
		averageLatency += entryLatency / dataEntries;
	}
	
	public void removeEntryLatency(DataEntry entry, IoTDevice dev, ComputationalNode n, MobileDevice mobile, MobileDataDistributionInfrastructure inf) 
	{
		dataEntries--;
		double entryLatency = entry.getTotalProcessingTime(dev, n, mobile, inf);
		averageLatency -= entryLatency / dataEntries;
	}

	public double getAverageLatency() {
		return averageLatency;
	}

	public void setAverageLatency(double averageLatency) {
		this.averageLatency = averageLatency;
	}
	
	

}
