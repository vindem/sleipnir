package at.ac.tuwien.ec.sleipnir.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class ConfigFileParser {
	
	public static void parseFile(String fileUrl)  {
		JSONParser jsonParser = new JSONParser();
		
		try(FileReader reader = new FileReader(fileUrl))
		{
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
			SimulationSetup.area = (String) obj.get("area");
			SimulationSetup.mobileNum = Integer.parseInt((String) obj.get("mobileNum"));
			SimulationSetup.cloudNum = Integer.parseInt((String) obj.get("cloudNum"));
		} 
		catch (FileNotFoundException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
