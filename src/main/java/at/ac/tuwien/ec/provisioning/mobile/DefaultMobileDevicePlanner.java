package at.ac.tuwien.ec.provisioning.mobile;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.sleipnir.configurations.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;

public class DefaultMobileDevicePlanner {
	
	static int mobileNum = SimulationSetup.mobileNum;
	static double mobileEnergyBudget = OffloadingSetup.mobileEnergyBudget;
	static HardwareCapabilities defaultMobileDeviceHardwareCapabilities 
				= SimulationSetup.defaultMobileDeviceHardwareCapabilities;
	static CPUEnergyModel defaultMobileDeviceCPUModel = SimulationSetup.defaultMobileDeviceCPUModel;
	static NETEnergyModel defaultMobileDeviceNetModel = SimulationSetup.defaultMobileDeviceNETModel;
	
	
	public static void setupMobileDevices(MobileCloudInfrastructure inf, int number)
	{
		for(int i = 0; i < number; i++)
		{
			MobileDevice device = new MobileDevice("mobile_"+i,defaultMobileDeviceHardwareCapabilities.clone(),mobileEnergyBudget);
			device.setCPUEnergyModel(defaultMobileDeviceCPUModel);
			device.setNetEnergyModel(defaultMobileDeviceNetModel);
			Coordinates randomCoordinates = new Coordinates(SimulationSetup.MAP_M / 2,
					SimulationSetup.MAP_N);
			device.setCoords(randomCoordinates);
			inf.addMobileDevice(device);
			//depending on setup of traffic
						
		}
	}
	
	public static void setupMobileDevices(MobileDataDistributionInfrastructure inf, int number)
	{
		for(int i = 0; i < number; i++)
		{
			MobileDevice device = new MobileDevice("mobile_"+i,defaultMobileDeviceHardwareCapabilities.clone(),mobileEnergyBudget);
			device.setCPUEnergyModel(defaultMobileDeviceCPUModel);
			device.setNetEnergyModel(defaultMobileDeviceNetModel);
			Coordinates randomCoordinates = new Coordinates(RandomUtils.nextInt(SimulationSetup.MAP_M),
												RandomUtils.nextInt(SimulationSetup.MAP_N*2));
			device.setCoords(randomCoordinates);
			inf.addMobileDevice(device);
			//depending on setup of traffic
			
			
			
		}
	}
	
	public static double nodeDistance(NetworkedNode n1, NetworkedNode n2) {
		Coordinates c1,c2;
		
		c1 = n1.getCoords();
		c2 = n2.getCoords();
		
		return (Math.abs(c1.getLatitude()-c2.getLatitude()) 
				+ Math.max(0, 
						(Math.abs(c1.getLatitude()-c2.getLatitude())
								- Math.abs(c1.getLongitude()-c2.getLongitude()) )/2));
	}
	

}
