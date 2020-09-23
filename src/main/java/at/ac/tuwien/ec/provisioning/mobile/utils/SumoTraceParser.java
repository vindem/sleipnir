package at.ac.tuwien.ec.provisioning.mobile.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.mobility.SumoTraceMobility;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class SumoTraceParser {
	public static ArrayList<ArrayList<Coordinates>> tracesList = null; 

	public static void preParse(File inputSumoFile, ArrayList<String> deviceIdList) throws ParserConfigurationException, SAXException, IOException {
		if(tracesList == null)
		{
			tracesList = new ArrayList<ArrayList<Coordinates>>(); 
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inputSumoFile);
			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("timestep");
			
			for(int i = 0; i < SimulationSetup.mobileNum; i++)
				tracesList.add(new ArrayList<Coordinates>());
			
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
							tracesList.get(((int)Double.parseDouble(currId))).add(coords);
							/*if(tracesList.containsKey(currId))
								tracesList.get(currId).add(coords);
							else
							{
								tracesList.put(currId,new ArrayList<Coordinates>());
								tracesList.get(currId).add(coords);
							}*/
						}
					}	
				}

			}
		}
	}

	public static SumoTraceMobility getTrace(String string) {
		ArrayList<Coordinates> currCoords = tracesList.get((int)Double.parseDouble(string));
		return new SumoTraceMobility(currCoords);
	}

}
