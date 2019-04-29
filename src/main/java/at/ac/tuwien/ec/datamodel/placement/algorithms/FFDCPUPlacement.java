package at.ac.tuwien.ec.datamodel.placement.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.datamodel.placement.algorithms.vmplanner.VMPlanner;
import at.ac.tuwien.ec.model.Scheduling;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;
import scala.Tuple2;

public class FFDCPUPlacement extends DataPlacementAlgorithm {

	public FFDCPUPlacement(VMPlanner planner,ArrayList<DataEntry> dataEntries, MobileDataDistributionInfrastructure inf)
	{
		super(planner);
		setInfrastructure(inf);
		this.dataEntries = dataEntries;		
	}
	
	public FFDCPUPlacement(VMPlanner planner, Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure> arg)
	{
		super(planner);
		setInfrastructure(arg._2);
		this.dataEntries = arg._1;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4207485325424140303L;

	class CPUComparator implements Comparator<ComputationalNode>
	{

		@Override
		public int compare(ComputationalNode o1, ComputationalNode o2) {
			return Double.compare(o2.getMipsPerCore() * o2.getCapabilities().getAvailableCores(),
					o1.getMipsPerCore() * o1.getCapabilities().getAvailableCores());
		}
		
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<DataPlacement> dataPlacements = new ArrayList<DataPlacement>();
		DataPlacement dp = new DataPlacement();
		dp.setCurrentInfrastructure((MobileDataDistributionInfrastructure) this.currentInfrastructure);
		MobileDataDistributionInfrastructure mddi = (MobileDataDistributionInfrastructure) this.currentInfrastructure;
		ArrayList<ComputationalNode> sortedTargets = mddi.getAllNodes();
		Collections.sort(sortedTargets,new CPUComparator());
		for(MobileDevice dev: currentInfrastructure.getMobileDevices().values())
		{
			ArrayList<DataEntry> dataEntriesForDev = filterByDevice(dataEntries, dev);
			ArrayList<VMInstance> instancesPerUser = this.vmPlanner.performVMAllocation(dataEntriesForDev, dev, (MobileDataDistributionInfrastructure) this.currentInfrastructure);
			double timeStep = 0.0;
			int j = 0;
			for(DataEntry de : dataEntriesForDev)
			{
				ComputationalNode target = null;
				for(ComputationalNode cn : sortedTargets)
				{
					IoTDevice iotD = (IoTDevice) mddi.getNodeById(de.getIotDeviceId());
					if(mddi.getConnectionMap().getEdge(iotD,cn) == null)
						continue;
					if(mddi.getConnectionMap().getEdge(iotD, cn).getBandwidth() == 0 ||
							!Double.isFinite(mddi.getConnectionMap().getEdge(iotD, cn).getLatency()))
						continue;
					if(mddi.getConnectionMap().getEdge(cn, dev).getBandwidth() == 0 ||
							!Double.isFinite(mddi.getConnectionMap().getEdge(cn, dev).getLatency()))
						continue;
					if(cn.getCapabilities().supports(de.getVMInstance().getCapabilities().getHardware()))
						target = cn;
					

				}
				if(target == null)
				{
					dp = null;
					break;
				}
				else 
					deployVM(dp, de, dataEntriesForDev.size() ,(IoTDevice) mddi.getNodeById(de.getIotDeviceId()), target, dev, de.getVMInstance());
				j++;
				if(j%10 == 0) 
				{
					timeStep++;
					dev.updateCoordsWithMobility(timeStep);
				}
			}
			double vmCost = 0.0;
			for(VMInstance vm : instancesPerUser)
				vmCost += vm.getPricePerSecond(); 
			dev.setCost(vmCost);
		}
		
		
		if(dp != null)
		{
			double avgLat = 0.0,avgCost=0.0,maxLat=0.0;
			for(MobileDevice dev: currentInfrastructure.getMobileDevices().values()) 
			{
				avgLat += dev.getAverageLatency();
				avgCost += dev.getCost();
				maxLat += dev.getMaxLatency();
			}
			
			dp.setAverageLatency(avgLat / currentInfrastructure.getMobileDevices().size());
			dp.setAverageMaxLatency(maxLat / currentInfrastructure.getMobileDevices().size());
			dp.setCost(avgCost / currentInfrastructure.getMobileDevices().size());
			dataPlacements.add(dp);
		}
			
			

		return dataPlacements;
	}

}
