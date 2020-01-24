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
	
	public static String timezoneData = null;
	public static void readFromFile(String url){
		
	}
	
	
	public static int MAP_M = 6;
	public static int MAP_N = 6;
	public static int cloudMaxHops = 10;
	public static int cloudCoreNum = 128;
	public static double cloudRam = 256;
	public static double cloudStorage = Double.MAX_VALUE;
	public static double cloudMipsPerCore = 6400000.0;
	public static int edgeCoreNum = 16;
	public static double edgeRam = 128;
	public static double edgeStorage = 5e9;
	public static double edgeMipsPerCore = 80000.0;
	public static EdgePricingModel edgePricingModel = new EdgePricingModel();
	public static CPUEnergyModel edgeCPUEnergyModel = new AMDCPUEnergyModel();
	public static int mobileNum = 1;
	public static double mobileEnergyBudget = 26640.0;
	public static HardwareCapabilities defaultMobileDeviceHardwareCapabilities = 
			new HardwareCapabilities(new Hardware(2,16,(int)16e10),600);
	public static HardwareCapabilities defaultIoTDeviceHardwareCapabilities = 
			new HardwareCapabilities(new Hardware(1,1,1),0);
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
	public static double antivirusFileSize = 1000;
	public static double task_multiplier = 0.1;
	public static int lambdaLatency = 0;
	public static int chessMovesNum = 1;
	public static double chess_mi = 10e3;
	public static double facebookImageSize = 20e3;
	public static double facerecImageSize = 10e3;
	public static double navigatorMapSize = 25e6;
	public static String[] algorithms = {"nsgaIII"};
	public static boolean batch;
	public static double batteryCapacity = mobileEnergyBudget * mobileNum;
	public static int iterations = 10;
	public static int cloudNum = 6;
	public static double EchoGamma;
	public static double EchoAlpha;
	public static double EchoBeta;
	public static double Eta = 1.0;
	public static int appNumber = 2;
	
	public static String[] topics = {"temperature", "moisture", "wind"};
	public static int iotDevicesNum = 36;
	
	public static Hardware defaultDataEntryRequirements = new Hardware(1,1,1);
	public static int dataEntryNum = 2592 ;
	//public static int dataEntryNum = 259200;
	public static String placementAlgorithm = "FFDCPU";
	public static String filename = "testFile";
	public static String traffic ="HIGH";
	public static String area = "LEOPOLDSTADT";
	public static double dataRate = 2.0;
	public static String workloadType = "DATA3";
	public static double y_max = 3224;
	public static double x_max = 3119;
	public static String mobilityTraceFile = "traces/hernals.coords";
	
	public static String mobileApplication = "FACEBOOK";
	public static String edgePlanningAlgorithm;
	public static String electricityTraceFile;
	public static String outfile = "../output/";
	public static int edgeNodes;
	public static boolean cloudOnly;
	public static int numberOfApps = 30;
	
}
