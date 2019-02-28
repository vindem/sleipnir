package at.ac.tuwien.ec.model.infrastructure.planning.mobile;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class DefaultMobileDevicePlanner {
	
	static int mobileNum = SimulationSetup.mobileNum;
	static double mobileEnergyBudget = SimulationSetup.mobileEnergyBudget;
	static HardwareCapabilities defaultMobileDeviceHardwareCapabilities 
				= SimulationSetup.defaultMobileDeviceHardwareCapabilities;
	static CPUEnergyModel defaultMobileDeviceCPUModel = SimulationSetup.defaultMobileDeviceCPUModel;
	static NETEnergyModel defaultMobileDeviceNetModel = SimulationSetup.defaultMobileDeviceNETModel;
	
	public static void setupMobileDevices(MobileDataDistributionInfrastructure inf, int number)
	{
		UniformIntegerDistribution chooser = new UniformIntegerDistribution(0, SimulationSetup.topics.length-1);
		for(int i = 0; i < number; i++)
		{
			MobileDevice device = new MobileDevice("mobile_"+i,defaultMobileDeviceHardwareCapabilities.clone(),mobileEnergyBudget);
			device.setCPUEnergyModel(defaultMobileDeviceCPUModel);
			device.setNetEnergyModel(defaultMobileDeviceNetModel);
			Coordinates randomCoordinates = new Coordinates(RandomUtils.nextInt(SimulationSetup.MAP_M),
												RandomUtils.nextInt(SimulationSetup.MAP_N));
			device.setCoords(randomCoordinates);
			inf.addMobileDevice(device);
			for(int j = 0; j < SimulationSetup.topics.length; j++)
				inf.subscribeDeviceToTopic(device, SimulationSetup.topics[j]);
		}
	}

}
