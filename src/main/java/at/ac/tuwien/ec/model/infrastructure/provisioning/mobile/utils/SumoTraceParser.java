package at.ac.tuwien.ec.model.infrastructure.provisioning.mobile.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.mobility.SumoTraceMobility;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class SumoTraceParser {
	public static HashMap<String,ArrayList<Coordinates>> tracesList = null; 

	public static void preParse(File inputSumoFile, ArrayList<String> deviceIdList) throws ParserConfigurationException, SAXException, IOException {
		if(tracesList == null)
		{
			tracesList = new HashMap<String,ArrayList<Coordinates>>(); 
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputSumoFile);
			doc.getDocumentElement().normalize();

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
						String currId = vehicle.getAttribute("id");
						if(deviceIdList.contains(currId) )
						{
							double x = Double.parseDouble(vehicle.getAttribute("x"));
							double y = Double.parseDouble(vehicle.getAttribute("y"));
							Coordinates coords = new Coordinates(x,y);
							if(tracesList.containsKey(currId))
								tracesList.get(currId).add(coords);
							else
							{
								tracesList.put(currId,new ArrayList<Coordinates>());
								tracesList.get(currId).add(coords);
							}
						}
					}	
				}

			}
		}
	}

	public static SumoTraceMobility getTrace(String string) {
		ArrayList<Coordinates> currCoords = tracesList.get(string);
		return new SumoTraceMobility(currCoords);
	}

}
