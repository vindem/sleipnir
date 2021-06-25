package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.Timezone;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.model.pricing.PricingModel;
import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.scheduling.utils.RuntimeComparator;

public abstract class ComputationalNode extends NetworkedNode implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3728294299293549641L;

	private class DefaultPriceModel implements PricingModel,Serializable
	{
		private static final long serialVersionUID = 8372377992210681188L;

		public double computeCost(SoftwareComponent sc, ComputationalNode cn0, ComputationalNode cn, MobileCloudInfrastructure i) {
			return 0.0;
		}

		@Override
		public double computeCost(SoftwareComponent sc, ComputationalNode src, MobileCloudInfrastructure i) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
	
	
	protected CPUEnergyModel cpuEnergyModel;
	protected PricingModel priceModel;
	protected double bandwidth, latency, est = 0.0;
	protected ArrayList<MobileSoftwareComponent> allocated;
		
	public ComputationalNode(String id, HardwareCapabilities capabilities)
	{
		super(id,capabilities);
		setPricingModel(new DefaultPriceModel());
		this.setMaxDistance(-1);
		this.allocated = new ArrayList<MobileSoftwareComponent>();
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
		
	public double computeCost(SoftwareComponent sc, ComputationalNode src, MobileCloudInfrastructure i)
	{
		return priceModel.computeCost(sc, src, i);
	}
	
	public boolean deploy(SoftwareComponent sc)
	{
		allocated.add((MobileSoftwareComponent) sc);
		return capabilities.deploy(sc);
	}
	
	public void undeploy(SoftwareComponent sc) 
	{
		capabilities.undeploy(sc);
		allocated.remove(sc);
	}
	
	public double getESTforTask(MobileSoftwareComponent sc)
	{
		double est = 0.0;
		if(this.isCompatible(sc))
			return est;
		else
		{
			//we check when it will be possible to allocate sc
			Collections.sort(allocated, new RuntimeComparator());
			ArrayList<MobileSoftwareComponent> tmpAllocated = (ArrayList<MobileSoftwareComponent>) allocated.clone();
			HardwareCapabilities futureCapabilities = capabilities.clone();
			MobileSoftwareComponent firstTask = tmpAllocated.remove(0);
			while(!futureCapabilities.supports(sc.getHardwareRequirements()))
			{
				futureCapabilities.undeploy(firstTask);
				est = firstTask.getRunTime();
				firstTask = tmpAllocated.remove(0);
			}
			return est;
		}
	}
	
	public abstract void sampleNode();

	public double getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}

	public double getLatency() {
		return latency;
	}

	public void setLatency(double latency) {
		this.latency = latency;
	}

	public void undeploy(ContainerInstance vmInstance) {
		capabilities.undeploy(vmInstance);		
	}

	public void deployVM(ContainerInstance vm) {
		capabilities.deploy(vm);
	}

	public ArrayList<MobileSoftwareComponent> getAllocatedTasks() {
		return allocated;
	}

	public void setAllocatedTasks(ArrayList<MobileSoftwareComponent> allocated) {
		this.allocated = allocated;
	}
	
}
