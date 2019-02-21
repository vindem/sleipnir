package at.ac.tuwien.ec.model.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;

import at.ac.tuwien.ac.data.DataEntry;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;

public class MobileDataDistributionInfrastructure extends MobileCloudInfrastructure {
	
	private HashMap<String, IoTDevice> iotDevices;
	private HashMap<String, ArrayList<MobileDevice>> registry;
	
	public MobileDataDistributionInfrastructure()
	{
		super();
		iotDevices = new HashMap<String,IoTDevice>();
		registry = new HashMap<String, ArrayList<MobileDevice>>();
	}
	
	public void addIoTDevice(IoTDevice device)
	{
		iotDevices.put(device.getId(), device);
		connectionMap.addVertex(device);
	}

	public HashMap<String, IoTDevice> getIotDevices() {
		return iotDevices;
	}

	public void setIotDevices(HashMap<String, IoTDevice> iotDevices) {
		this.iotDevices = iotDevices;
	}

	public HashMap<String, ArrayList<MobileDevice>> getRegistry() {
		return registry;
	}

	public void setRegistry(HashMap<String, ArrayList<MobileDevice>> registry) {
		this.registry = registry;
	}
	
	public void subscribeDeviceToTopic(MobileDevice dev, String topic) {
		if(registry.containsKey(topic))
			registry.get(topic).add(dev);
		else
		{
			registry.put(topic, new ArrayList<MobileDevice>());
			registry.get(topic).add(dev);
		}
	}

	public double getOutDataTransmissionTime(DataEntry dataEntry, ComputationalNode node, MobileDevice mobile) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getInDataTransmissionTime(DataEntry dataEntry, IoTDevice dev, ComputationalNode node) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
