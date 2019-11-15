package at.ac.tuwien.ec.model.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.jgrapht.Graph;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class MobileDataDistributionInfrastructure extends MobileCloudInfrastructure {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1870819700911396938L;
	private HashMap<String, IoTDevice> iotDevices;
	private HashMap<String, ArrayList<MobileDevice>> registry;
	private HashMap<String, ContainerInstance > vmRepository;
	private HashMap<String, ArrayList<ContainerInstance>> vmAssignment;
	
	public MobileDataDistributionInfrastructure()
	{
		super();
		iotDevices = new HashMap<String,IoTDevice>();
		registry = new HashMap<String, ArrayList<MobileDevice>>();
		vmRepository = new HashMap<String, ContainerInstance>();
		vmRepository.put("c5.large", new ContainerInstance("c5.large",new HardwareCapabilities(new Hardware(2,100,1e7), 10000),0.085));
		vmRepository.put("c5.xlarge", new ContainerInstance("c5.xlarge",new HardwareCapabilities(new Hardware(4,100,1e7), 10000),0.17));
		vmRepository.put("c5.2xlarge", new ContainerInstance("c5.2xlarge",new HardwareCapabilities(new Hardware(8,100,1e7), 10000),0.34));
		vmRepository.put("c5.4xlarge", new ContainerInstance("c5.4xlarge",new HardwareCapabilities(new Hardware(16,100,1e7), 10000),0.68));
		vmRepository.put("c5d.2xlarge", new ContainerInstance("c5d.2xlarge",new HardwareCapabilities(new Hardware(8,100,2.5e7), 10000),0.384));
		vmRepository.put("c5d.4xlarge", new ContainerInstance("c5d.4xlarge",new HardwareCapabilities(new Hardware(16,100,2.5e7), 10000),0.768));
		vmRepository.put("h1.2xlarge", new ContainerInstance("h1.2xlarge",new HardwareCapabilities(new Hardware(8,100,1e8), 10000),0.468));
		vmRepository.put("h1.4xlarge", new ContainerInstance("h1.4xlarge",new HardwareCapabilities(new Hardware(16,100,1e8), 10000),0.936));
		vmRepository.put("i3.2xlarge", new ContainerInstance("i3.2xlarge",new HardwareCapabilities(new Hardware(8,100,1e8), 10000),0.624));
		vmRepository.put("i3.4xlarge", new ContainerInstance("i3.4xlarge",new HardwareCapabilities(new Hardware(16,100,1e8), 10000),1.248));
		vmAssignment = new HashMap<String, ArrayList<ContainerInstance>>();
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

	public double computeDataEntryLatency(DataEntry de, ComputationalNode target, MobileDevice mDev)
	{
		IoTDevice dev = iotDevices.get(de.getIotDeviceId());
		return de.getTotalProcessingTime(dev, target, mDev, this);
	}

	public double getInDataTransmissionTime(DataEntry dataEntry, IoTDevice dev, ComputationalNode node) {
		return connectionMap.getInDataTransmissionTime(dataEntry, dev, node);
	}

	public double getOutDataTransmissionTime(DataEntry dataEntry, ComputationalNode node, MobileDevice mobile) {
		return connectionMap.getOutDataTransmissionTime(dataEntry, node, mobile);
	}
	
	public NetworkedNode getNodeById(String id)
	{
		if(iotDevices.containsKey(id))
			return iotDevices.get(id);
		return super.getNodeById(id);				
	}
	
	public void addVMInstance(String id, ContainerInstance vm)
	{
		if(!vmRepository.containsKey(id))
			vmRepository.put(id, vm);
	}
	
	public void instantiateContainerForUser(String uid, ContainerInstance vm)
	{
		if(!vmAssignment.containsKey(uid))
			vmAssignment.put(uid, new ArrayList<ContainerInstance>());
		vmAssignment.get(uid).add(vm);
	}

	public ArrayList<ContainerInstance> getVMAssignment(String uid) {
		return vmAssignment.get(uid);
	}

	public HashMap<String, ContainerInstance> getVMRepository() {
		return vmRepository;
	}
	
	public void setEdgeWeights()
	{
		connectionMap.setEdgeWeights();
	}

	public Graph<NetworkedNode, NetworkConnection> getConnectionMap() {
		// TODO Auto-generated method stub
		return this.connectionMap;
	}
	
}
