package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import java.io.Serializable;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.Timezone;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.model.pricing.PricingModel;
import at.ac.tuwien.ec.model.software.SoftwareComponent;

public abstract class ComputationalNode implements Serializable{
	
	private class DefaultPriceModel implements PricingModel,Serializable
	{
		public double computeCost(SoftwareComponent sc, ComputationalNode cn, MobileCloudInfrastructure i) {
			return 0.0;
		}
	}
	
	protected String id;
	protected Coordinates coords;
	protected HardwareCapabilities capabilities;
	protected CPUEnergyModel cpuEnergyModel;
	protected NETEnergyModel netEnergyModel;
	protected PricingModel priceModel;
		
	public ComputationalNode(String id, HardwareCapabilities capabilities)
	{
		this.id = id;
		setCapabilities(capabilities);
		setPricingModel(new DefaultPriceModel());
	}
	
	public String getId()
	{
		return this.id;
	}
	
	public void setCapabilities(HardwareCapabilities capabilities)
	{
		this.capabilities = capabilities;
	}
	
	private void setPricingModel(DefaultPriceModel pricingModel) {
		this.priceModel = pricingModel;		
	}
	
	public HardwareCapabilities getCapabilities()
	{
		return this.capabilities;
	}
	
	public double getMipsPerCore(){
		return this.capabilities.getMipsPerCore();
	}

	public Coordinates getCoords() {
		return coords;
	}

	public void setCoords(Coordinates coords) {
		this.coords = coords;
	}
	
	public void setCoords(Timezone tz) 
	{
		setCoords(tz.getX(),tz.getY());
	}
	
	public void setCoords(double x, double y){
		this.coords = new Coordinates(x,y);
	}
	
	public CPUEnergyModel getCPUEnergyModel() {
		return cpuEnergyModel;
	}

	public void setCPUEnergyModel(CPUEnergyModel cpuEnergyModel) {
		this.cpuEnergyModel = cpuEnergyModel;
	}

	public NETEnergyModel getNetEnergyModel() {
		return netEnergyModel;
	}

	public void setNetEnergyModel(NETEnergyModel netEnergyModel) {
		this.netEnergyModel = netEnergyModel;
	}
		
	public boolean isCompatible(SoftwareComponent sc)
	{
		return capabilities.supports(sc.getHardwareRequirements());
	}
	
	public double computeCost(SoftwareComponent sc, MobileCloudInfrastructure i)
	{
		return priceModel.computeCost(sc, this, i);
	}
	
	public boolean deploy(SoftwareComponent sc)
	{
		return capabilities.deploy(sc);
	}
	
	public void undeploy(SoftwareComponent sc) 
	{
		capabilities.undeploy(sc);
	}
	
	public abstract void sampleNode();
	
}
