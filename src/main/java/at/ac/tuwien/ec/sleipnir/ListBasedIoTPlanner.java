package at.ac.tuwien.ec.sleipnir;

import java.util.ArrayList;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;

public class ListBasedIoTPlanner {
	
	static HardwareCapabilities capabilities = SimulationSetup.defaultIoTDeviceHardwareCapabilities;

	public static void setupIoTDevices(MobileDataDistributionInfrastructure inf, ArrayList<Coordinates> iotDevicesCoordinates) {
		int i = 0;
		for(Coordinates coords: iotDevicesCoordinates)
		{
			IoTDevice device = new IoTDevice("iot"+i, capabilities.clone());
			device.setCoords(coords);
			device.setTopics(null);
			device.setNetEnergyModel(SimulationSetup.defaultMobileDeviceNETModel);
			inf.addIoTDevice(device);
			i++;
		}
	}

}
