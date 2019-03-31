package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import at.ac.tuwien.ec.model.HardwareCapabilities;

public class VMInstance extends ComputationalNode implements Cloneable{

	private Double pricePerSecond;
	private boolean deployed;
	
	public VMInstance(String id, HardwareCapabilities capabilities, Double price) {
		super(id, capabilities);
		this.pricePerSecond = price;
		deployed = false;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6623060297429718097L;

	@Override
	public void sampleNode() {
		// TODO Auto-generated method stub
		
	}

	public Double getPricePerSecond() {
		return pricePerSecond;
	}

	public void setPricePerSecond(Double pricePerSecond) {
		this.pricePerSecond = pricePerSecond;
	}

	public VMInstance clone() 
	{
		return new VMInstance(id, capabilities, pricePerSecond);
		
	}
	
	public boolean isDeployed()
	{
		return deployed;
	}
	
	public void setDeployed(boolean deployed)
	{
		this.deployed = deployed;
	}
	
	

}
