package at.ac.tuwien.ec.model.infrastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.jgrapht.Graph;

import at.ac.tuwien.ec.datamodel.DataEntry;
import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.EdgeNode;
import at.ac.tuwien.ec.model.infrastructure.network.ConnectionMap;
import at.ac.tuwien.ec.model.infrastructure.network.NetworkConnection;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.provisioning.MobilityBasedNetworkPlanner;
import at.ac.tuwien.ec.provisioning.mobile.MobileDevicePlannerWithIoTMobility;
import at.ac.tuwien.ec.sleipnir.configurations.IoTFaaSSetup;

public class MobileDataDistributionInfrastructure extends MobileCloudInfrastructure implements Serializable,Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1870819700911396938L;
	private HashMap<String, IoTDevice> iotDevices;
	private HashMap<String, ArrayList<MobileDevice>> registry;
	
	
	public MobileDataDistributionInfrastructure()
	{
		super();
		iotDevices = new HashMap<String,IoTDevice>();
		registry = new HashMap<String, ArrayList<MobileDevice>>();
		/*
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
		*/
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
	
	public void removeDeviceFromTopic(MobileDevice dev, String topic) {
		if(registry.containsKey(topic))
			registry.get(topic).remove(dev);
	}
	
	public ArrayList<MobileDevice> getSubscribedDevices(String topic)
	{
		if(registry.containsKey(topic))
			return registry.get(topic);
		return null;
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
	
	public void setEdgeWeights()
	{
		connectionMap.setEdgeWeights();
	}

	public ConnectionMap getConnectionMap() {
		// TODO Auto-generated method stub
		return this.connectionMap;
	}

	public void removeEdgeNodeAt(Coordinates coord, int index) {
		EdgeNode edge = edgeNodes.get("edge_"+index);
		if(edge != null)
			removeEdgeNode(edge);
	}
	
	public MobileDataDistributionInfrastructure clone() throws CloneNotSupportedException
	{
		return (MobileDataDistributionInfrastructure) super.clone();
		
	}
	
	public MobileDataDistributionInfrastructure lookupAtTimestamp(double timestamp)
	{
		MobileDataDistributionInfrastructure future = null;
		try {
			future = clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(MobileDevice d : future.getMobileDevices().values()) 
			d.updateCoordsWithMobility((double) timestamp);
		//System.out.println("ID: " + d.getId() + "COORDS: " + d.getCoords());
		
		MobilityBasedNetworkPlanner.setupMobileConnections(future);
		MobileDevicePlannerWithIoTMobility.updateDeviceSubscriptions(future,
				IoTFaaSSetup.selectedWorkflow);
		return future;
	}
	
	public MobileDataDistributionInfrastructure predictAtTimestamp(double timestamp)
	{
		MobileDataDistributionInfrastructure future = null;
		try {
			future = clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(MobileDevice d : future.getMobileDevices().values()) 
			d.predictCoordinatesAt((double) timestamp);
		//System.out.println("ID: " + d.getId() + "COORDS: " + d.getCoords());
		
		MobilityBasedNetworkPlanner.setupMobileConnections(future);
		MobileDevicePlannerWithIoTMobility.updateDeviceSubscriptions(future,
				IoTFaaSSetup.selectedWorkflow);
		return future;
	}
	
}
