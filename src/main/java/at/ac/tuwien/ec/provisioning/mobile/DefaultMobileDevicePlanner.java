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
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class DefaultMobileDevicePlanner {
	
	static int mobileNum = OffloadingSetup.mobileNum;
	static double mobileEnergyBudget = OffloadingSetup.mobileEnergyBudget;
	static HardwareCapabilities defaultMobileDeviceHardwareCapabilities 
				= OffloadingSetup.defaultMobileDeviceHardwareCapabilities;
	static CPUEnergyModel defaultMobileDeviceCPUModel = OffloadingSetup.defaultMobileDeviceCPUModel;
	static NETEnergyModel defaultMobileDeviceNetModel = OffloadingSetup.defaultMobileDeviceNETModel;
	
	
	public static void setupMobileDevices(MobileCloudInfrastructure inf, int number)
	{
		for(int i = 0; i < number; i++)
		{
			MobileDevice device = new MobileDevice("mobile_"+i,defaultMobileDeviceHardwareCapabilities.clone(),mobileEnergyBudget);
			device.setCPUEnergyModel(defaultMobileDeviceCPUModel);
			device.setNetEnergyModel(defaultMobileDeviceNetModel);
			Coordinates randomCoordinates = new Coordinates(OffloadingSetup.MAP_M / 2,
					OffloadingSetup.MAP_N);
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
			
			switch(SimulationSetup.traffic)
			{
			case "LOW": //subscription only to the closest
				double minDist = Double.MAX_VALUE;
				String targetId = "";
				for(IoTDevice iot : inf.getIotDevices().values())
				{
					double tmpDist = nodeDistance(device,iot);
					if(tmpDist < minDist)
					{
						minDist = tmpDist;
						targetId = iot.getId();
					}
						
				}
				inf.subscribeDeviceToTopic(device, targetId);
				break;
			case "MEDIUM":
				int startIndex = (i%2==0)? 0 : 1;
				for(int j = startIndex; j < SimulationSetup.iotDevicesNum; j+=2)
					inf.subscribeDeviceToTopic(device, "iot"+j);
				break;
			case "HIGH":
				for(int j = 0; j < SimulationSetup.iotDevicesNum; j++)
					inf.subscribeDeviceToTopic(device, "iot"+j);
				break;
			}
			
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
