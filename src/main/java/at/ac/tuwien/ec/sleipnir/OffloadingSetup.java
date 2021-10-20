package at.ac.tuwien.ec.sleipnir;

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

public class OffloadingSetup {
	
	public static final int chessMovesNum = 5;
	//AREA PARAMETERS
	public static int MAP_M = 6;
	public static int MAP_N = MAP_M * 2;
	public static double cloudMaxHops = 17.0;
	
	//HARDWARE CONFIGURATIONS
	public static double edgeNodesPerCell = 10;
	public static int mobileNum = 1;
	public static int cloudNum = 6;
	public static int cloudCoreNum = Integer.MAX_VALUE;
	public static double cloudRam = Double.MAX_VALUE;
	public static double cloudStorage = Double.MAX_VALUE;
	public static double cloudMipsPerCore = 25.0e4;
	public static int edgeCoreNum =  (int) (4 * edgeNodesPerCell);
	public static double edgeRam = 128.0 * edgeNodesPerCell;
	public static double edgeStorage = 5e9 * edgeNodesPerCell;
	public static double edgeMipsPerCore = 20.0e4;
	public static EdgePricingModel edgePricingModel = new EdgePricingModel();
	public static CPUEnergyModel edgeCPUEnergyModel = new AMDCPUEnergyModel();
	public static double mobileEnergyBudget = 26640.0;
	public static HardwareCapabilities defaultMobileDeviceHardwareCapabilities = 
			new HardwareCapabilities(new Hardware(2,16,(int)16e10),600);
	public static HardwareCapabilities defaultIoTDeviceHardwareCapabilities = 
			new HardwareCapabilities(new Hardware(1,1,1),0);
	public static CPUEnergyModel defaultMobileDeviceCPUModel = new SamsungS2DualEnergyModel();
	public static NETEnergyModel defaultMobileDeviceNETModel = new Mobile3GNETEnergyModel();
	public static HardwareCapabilities defaultEdgeNodeCapabilities = new HardwareCapabilities(
			new Hardware(OffloadingSetup.edgeCoreNum,
			OffloadingSetup.edgeRam,
			OffloadingSetup.edgeStorage),
			OffloadingSetup.edgeMipsPerCore);
	public static HardwareCapabilities defaultCloudCapabilities = new HardwareCapabilities(
			new Hardware(OffloadingSetup.cloudCoreNum,
			OffloadingSetup.cloudRam,
			OffloadingSetup.cloudStorage),
			OffloadingSetup.cloudMipsPerCore);
	public static double batteryCapacity = mobileEnergyBudget * mobileNum;
	public static double wifiAvailableProbability = 0.25;
		
	public static double chessMI = 1.0e3;
	public static double facebookImageSize = 20e3;
	public static double facerecImageSize = 10e6;
	public static double navigatorMapSize = 25e6;
	public static double antivirusFileSize = 1000;
		
	public static boolean batch;
	
	public static int iterations = 100;
	
	
	public static double Eta = 1.0;

	public static String filename = "./test/testFile.sp";
	public static String area = "HERNALS";

	public static double y_max = 3224;
	public static double x_max = 3119;
	public static String mobilityTraceFile = "traces/hernals.coords";
	
	public static String mobileApplication = "FACEBOOK";

	public static String outfile = "../output/";
	public static int edgeNodes;
	public static boolean cloudOnly;
	public static int numberOfApps = 5;
	public static int numberOfParallelApps = 1;
	public static NETEnergyModel edgeNETEnergyModel = new ComputationalNodeNetEnergyModel();
	
	public static String[] testAlgorithms;
	public static String algoName;
	public static int lambdaLatency = 0;
	public static double antivirusDistr = 0.2;
	public static double facerecDistr = 0.2;
	public static double navigatorDistr = 0.2;
	public static double chessDistr = 0.2;
	public static double facebookDistr = 0.2;
	public static boolean mobility = true;
	public static double EchoGamma;
	public static double EchoAlpha;
	public static double EchoBeta;
	
}
