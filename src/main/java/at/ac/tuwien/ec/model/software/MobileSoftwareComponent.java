package at.ac.tuwien.ec.model.software;

import java.io.Serializable;

import at.ac.tuwien.ec.model.Hardware;

public class MobileSoftwareComponent extends SoftwareComponent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5500721963611706499L;
	private double inData,outData,runTime = Double.MAX_VALUE;
	private boolean offloadable;
	private double rank = 0.0;
	private boolean visited = false;
		
	public MobileSoftwareComponent(String id, Hardware requirements, double millionsOfInstructions,
			String uid, double inData, double outData) {
		super(id, requirements, millionsOfInstructions, uid);
		this.inData = inData;
		this.outData = outData;
		this.offloadable = true;
	}
	
	public MobileSoftwareComponent(String id, Hardware requirements, double millionsOfInstructions,
			String uid, double inData, double outData, boolean offloadable) {
		super(id, requirements, millionsOfInstructions, uid);
		this.inData = inData;
		this.outData = outData;
		this.offloadable = offloadable;
	}
	
	public double getInData() {
		// TODO Auto-generated method stub
		return inData;
	}

	public double getOutData() {
		// TODO Auto-generated method stub
		return outData;
	}

	public void setInData(int inData) {
		this.inData = inData;
	}

	public void setOutData(int outData) {
		this.outData = outData;
	}
	
	public boolean isOffloadable() {
		return offloadable;
	}

	public void setOffloadable(boolean offloadable) {
		this.offloadable = offloadable;
	}

	public double getRunTime() {
		return runTime;
	}

	public void setRunTime(double runTime) {
		this.runTime = runTime;
	}

	public double getRank() {
		return rank;
	}

	public void setRank(double rank) {
		this.rank = rank;
	}

	public boolean isVisited() {
		return visited ;
	}

	public void setVisited(boolean b) {
		visited = b;
		
	}
	
	public String toString()
	{
		return this.getId();
	}	
}
