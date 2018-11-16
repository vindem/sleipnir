package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.math.RandomUtils;

import at.ac.tuwien.ec.model.software.MobileSoftwareComponent;
import at.ac.tuwien.ec.model.software.SoftwareComponent;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;


public class MobileDevice extends ComputationalNode {

	private double energyBudget = 0.0;
	
	public MobileDevice(String id, HardwareCapabilities capabilities, double energyBudget) {
		super(id, capabilities);
		this.energyBudget = energyBudget;
	}
	
	public double getEnergyBudget() {
		return energyBudget;
	}

	public void setEnergyBudget(double energyBudget) {
		this.energyBudget = energyBudget;
	}

	public void removeFromBudget(double computeCPUEnergyConsumption) {
		this.energyBudget -= computeCPUEnergyConsumption;
	}

	public void addToBudget(double computeCPUEnergyConsumption) {
		this.energyBudget += computeCPUEnergyConsumption;
		
	}
	
	public String toString(){
		return this.toString();
	}
					
	@Override
	public void sampleNode() {
		int x = RandomUtils.nextInt(SimulationSetup.MAP_M);
		int y = RandomUtils.nextInt(SimulationSetup.MAP_N);;
		this.setCoords(x, y);
	}

	
	
}
