package at.ac.tuwien.ec.blockchain;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ComputationalNode;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;

public class Transaction extends MobileSoftwareComponent {

	private double quantileOfMI;
	private ComputationalNode offloadTarget;
	
	public ComputationalNode getOffloadTarget() {
		return offloadTarget;
	}

	public void setOffloadTarget(ComputationalNode offloadTarget) {
		this.offloadTarget = offloadTarget;
	}

	public Transaction(String id, Hardware requirements, double millionsOfInstructions, String uid, double inData,
			double outData) {
		super(id, requirements, millionsOfInstructions, uid, inData, outData);
		// TODO Auto-generated constructor stub
	}

	public double getQuantileOfMI()
	{
		return quantileOfMI;
	}
	
	public void setQuantileOfMI(double quantileOfMI)
	{
		this.quantileOfMI = quantileOfMI;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9050849689680267829L;

}
