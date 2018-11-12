package at.ac.tuwien.ec.sleipnir;

import org.apache.commons.lang.math.RandomUtils;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.Mobile3GNETEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.SamsungS2DualEnergyModel;
import at.ac.tuwien.ec.model.pricing.EdgePricingModel;

public class SimulationSetup {
	
	public static void readFromFile(String url){
		
	}
	
	public static RandomUtils rand = new RandomUtils();
	public static int MAP_M;
	public static int MAP_N;
	public static int cloudMaxHops;
	public static int cloudCoreNum;
	public static double cloudRam;
	public static double cloudStorage;
	public static int cloudMipsPerCore;
	public static int edgeCoreNum;
	public static double edgeRam;
	public static double edgeStorage;
	public static double edgeMipsPerCore;
	public static EdgePricingModel edgePricingModel;
	public static CPUEnergyModel edgeCPUEnergyModel;
	public static int mobileNum;
	public static double mobileEnergyBudget;
	public static HardwareCapabilities defaultMobileDeviceHardwareCapabilities = 
			new HardwareCapabilities(new Hardware(2,16,(int)16e10),600);
	public static CPUEnergyModel defaultMobileDeviceCPUModel = new SamsungS2DualEnergyModel();
	public static NETEnergyModel defaultMobileDeviceNETModel = new Mobile3GNETEnergyModel();
	public static double wifiAvailableProbability;
	public static HardwareCapabilities defaultEdgeNodeCapabilities = new HardwareCapabilities(
			new Hardware(SimulationSetup.edgeCoreNum,
			SimulationSetup.edgeRam,
			SimulationSetup.edgeStorage),
			SimulationSetup.edgeMipsPerCore);
	public static HardwareCapabilities defaultCloudCapabilities = new HardwareCapabilities(
			new Hardware(SimulationSetup.cloudCoreNum,
			SimulationSetup.cloudRam,
			SimulationSetup.cloudStorage),
			SimulationSetup.cloudMipsPerCore);

}
