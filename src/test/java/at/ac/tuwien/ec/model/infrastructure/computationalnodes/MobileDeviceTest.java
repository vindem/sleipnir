package at.ac.tuwien.ec.model.infrastructure.computationalnodes;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.HardwareCapabilities;
import at.ac.tuwien.ec.model.infrastructure.energy.CPUEnergyModel;
import at.ac.tuwien.ec.model.infrastructure.energy.NETEnergyModel;
import at.ac.tuwien.ec.model.mobility.SumoTraceMobility;
import at.ac.tuwien.ec.provisioning.mobile.utils.SumoTraceParser;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

class MobileDeviceTest {

	@Test
	final void testIsCompatible() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testDeploy() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testSampleNode() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testMobileDevice() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testGetEnergyBudget() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testSetEnergyBudget() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testRemoveFromBudget() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testAddToBudget() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	final void testToString() {
		fail("Not yet implemented"); // TODO
	}
	
	@Test
	final void testTraceBasedMovement() throws ParserConfigurationException, SAXException, IOException
	{
		HardwareCapabilities defaultMobileDeviceHardwareCapabilities 
		= SimulationSetup.defaultMobileDeviceHardwareCapabilities;
		CPUEnergyModel defaultMobileDeviceCPUModel = SimulationSetup.defaultMobileDeviceCPUModel;
		NETEnergyModel defaultMobileDeviceNetModel = SimulationSetup.defaultMobileDeviceNETModel;
		File testFile = new File("./traces/hernals.coords");
		double mobileEnergyBudget = SimulationSetup.mobileEnergyBudget;
		
		ArrayList<String> ids = new ArrayList<String>();
		ids.add("0.0");
		SumoTraceParser.preParse(testFile, ids);
		
		MobileDevice device = new MobileDevice("mobile_0",defaultMobileDeviceHardwareCapabilities.clone(),mobileEnergyBudget);
		device.setCPUEnergyModel(defaultMobileDeviceCPUModel);
		device.setNetEnergyModel(defaultMobileDeviceNetModel);
		SumoTraceMobility mobilityTrace = SumoTraceParser.getTrace("0.0");
		device.setMobilityTrace(mobilityTrace);
		device.setCoords(mobilityTrace.getCoordinatesForTimestep(0.0));
		double size_x = SimulationSetup.x_max/SimulationSetup.MAP_M;;
		double size_y = SimulationSetup.y_max/(SimulationSetup.MAP_N*2);		
		
		for(int i = 0; i < 10000; i++)
		{
			device.updateCoordsWithMobility(i);
			Coordinates coords = device.getCoords();
			System.out.println("x: " + coords.getLatitude() + " y: " + coords.getLongitude());
			System.out.println("cell, x: " + Math.round(coords.getLatitude()/size_x) 
					+ " y: " + Math.round(coords.getLatitude()/size_y));
		}
			
	}

}
