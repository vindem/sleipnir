package at.ac.tuwien.ec.sleipnir.configurations;

import java.util.ArrayList;

import org.apache.commons.lang.math.RandomUtils;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.energy.AMDCPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.ComputationalNodeNetEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.Mobile3GNETEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.SamsungS2DualEnergyModel;
import at.ac.tuwien.ec.model.pricing.EdgePricingModel;

public class SimulationSetup {
	
	//AREA PARAMETERS
	public static int MAP_M = 6;
	public static int MAP_N = MAP_M * 2;
	public static double cloudMaxHops = 17.0;
	public static String area = "HERNALS";
	public static double y_max = 3224;
	public static double x_max = 3119;
	public static String mobilityTraceFile = "traces/hernals.coords";
	public static NETEnergyModel edgeNETEnergyModel = new ComputationalNodeNetEnergyModel();
	public static int edgeNodeLimit = 900;

	//HARDWARE CONFIGURATIONS
	public static int edgeNodesPerCell = 8;
	public static int mobileNum = 1;
	public static int cloudNum = 6;
	//public static int cloudCoreNum = Integer.MAX_VALUE;
	public static int cloudCoreNum = 32;
	public static double cloudRam = Double.MAX_VALUE;
	public static double cloudStorage = Double.MAX_VALUE;
	//public static double cloudMipsPerCore = 25.0e4;
	public static double cloudMipsPerCore = 25.0e4;
	//public static int edgeCoreNum =  (int) (4 * edgeNodesPerCell);
	public static int edgeCoreNum = 4 * edgeNodesPerCell;
	public static double edgeRam = 128.0 * edgeNodesPerCell;
	public static double edgeStorage = 5e9 * edgeNodesPerCell;
	public static double edgeMipsPerCore = 20.0e4;
	public static int edgeNodes;
	public static boolean cloudOnly;
	public static EdgePricingModel edgePricingModel = new EdgePricingModel();
	public static CPUEnergyModel edgeCPUEnergyModel = new AMDCPUEnergyModel();

	public static HardwareCapabilities defaultMobileDeviceHardwareCapabilities = 
			new HardwareCapabilities(new Hardware(2,16,(int)16e10),600);
	public static HardwareCapabilities defaultIoTDeviceHardwareCapabilities = 
			new HardwareCapabilities(new Hardware(1,1,1),0);
	public static CPUEnergyModel defaultMobileDeviceCPUModel = new SamsungS2DualEnergyModel();
	public static NETEnergyModel defaultMobileDeviceNETModel = new Mobile3GNETEnergyModel();
	public static HardwareCapabilities defaultEdgeNodeCapabilities = new HardwareCapabilities(
			new Hardware(edgeCoreNum,
					edgeRam,
					edgeStorage),
			edgeMipsPerCore);
	public static HardwareCapabilities defaultCloudCapabilities = new HardwareCapabilities(
			new Hardware(cloudCoreNum,
					cloudRam,
					cloudStorage),
			cloudMipsPerCore);
			
	public static String[] algorithms = {"PEFT"};
	public static boolean batch;
	
	public static int iterations = 5;
	public static double Eta = 1.0;
	
	public static String configurationJsonFile = "./config/simulation.json";
		
	public static int numberOfApps = 1;
	public static int numberOfParallelApps = 1;
	
	public static ArrayList<Coordinates> admissibleEdgeCoordinates;
	public static ArrayList<Coordinates> iotDevicesCoordinates;
	public static ArrayList<Double> failureProbList;
	public static String edgePlanningAlgorithm = "ares";
	public static String electricityTraceFile;
	public static String outfile = "./output/test";
	public static boolean mobility = true;
	public static String timezoneData;
	public static double wifiAvailableProbability;

	
}
