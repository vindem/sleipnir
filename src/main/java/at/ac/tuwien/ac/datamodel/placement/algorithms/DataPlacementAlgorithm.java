package at.ac.tuwien.ac.datamodel.placement.algorithms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ac.datamodel.placement.DataPlacement;
import at.ac.tuwien.ac.datamodel.placement.algorithms.vmplanner.VMPlanner;
import at.ac.tuwien.ec.model.Scheduling;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;
import at.ac.tuwien.ec.scheduling.simulation.SimIteration;

public abstract class DataPlacementAlgorithm extends SimIteration implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4568099240968031102L;
	protected ArrayList<DataEntry> dataEntries;
	protected VMPlanner vmPlanner;
	
	
	public DataPlacementAlgorithm(VMPlanner vmPlanner) 
	{
		super();
		this.vmPlanner = vmPlanner;
	}
	
	@Override
	public abstract ArrayList<? extends Scheduling> findScheduling();
	
	protected synchronized void deploy(DataPlacement dp, int entriesNum, DataEntry de, IoTDevice id, ComputationalNode cn, MobileDevice dev)
	{
		dp.put(de, cn);
		dp.addEntryLatency(de, entriesNum, id, cn, dev, (MobileDataDistributionInfrastructure) currentInfrastructure);
	}
	
	protected synchronized void undeploy(DataPlacement dp, DataEntry de, IoTDevice id, ComputationalNode cn, MobileDevice dev)
	{
		//dp.remove(de);
		//dp.removeEntryLatency(de, id, cn, dev, (MobileDataDistributionInfrastructure) currentInfrastructure);
		ComputationalNode n = (ComputationalNode) dp.get(de);
		n.undeploy(de.getVMInstance());
	}
	
	protected synchronized void deployVM(DataPlacement dp, DataEntry de, int entriesNum, IoTDevice id, ComputationalNode cn, MobileDevice dev, VMInstance vm)
	{
		if(dp != null)
		{
			de.setVMInstance(vm);
			dp.put(de, cn);
			dev.addEntryLatency(de,entriesNum, id, cn, dev, (MobileDataDistributionInfrastructure) currentInfrastructure);
			if(!vm.isDeployed())
				cn.deployVM(vm);
		}
	}
	
	protected ArrayList<DataEntry> filterByDevice(ArrayList<DataEntry> dataEntries, MobileDevice dev) {
		ArrayList<DataEntry> filtered = new ArrayList<DataEntry>();
		HashMap<String, ArrayList<MobileDevice>> registry 
			= ((MobileDataDistributionInfrastructure)this.getInfrastructure()).getRegistry();
		for(DataEntry de : dataEntries)
			if(registry.get(de.getTopic()).contains(dev))
				filtered.add(de);
				
		return filtered;	
	}
	
}
