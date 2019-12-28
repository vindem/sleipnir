package at.ac.tuwien.ec.datamodel.algorithms.placement;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.datamodel.algorithms.selection.ContainerPlanner;
import at.ac.tuwien.ec.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;
import at.ac.tuwien.ec.scheduling.Scheduling;
import scala.Tuple2;

public class RandomDataPlacementAlgorithm extends DataPlacementAlgorithm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6767647040932539252L;

	public RandomDataPlacementAlgorithm(ArrayList<DataEntry> dataEntries, MobileDataDistributionInfrastructure inf)
	{
		super();
		setInfrastructure(inf);
		this.dataEntries = dataEntries;
	}
	
	public RandomDataPlacementAlgorithm(Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure> arg)
	{
		super();
		setInfrastructure(arg._2);
		this.dataEntries = arg._1;
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<DataPlacement> dataPlacements = new ArrayList<DataPlacement>();
		DataPlacement dp = new DataPlacement();
		dp.setCurrentInfrastructure((MobileDataDistributionInfrastructure) this.currentInfrastructure);
		MobileDataDistributionInfrastructure mddi = (MobileDataDistributionInfrastructure) this.currentInfrastructure;
		for(MobileDevice dev: currentInfrastructure.getMobileDevices().values())
		{
			ArrayList<DataEntry> dataEntriesForDev = filterByDevice(dataEntries, dev);
			ArrayList<ContainerInstance> instancesPerUser = this.vmPlanner.performVMAllocation(dataEntriesForDev, dev, (MobileDataDistributionInfrastructure) this.currentInfrastructure);

			for(DataEntry de : dataEntriesForDev)
			{
				ComputationalNode target;
				
				//do
					target = findTarget((MobileDataDistributionInfrastructure) this.currentInfrastructure);
				//while(!target.isCompatible(de));
				
				deployVM(dp, de, dataEntriesForDev.size(), (IoTDevice) mddi.getNodeById(de.getIotDeviceId()), target, dev, de.getContainerInstance());
				
			}
			double vmCost = 0.0;
			for(ContainerInstance vm : instancesPerUser)
				vmCost += vm.getPricePerSecond(); 
			dev.setCost(vmCost);
		}
		if(dp != null)
		{
			double avgLat = 0.0,avgCost=0.0;
			for(MobileDevice dev: currentInfrastructure.getMobileDevices().values()) 
			{
				avgLat += dev.getAverageLatency();
				avgCost += dev.getCost();
			}
			dp.setAverageLatency(avgLat / currentInfrastructure.getMobileDevices().size());
			dp.setCost(avgCost / currentInfrastructure.getMobileDevices().size());
			dataPlacements.add(dp);
		}
			
		return dataPlacements;
	}

	private ComputationalNode findTarget(MobileDataDistributionInfrastructure inf) {
		UniformRealDistribution udd = new UniformRealDistribution();
		UniformIntegerDistribution nodeChooser;
		boolean cloud = udd.sample() < 0.5;
		ComputationalNode target;
		if(cloud)
		{
			nodeChooser = new UniformIntegerDistribution(0,inf.getCloudNodes().size()-1);
			int idx = nodeChooser.sample();
			target = (ComputationalNode) inf.getCloudNodes().values().toArray()[idx];
		}
		else
		{
			nodeChooser = new UniformIntegerDistribution(0,inf.getEdgeNodes().size()-1);
			int idx = nodeChooser.sample();
			target = (ComputationalNode) inf.getEdgeNodes().values().toArray()[idx];
		}
		return target;
	}

}
