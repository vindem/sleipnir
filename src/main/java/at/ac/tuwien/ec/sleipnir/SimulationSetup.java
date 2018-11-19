package at.ac.tuwien.ec.sleipnir;

import org.apache.commons.lang.math.RandomUtils;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.energy.AMDCPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.Mobile3GNETEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.SamsungS2DualEnergyModel;
import at.ac.tuwien.ec.model.pricing.EdgePricingModel;

public class SimulationSetup {
	
	public static void readFromFile(String url){
		
	}
	
	public static RandomUtils rand = new RandomUtils();
	public static int MAP_M = 2;
	public static int MAP_N = 2;
	public static int cloudMaxHops;
	public static int cloudCoreNum;
	public static double cloudRam;
	public static double cloudStorage;
	public static int cloudMipsPerCore;
	public static int edgeCoreNum;
	public static double edgeRam;
	public static double edgeStorage;
	public static double edgeMipsPerCore;
	public static EdgePricingModel edgePricingModel = new EdgePricingModel();
	public static CPUEnergyModel edgeCPUEnergyModel = new AMDCPUEnergyModel();
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
	public static double antivirusFileSize;
	public static double task_multiplier;
	public static int lambdaLatency;
	public static int chessMovesNum;
	public static double chess_mi;
	public static double facebookImageSize;
	public static double facerecImageSize;
	public static double navigatorMapSize;
	public static String[] algorithms;
	public static boolean batch;
	public static double batteryCapacity;

}
