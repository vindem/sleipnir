package at.ac.tuwien.ec.provisioning.mobile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

//import javax.xml.parsers.ParserConfigurationException;

//import javax.xml.parsers.ParserConfigurationException;

//import org.xml.sax.SAXException;
import org.xml.sax.SAXException;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.MobileCloudInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.model.mobility.SumoTraceMobility;
import at.ac.tuwien.ec.provisioning.mobile.utils.SumoTraceParser;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class MobileDevicePlannerWithMobility implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4303079692763732917L;
	static int mobileNum = OffloadingSetup.mobileNum;
	static double mobileEnergyBudget = OffloadingSetup.mobileEnergyBudget;
	static HardwareCapabilities defaultMobileDeviceHardwareCapabilities 
				= OffloadingSetup.defaultMobileDeviceHardwareCapabilities;
	static CPUEnergyModel defaultMobileDeviceCPUModel = OffloadingSetup.defaultMobileDeviceCPUModel;
	static NETEnergyModel defaultMobileDeviceNetModel = OffloadingSetup.defaultMobileDeviceNETModel;
	
	public static void setupMobileDevices(MobileCloudInfrastructure inf, int number)
	{
		File inputSumoFile = new File(OffloadingSetup.mobilityTraceFile);
		System.out.println("Mobility traces parsing started...");
		ArrayList<String> devIds = new ArrayList<String>();
		for(int i = 0; i < OffloadingSetup.mobileNum; i++)
			devIds.add(""+((double)i));
		try {
			SumoTraceParser.preSAXParse(inputSumoFile, devIds);
			
		} 
		catch (ParserConfigurationException | SAXException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		System.out.println("Mobility traces parsing completed.");
		
		for(int i = 0; i < number; i++)
		{
			
			MobileDevice device = new MobileDevice("mobile_"+i,defaultMobileDeviceHardwareCapabilities.clone());
			device.setCPUEnergyModel(defaultMobileDeviceCPUModel);
			device.setNetEnergyModel(defaultMobileDeviceNetModel);
			SumoTraceMobility mobilityTrace = null;
			mobilityTrace = SumoTraceParser.getTrace(""+((double)i));
			device.setMobilityTrace(mobilityTrace);
			device.setCoords(mobilityTrace.getCoordinatesForTimestep(0.0));
			inf.addMobileDevice(device);
						
		}
	}
	
	
	public static double nodeDistance(NetworkedNode n1, NetworkedNode n2) {
		Coordinates c1,c2;
		
		//mapping coordinates to cells
		double size_x = SimulationSetup.x_max/SimulationSetup.MAP_M;;
		double size_y = SimulationSetup.y_max/(SimulationSetup.MAP_N*2);
		
		c1 = n1.getCoords();
		c2 = n2.getCoords();
		return (Math.abs(Math.round(c1.getLatitude()/size_x)-Math.round(c2.getLatitude()/size_x)) 
				+ Math.max(0, 
						(Math.abs(Math.round(c1.getLatitude()/size_x)-Math.round(c2.getLatitude()/size_x))
								- Math.abs(Math.round(c1.getLongitude()/size_y)-Math.round(c2.getLongitude()/size_y)) )/2));
	}
	

}


