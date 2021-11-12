package at.ac.tuwien.ec.sleipnir.configurations;

import at.ac.tuwien.ec.model.Hardware;

public class IoTFaaSSetup {
	public static Hardware defaultDataEntryRequirements = new Hardware(1,1,1);
	public static int dataEntryNum = 2592 ;
	//public static int dataEntryNum = 259200;
	public static String[] placementAlgorithms = {"PEFT"};
	public static String traffic ="HIGH";
	public static String[] topics = {"temperature", "moisture", "wind"};
	public static int iotDevicesNum = 36;
    public static String workloadType = "DATA3";
	public static int nCenters = 1;
	public static double updateTime = 10.0;
	public static String selectedWorkflow = "IR";
	public static double dataMultiplier = 1;
	public static double IRParameter = 400e3;
	public static double IntraSafedParameter = 400e3;
	public static double dataRate = 2.0;
}
