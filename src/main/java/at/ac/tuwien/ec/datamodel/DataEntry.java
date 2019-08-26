package at.ac.tuwien.ec.datamodel;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.VMInstance;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public class DataEntry extends MobileSoftwareComponent implements Cloneable {

	private String topic, iotDeviceId;
	private VMInstance vm;
	
	public DataEntry(String id, Hardware requirements, double millionsOfInstructions, String iotNode, double inData,
			double outData, String topic)
	{
		super(id, requirements, millionsOfInstructions, iotNode, inData, outData, false);
		setTopic(topic);
		setIotDeviceId(iotNode);
	}
	
	private DataEntry(String id, Hardware requirements, double millionsOfInstructions, String uid, double inData,
			double outData, boolean offloadable) {
		super(id, requirements, millionsOfInstructions, uid, inData, outData, offloadable);
		// TODO Auto-generated constructor stub
	}
	
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	
		
	public double getTotalProcessingTime(IoTDevice dev, ComputationalNode node, MobileDevice mobile, MobileDataDistributionInfrastructure infrastructure)
	{
		double dataTransmissionTime = getDataTransmissionTimeOn(dev, node, infrastructure);
		double processingTime = getProcessingTime(node);
		double outDataTransmission = getProcessedDataTransmissionTime(node, mobile, infrastructure);
		return dataTransmissionTime + processingTime + outDataTransmission;
	}
	
	private double getDataTransmissionTimeOn(IoTDevice dev, ComputationalNode node, MobileDataDistributionInfrastructure infrastructure)
	{
		return infrastructure.getInDataTransmissionTime(this, dev, node);		
	}
	
	private double getProcessingTime(ComputationalNode node)
	{
		return this.getMillionsOfInstruction() / Math.min(node.getMipsPerCore(), this.getVMInstance().getMipsPerCore());
	}
	
	private double getProcessedDataTransmissionTime(ComputationalNode node, MobileDevice mobile, MobileDataDistributionInfrastructure infrastructure)
	{
		return infrastructure.getOutDataTransmissionTime(this, node, mobile);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1297937242964874317L;

	public String getIotDeviceId() {
		return iotDeviceId;
	}

	public void setIotDeviceId(String iotDeviceId) {
		this.iotDeviceId = iotDeviceId;
	}
	
	public void setVMInstance(VMInstance vm)
	{
		this.vm = vm;
	}
	
	public VMInstance getVMInstance() {
		// TODO Auto-generated method stub
		return this.vm;
	}

	public DataEntry clone() throws CloneNotSupportedException
	{
		return (DataEntry) super.clone();
	}

	

	
	
}
