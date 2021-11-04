package at.ac.tuwien.ec.provisioning.mobile.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import at.ac.tuwien.ec.model.Coordinates;
import at.ac.tuwien.ec.model.mobility.SumoTraceMobility;
import at.ac.tuwien.ec.sleipnir.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;



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

	public static void preSAXParse(File inputSumoFile, ArrayList<String> deviceIdList, int deviceNumber) throws ParserConfigurationException, SAXException, IOException
	{
		if(tracesList == null) {
			tracesList = new ArrayList<ArrayList<Coordinates>>(); 
			SAXParserFactory SAXFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = SAXFactory.newSAXParser();

			for(int i = 0; i < deviceNumber; i++)
				tracesList.add(new ArrayList<Coordinates>());

			DefaultHandler handler = new DefaultHandler() 
			{
				boolean stopParsing = false;
				public void startElement(String uri, String localName,String qName, 
						Attributes attributes) throws SAXException {
					if(stopParsing)
						throw new SAXException();
					switch(qName)
					{
					case "vehicle":
						String currId = attributes.getValue(0);
						currId = "" + Math.floor(Double.parseDouble(currId));
						if(deviceIdList.contains(currId))
						{
							double x = Double.parseDouble(attributes.getValue("x"));
							double y = Double.parseDouble(attributes.getValue("y"));
							Coordinates coords = new Coordinates(x,y);
							int vehicleId = ((int)Double.parseDouble(currId));
							if(vehicleId <= deviceNumber)
								tracesList.get(vehicleId).add(coords);
							
						}

					}
				}
				 
			};
			saxParser.parse(inputSumoFile, handler);
			
		}
	}

	
	public static SumoTraceMobility getTrace(String string) {
		ArrayList<Coordinates> currCoords = tracesList.get((int)Double.parseDouble(string));
		return new SumoTraceMobility(currCoords);
	}

}
