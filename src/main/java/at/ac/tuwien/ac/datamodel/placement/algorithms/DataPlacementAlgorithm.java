package at.ac.tuwien.ac.datamodel.placement.algorithms;

import java.io.Serializable;
import java.util.ArrayList;

import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ac.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.model.Scheduling;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.scheduling.simulation.SimIteration;

public abstract class DataPlacementAlgorithm extends SimIteration implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4568099240968031102L;
	protected ArrayList<DataEntry> dataEntries;
	
	
	public DataPlacementAlgorithm() 
	{
		super();		
	}
	
	@Override
	public abstract ArrayList<? extends Scheduling> findScheduling();
	
	protected synchronized void deploy(DataPlacement dp, DataEntry de, IoTDevice id, ComputationalNode cn, MobileDevice dev)
	{
		dp.put(de, cn);
		dp.addEntryLatency(de, id, cn, dev, (MobileDataDistributionInfrastructure) currentInfrastructure);
	}
	
	protected synchronized void undeploy(DataPlacement dp, DataEntry de, IoTDevice id, ComputationalNode cn, MobileDevice dev)
	{
		dp.remove(de);
		dp.removeEntryLatency(de, id, cn, dev, (MobileDataDistributionInfrastructure) currentInfrastructure);
	}
	
}
