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

public class L2NormPlacement extends DataPlacementAlgorithm {

	public L2NormPlacement(VMPlanner planner,ArrayList<DataEntry> dataEntries, MobileDataDistributionInfrastructure inf)
	{
		super(planner);
		setInfrastructure(inf);
		this.dataEntries = dataEntries;		
	}

	public L2NormPlacement(VMPlanner planner, Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure> arg)
	{
		super(planner);
		setInfrastructure(arg._2);
		this.dataEntries = arg._1;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4207485325424140303L;


	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<DataPlacement> dataPlacements = new ArrayList<DataPlacement>();
		DataPlacement dp = new DataPlacement();
		dp.setCurrentInfrastructure((MobileDataDistributionInfrastructure) this.currentInfrastructure);
		MobileDataDistributionInfrastructure mddi = (MobileDataDistributionInfrastructure) this.currentInfrastructure;
		ArrayList<ComputationalNode> sortedTargets = mddi.getAllNodes();

		for(MobileDevice dev: currentInfrastructure.getMobileDevices().values())
		{
			ArrayList<DataEntry> dataEntriesForDev = filterByDevice(dataEntries, dev);
			ArrayList<VMInstance> instancesPerUser = this.vmPlanner.performVMAllocation(dataEntriesForDev, dev, (MobileDataDistributionInfrastructure) this.currentInfrastructure);
			double timeStep = 0.0;
			int j = 0;
			for(DataEntry de : dataEntriesForDev)
			{
				ComputationalNode target = null;
				double minNorm = Double.MAX_VALUE;
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
					if(cn.getCapabilities().supports(de.getVMInstance().getCapabilities().getHardware())) {

						double tmp = norm(de.getVMInstance(),cn);
						if(Double.compare(tmp,minNorm) < 0)
						{
							minNorm = tmp;
							target = cn;
						}
					}
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

	private double norm(VMInstance vmInstance, ComputationalNode cn) {
		return Math.pow((vmInstance.getCapabilities().getAvailableCores()
				- cn.getCapabilities().getAvailableCores()),2.0);
	}

}
