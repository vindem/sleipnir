package at.ac.tuwien.ec.datamodel.algorithms.placement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.algorithms.selection.ContainerPlanner;
import at.ac.tuwien.ec.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;
import at.ac.tuwien.ec.scheduling.Scheduling;
import at.ac.tuwien.ec.scheduling.simulation.SimIteration;

public abstract class DataPlacementAlgorithm extends SimIteration implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4568099240968031102L;
	protected ArrayList<DataEntry> dataEntries;
	protected ContainerPlanner vmPlanner;
	
	
	public DataPlacementAlgorithm() 
	{
		super();
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
		n.undeploy(de.getContainerInstance());
	}
	
	protected synchronized void deployVM(DataPlacement dp, DataEntry de, int entriesNum, IoTDevice id, ComputationalNode cn, MobileDevice dev, ContainerInstance vm)
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
			if(registry.containsKey(de.getTopic()))
				if(registry.get(de.getTopic()).contains(dev))
					filtered.add(de);
				
		return filtered;	
	}
	
}
