package at.ac.tuwien.ec.model.infrastructure.planning.mobile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.math.RandomUtils;
import org.xml.sax.SAXException;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.planning.mobile.utils.SumoTraceParser;
import at.ac.tuwien.ec.model.mobility.SumoTraceMobility;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class MobileDevicePlannerWithMobility implements Serializable{
	
	static int mobileNum = SimulationSetup.mobileNum;
	static double mobileEnergyBudget = SimulationSetup.mobileEnergyBudget;
	static HardwareCapabilities defaultMobileDeviceHardwareCapabilities 
				= SimulationSetup.defaultMobileDeviceHardwareCapabilities;
	static CPUEnergyModel defaultMobileDeviceCPUModel = SimulationSetup.defaultMobileDeviceCPUModel;
	static NETEnergyModel defaultMobileDeviceNetModel = SimulationSetup.defaultMobileDeviceNETModel;
	
	public static void setupMobileDevices(MobileDataDistributionInfrastructure inf, int number)
	{
		File inputSumoFile = new File("traces/hernals.coords");
		System.out.println("Mobility traces parsing started...");
		ArrayList<String> devIds = new ArrayList<String>();
		for(int i = 0; i < SimulationSetup.mobileNum; i++)
			devIds.add(""+((double)i));
		try {
			SumoTraceParser.preParse(inputSumoFile, devIds);
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Mobility traces parsing completed.");
		
		for(int i = 0; i < number; i++)
		{
			
			MobileDevice device = new MobileDevice("mobile_"+i,defaultMobileDeviceHardwareCapabilities.clone(),mobileEnergyBudget);
			device.setCPUEnergyModel(defaultMobileDeviceCPUModel);
			device.setNetEnergyModel(defaultMobileDeviceNetModel);
			String deviceId = null;
			SumoTraceMobility mobilityTrace = null;
			mobilityTrace = SumoTraceParser.getTrace(""+((double)i));
			device.setMobilityTrace(mobilityTrace);
			inf.addMobileDevice(device);
			device.setCoords(mobilityTrace.getCoordinatesForTimestep(0.0));
			//depending on setup of traffic
			
			switch(SimulationSetup.traffic)
			{
			case "LOW": //subscription only to the closest
				double minDist = Double.MAX_VALUE;
				String targetId = "";
				for(IoTDevice iot : inf.getIotDevices().values())
				{
					double tmpDist = nodeDistance(device,iot);
					if(tmpDist < minDist)
					{
						minDist = tmpDist;
						targetId = iot.getId();
					}
						
				}
				inf.subscribeDeviceToTopic(device, targetId);
				break;
			case "MEDIUM":
				int startIndex = (i%2==0)? 0 : 1;
				for(int j = startIndex; j < SimulationSetup.iotDevicesNum; j+=2)
					inf.subscribeDeviceToTopic(device, "iot"+j);
				break;
			case "HIGH":
				for(int j = 0; j < SimulationSetup.iotDevicesNum; j++)
					inf.subscribeDeviceToTopic(device, "iot"+j);
				break;
			}
			
		}
	}
	
	public static double nodeDistance(NetworkedNode n1, NetworkedNode n2) {
		Coordinates c1,c2;
		
		c1 = n1.getCoords();
		c2 = n2.getCoords();
		
		return (Math.abs(c1.getLatitude()-c2.getLatitude()) 
				+ Math.max(0, 
						(Math.abs(c1.getLatitude()-c2.getLatitude())
								- Math.abs(c1.getLongitude()-c2.getLongitude()) )/2));
	}
	

}


