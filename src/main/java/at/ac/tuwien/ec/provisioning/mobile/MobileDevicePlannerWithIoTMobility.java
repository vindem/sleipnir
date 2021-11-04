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
import at.ac.tuwien.ec.model.infrastructure.MobileDataDistributionInfrastructure;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.IoTDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.MobileDevice;
import at.ac.tuwien.ec.model.infrastructure.computationalnodes.NetworkedNode;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.model.mobility.SumoTraceMobility;
import at.ac.tuwien.ec.provisioning.mobile.utils.SumoTraceParser;
import at.ac.tuwien.ec.sleipnir.configurations.IoTFaaSSetup;
import at.ac.tuwien.ec.sleipnir.configurations.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;

public class MobileDevicePlannerWithIoTMobility implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4303079692763732917L;
	static int mobileNum = SimulationSetup.mobileNum;
	static double mobileEnergyBudget = OffloadingSetup.mobileEnergyBudget;
	static HardwareCapabilities defaultMobileDeviceHardwareCapabilities 
				= SimulationSetup.defaultMobileDeviceHardwareCapabilities;
	static CPUEnergyModel defaultMobileDeviceCPUModel = SimulationSetup.defaultMobileDeviceCPUModel;
	static NETEnergyModel defaultMobileDeviceNetModel = SimulationSetup.defaultMobileDeviceNETModel;
	
	public static void setupMobileDevices(MobileDataDistributionInfrastructure inf, int number)
	{
		File inputSumoFile = new File(SimulationSetup.mobilityTraceFile);
		System.out.println("Mobility traces parsing started...");
		ArrayList<String> devIds = new ArrayList<String>();
		for(int i = 0; i < number; i++)
			devIds.add(""+((double)i));
		try {
			SumoTraceParser.preSAXParse(inputSumoFile, devIds, number);
			
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Mobility traces parsing completed.");
		
		for(int i = 0; i < number; i++)
		{
			
			MobileDevice device = new MobileDevice("vehicle_"+i,defaultMobileDeviceHardwareCapabilities.clone());
			device.setCPUEnergyModel(defaultMobileDeviceCPUModel);
			device.setNetEnergyModel(defaultMobileDeviceNetModel);
			SumoTraceMobility mobilityTrace = null;
			mobilityTrace = SumoTraceParser.getTrace(""+((double)i));
			device.setMobilityTrace(mobilityTrace);
			device.setCoords(mobilityTrace.getCoordinatesForTimestep(0.0));
			inf.addMobileDevice(device);
			
			//depending on setup of traffic
			switch(IoTFaaSSetup.selectedWorkflow)
			{
				case "OF":
				case "IR":
					double minDist = Double.MAX_VALUE;
					String topic = "";
					for(IoTDevice iot : inf.getIotDevices().values())
					{
						double tmp = nodeDistance(device,iot);
						if(tmp < minDist)
						{
							minDist = tmp;
							topic = iot.getTopics()[0];
						}
					}
					inf.subscribeDeviceToTopic(device, topic);
					device.addSubscriberTopic(topic);
					break;
				case "IntraSafed":
					for(IoTDevice iot : inf.getIotDevices().values())
					{
						double tmp = nodeDistance(device,iot);
						if(tmp <= 1)
						{
							inf.subscribeDeviceToTopic(device,iot.getTopics()[0]);
							device.addSubscriberTopic(iot.getTopics()[0]);
						}
					}
					break;
			}
			
						
			
		}
	}
	
	public static void updateDeviceSubscriptions(MobileDataDistributionInfrastructure inf, String workflowType)
	{
		switch(workflowType)
		{
			case "IR":
				for(MobileDevice device : inf.getMobileDevices().values()){
					ArrayList<String> subscriberTopics = device.getSubscriberTopic();
					String currTopic;
					for(int i = 0; i < subscriberTopics.size(); i++) 
					{
						currTopic = subscriberTopics.get(i);
						inf.removeDeviceFromTopic(device, currTopic);
						device.removeSubscription(currTopic);
					}
					double minDist = Double.MAX_VALUE;
					String topic = "";
					for(IoTDevice iot : inf.getIotDevices().values())
					{
						double tmp = nodeDistance(device,iot);
						if(tmp < minDist)
						{
							minDist = tmp;
							topic = iot.getTopics()[0];
						}
					}
					inf.subscribeDeviceToTopic(device, topic);
					device.addSubscriberTopic(topic);
				}
				break;
			case "IntraSafed":
				for(MobileDevice device : inf.getMobileDevices().values()){
					ArrayList<String> subscriberTopics = device.getSubscriberTopic();
					String currTopic;
					for(int i = 0; i < subscriberTopics.size(); i++) 
					{
						currTopic = subscriberTopics.get(i);
						inf.removeDeviceFromTopic(device, currTopic);
						device.removeSubscription(currTopic);
					}
					for(IoTDevice iot : inf.getIotDevices().values())
					{
						double tmp = nodeDistance(device,iot);
						//System.out.println(tmp);
						if(tmp == 0.0)
						{
							inf.subscribeDeviceToTopic(device,iot.getTopics()[0]);
							device.addSubscriberTopic(iot.getTopics()[0]);
						}
					}
				}
				break;
			default:
				break;
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


