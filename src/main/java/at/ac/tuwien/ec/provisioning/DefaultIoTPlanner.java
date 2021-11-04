package at.ac.tuwien.ec.provisioning;

import org.apache.commons.lang.math.RandomUtils;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;

public class DefaultIoTPlanner {
	
	static HardwareCapabilities capabilities = SimulationSetup.defaultIoTDeviceHardwareCapabilities;
	
	public static void setupIoTNodes(MobileDataDistributionInfrastructure inf, int iotDeviceNum)
	{
		for(int i = 0; i < iotDeviceNum; i++)
		{
			IoTDevice device = new IoTDevice("iot"+i, capabilities.clone());
			
			Coordinates randomCoordinates = new Coordinates(RandomUtils.nextInt((int)SimulationSetup.x_max),
												RandomUtils.nextInt((int)SimulationSetup.y_max));
			device.setCoords(randomCoordinates);
			String topics[] = new String[1];
			topics[0] = "iot-"+randomCoordinates.getLatitude()+","+randomCoordinates.getLongitude();
			device.setTopics(topics);
			inf.addIoTDevice(device);			
		}
	}

}
