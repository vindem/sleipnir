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

public abstract class ComputationalNode extends NetworkedNode implements Serializable{
	
	private class DefaultPriceModel implements PricingModel,Serializable
	{
		public double computeCost(SoftwareComponent sc, ComputationalNode cn, MobileCloudInfrastructure i) {
			return 0.0;
		}
	}
	
	
	
	protected CPUEnergyModel cpuEnergyModel;
	protected PricingModel priceModel;
		
	public ComputationalNode(String id, HardwareCapabilities capabilities)
	{
		super(id,capabilities);
		setPricingModel(new DefaultPriceModel());
	}
		
	private void setPricingModel(DefaultPriceModel pricingModel) {
		this.priceModel = pricingModel;		
	}
		
	public double getMipsPerCore(){
		return this.capabilities.getMipsPerCore();
	}
	
	public CPUEnergyModel getCPUEnergyModel() {
		return cpuEnergyModel;
	}

	public void setCPUEnergyModel(CPUEnergyModel cpuEnergyModel) {
		this.cpuEnergyModel = cpuEnergyModel;
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

	public void undeploy(VMInstance vmInstance) {
		capabilities.undeploy(vmInstance);		
	}

	public void deployVM(VMInstance vm) {
		capabilities.deploy(vm);
	}
	
}
