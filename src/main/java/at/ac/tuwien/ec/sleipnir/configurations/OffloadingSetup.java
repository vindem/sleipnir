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

public class OffloadingSetup {
	
	public static final int chessMovesNum = 5;
	
		
	public static double mobileEnergyBudget = 26640.0;	
	public static double batteryCapacity = mobileEnergyBudget * SimulationSetup.mobileNum;
	public static double wifiAvailableProbability = 0.25;
		
	public static double chessMI = 1.0e3;
	public static double facebookImageSize = 20e3;
	public static double facerecImageSize = 10e6;
	public static double navigatorMapSize = 25e6;
	public static double antivirusFileSize = 1000;
		
		
	public static String mobileApplication = "FACEBOOK";

	public static String outfile = "./output";
	
	public static int numberOfParallelApps = 1;
	
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
