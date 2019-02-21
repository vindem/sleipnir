package at.ac.tuwien.ac.data;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;

public class DataEntry extends MobileSoftwareComponent {

	private String topic, iotDeviceId;
	
	public DataEntry(String id, Hardware requirements, double millionsOfInstructions, String targetNode, double inData,
			double outData, String topic)
	{
		super(id, requirements, millionsOfInstructions, targetNode, inData, outData, false);
		setTopic(topic);
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
	
	public double getTotalData()
	{
		return getInData() + getOutData();
	}
	
	private double getDataTransmissionTimeOn(IoTDevice dev, ComputationalNode node, MobileDataDistributionInfrastructure infrastructure)
	{
		return infrastructure.getInDataTransmissionTime(this, dev, node);		
	}
	
	private double getProcessingTime(ComputationalNode node)
	{
		return this.getMillionsOfInstruction() / node.getMipsPerCore();
	}
	
	private double getProcessedDataTransmissionTime(ComputationalNode node, MobileDevice mobile, MobileDataDistributionInfrastructure infrastructure)
	{
		return infrastructure.getOutDataTransmissionTime(this, node, mobile);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1297937242964874317L;

	

}
