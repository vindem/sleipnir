package at.ac.tuwien.ec.model.infrastructure.planning.mobile;

import org.apache.commons.lang.math.RandomUtils;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
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
	
	public static void setupMobileDevices(MobileCloudInfrastructure inf, int number)
	{
		for(int i = 0; i < number; i++)
		{
			MobileDevice device = new MobileDevice("mobile_"+i,defaultMobileDeviceHardwareCapabilities,mobileEnergyBudget);
			device.setCPUEnergyModel(defaultMobileDeviceCPUModel);
			device.setNetEnergyModel(defaultMobileDeviceNetModel);
			Coordinates randomCoordinates = new Coordinates(RandomUtils.nextInt(SimulationSetup.MAP_M),
												RandomUtils.nextInt(SimulationSetup.MAP_N));
			device.setCoords(randomCoordinates);
			inf.addMobileDevice(device);			
		}
	}

}
