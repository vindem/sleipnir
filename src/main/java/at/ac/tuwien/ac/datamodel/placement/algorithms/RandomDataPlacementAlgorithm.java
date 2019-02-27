package at.ac.tuwien.ac.datamodel.placement.algorithms;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ac.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.model.Scheduling;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;

public class RandomDataPlacementAlgorithm extends DataPlacementAlgorithm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6767647040932539252L;

	public RandomDataPlacementAlgorithm(ArrayList<DataEntry> dataEntries, MobileDataDistributionInfrastructure inf)
	{
		setInfrastructure(inf);
		this.dataEntries = dataEntries;
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<DataPlacement> dataPlacements = new ArrayList<DataPlacement>();
		DataPlacement dp = new DataPlacement();
		for(DataEntry d: this.dataEntries)
		{
			MobileDataDistributionInfrastructure inf = 
					(MobileDataDistributionInfrastructure)this.currentInfrastructure;
			HashMap<String, ArrayList<MobileDevice>> registry = inf.getRegistry();
			if(registry.containsKey(d.getTopic()))
			{
				ArrayList<MobileDevice> devs = registry.get(d.getTopic());
				for(MobileDevice mDev : devs)
				{
					ComputationalNode target = findTarget(inf);
					double entryLatency = inf.computeDataEntryLatency(d, target, mDev);
					dp.addEntryLatency(d, (IoTDevice) inf.getNodeById(d.getIotDeviceId()), target, mDev, inf);
				}
			}
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
			nodeChooser = new UniformIntegerDistribution(0,inf.getCloudNodes().size());
			int idx = nodeChooser.sample();
			target = (ComputationalNode) inf.getCloudNodes().values().toArray()[idx];
		}
		else
		{
			nodeChooser = new UniformIntegerDistribution(0,inf.getEdgeNodes().size());
			int idx = nodeChooser.sample();
			target = (ComputationalNode) inf.getEdgeNodes().values().toArray()[idx];
		}
		return target;
	}

}
