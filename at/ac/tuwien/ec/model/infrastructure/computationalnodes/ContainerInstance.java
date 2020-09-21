package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import at.ac.tuwien.ec.model.HardwareCapabilities;

public class ContainerInstance extends ComputationalNode implements Cloneable{

	private Double pricePerSecond;
	private boolean deployed;
	
	public ContainerInstance(String id, HardwareCapabilities capabilities, Double price) {
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

	public ContainerInstance clone() 
	{
		return new ContainerInstance(id, capabilities, pricePerSecond);
		
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
