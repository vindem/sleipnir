package at.ac.tuwien.ac.datamodel.placement.algorithms;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import at.ac.tuwien.ac.datamodel.DataEntry;
import at.ac.tuwien.ac.datamodel.placement.DataPlacement;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.Scheduling;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;
import scala.Tuple2;

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
	
	public RandomDataPlacementAlgorithm(Tuple2<ArrayList<DataEntry>,MobileDataDistributionInfrastructure> arg)
	{
		setInfrastructure(arg._2);
		this.dataEntries = arg._1;
	}
	
	@Override
	public ArrayList<? extends Scheduling> findScheduling() {
		ArrayList<DataPlacement> dataPlacements = new ArrayList<DataPlacement>();
		DataPlacement dp = new DataPlacement();
		dp.setCurrentInfrastructure((MobileDataDistributionInfrastructure) this.currentInfrastructure);
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
					VMInstance vm = new VMInstance("m2xlarge", new HardwareCapabilities(new Hardware(1,1.0,1.0),1000.0),1.0);
					deployOnVM(dp, d, (IoTDevice) inf.getNodeById(d.getIotDeviceId()), target, mDev,vm);
				}
			}
		}
		dataPlacements.add(dp);
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
