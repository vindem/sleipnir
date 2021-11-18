package at.ac.tuwien.ec.model;

import java.io.Serializable;

import at.ac.tuwien.ec.model.infrastructure.computationalnodes.ContainerInstance;
import at.ac.tuwien.ec.model.software.SoftwareComponent;


public class HardwareCapabilities implements Serializable{

	private double mipsPerCore;
	private Hardware capabilities;
	private int maxCores;
	private double maxRam, maxStorage;
	
	public HardwareCapabilities(Hardware capabilities, double mipsPerCore) {
		this.capabilities = capabilities;
		this.mipsPerCore = mipsPerCore;
		this.maxCores = capabilities.getCores();
		this.maxRam = capabilities.getRam();
		this.maxStorage = capabilities.getStorage();
	}

	public int getMaxCores() {
		return maxCores;
	}

	public void setMaxCores(int maxCores) {
		this.maxCores = maxCores;
	}

	public double getMaxRam() {
		return maxRam;
	}

	public void setMaxRam(double maxRam) {
		this.maxRam = maxRam;
	}

	public double getMaxStorage() {
		return maxStorage;
	}

	public void setMaxStorage(double maxStorage) {
		this.maxStorage = maxStorage;
	}

	public int getAvailableCores() {
		return capabilities.cores;
	}
	
	public boolean supports(Hardware request)
	{
		return capabilities.cores >= request.cores &&
				capabilities.ram >= request.ram &&
				capabilities.storage >= request.storage;
	}
	
	public boolean deploy(SoftwareComponent cmp){
		if(!supports(cmp.getHardwareRequirements()))
			return false;
		capabilities.cores -= cmp.getHardwareRequirements().cores;
		capabilities.ram -= cmp.getHardwareRequirements().ram;
		capabilities.storage -= cmp.getHardwareRequirements().storage;
		return true;
		
	}
	
	public boolean deploy(ContainerInstance vm){
		if(!supports(new Hardware(vm.getCapabilities().maxCores, vm.getCapabilities().maxRam, vm.getCapabilities().maxStorage)))
			return false;
		capabilities.cores -= vm.getCapabilities().maxCores;
		capabilities.ram -= vm.getCapabilities().maxRam;
		capabilities.storage -= vm.getCapabilities().maxStorage;
		return true;
		
	}
	
	public void undeploy(ContainerInstance vm) {
		capabilities.cores += vm.getCapabilities().maxCores;
		capabilities.ram += vm.getCapabilities().maxRam;
		capabilities.storage += vm.getCapabilities().maxStorage;
	}

	public double getMipsPerCore() {
		return this.mipsPerCore;
	}

	public void undeploy(SoftwareComponent cmp) {
		capabilities.cores += cmp.getHardwareRequirements().cores;
		capabilities.ram += cmp.getHardwareRequirements().ram;
		capabilities.storage += cmp.getHardwareRequirements().storage;
	}
	
	public double getAvailableCPUResources()
	{
		return (double)capabilities.cores / (double)maxCores;
	}

	public Hardware getHardware()
	{
		return capabilities;
	}
	
	public HardwareCapabilities clone()
	{
		return new HardwareCapabilities(capabilities.clone(),mipsPerCore);
	}

	public void setMipsPerCore(double mipsPerCore) {
		this.mipsPerCore = mipsPerCore;
	}

	
	
}
