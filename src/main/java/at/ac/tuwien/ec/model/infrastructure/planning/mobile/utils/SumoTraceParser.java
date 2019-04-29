package at.ac.tuwien.ec.model.infrastructure.planning.mobile.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.mobility.SumoTraceMobility;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class SumoTraceParser {

	public static SumoTraceMobility parse(File inputSumoFile, String deviceId) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(inputSumoFile);
		doc.getDocumentElement().normalize();
		ArrayList<Coordinates> coordsForDevId = new ArrayList<Coordinates>();
		
		NodeList nList = doc.getElementsByTagName("timestep");
		for(int i = 0; i < nList.getLength(); i++)
		{
			Node timeStep = nList.item(i);
			NodeList vehicles = timeStep.getChildNodes();
			for(int j = 0; j < vehicles.getLength(); j++)
			{
				if(vehicles.item(j).getNodeType() == Node.ELEMENT_NODE)
				{
					Element vehicle = (Element) vehicles.item(j);
					if(vehicle.getAttribute("id").equals(deviceId) )
					{
						double x = Double.parseDouble(vehicle.getAttribute("x"));
						double y = Double.parseDouble(vehicle.getAttribute("y"));
						Coordinates coords = new Coordinates(x,y);
						coordsForDevId.add(coords);
					}
				}	
			}

		}
		return new SumoTraceMobility(coordsForDevId);
	}

}
