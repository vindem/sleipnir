package at.ac.tuwien.ec.model.software;

import java.io.Serializable;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;

public class SoftwareComponent implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6333164666099570460L;
	private String id;
	private Hardware requirements;
	protected double millionsOfInstruction;
	private String userId;
	
	public SoftwareComponent(String id, Hardware requirements,double millionsOfInstructions, String uid)
	{
		this.id = id;
		this.requirements = requirements;
		this.millionsOfInstruction = millionsOfInstructions;
		this.userId = uid;
	}	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Hardware getRequirements() {
		return requirements;
	}

	public void setRequirements(Hardware requirements) {
		this.requirements = requirements;
	}

	public double getMillionsOfInstruction() {
		return millionsOfInstruction;
	}

	public void setMillionsOfInstruction(double millionsOfInstruction) {
		this.millionsOfInstruction = millionsOfInstruction;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public double getLocalRuntimeOnNode(ComputationalNode n, MobileCloudInfrastructure i) {
		return (millionsOfInstruction/n.getMipsPerCore());
	}
	

	public double getRuntimeOnNode(ComputationalNode n, MobileCloudInfrastructure i) {
		return i.getTransmissionTime((MobileSoftwareComponent)this, i.getNodeById(userId), n)*1.0 
				+ (millionsOfInstruction/n.getMipsPerCore());
				
	}
	
	public double getRuntimeOnNode(ComputationalNode n, ComputationalNode m, MobileCloudInfrastructure i) {
		return ((n==null)? 0 : i.getTransmissionTime((MobileSoftwareComponent)this, n, m)*1.0) 
				+ (millionsOfInstruction/m.getMipsPerCore());
				
	}

	public Hardware getHardwareRequirements() {
		// TODO Auto-generated method stub
		return requirements;
	}

}
