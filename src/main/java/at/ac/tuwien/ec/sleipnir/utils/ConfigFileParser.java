package at.ac.tuwien.ec.sleipnir.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.ac.tuwien.ec.sleipnir.configurations.OffloadingSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;

public class ConfigFileParser {
	
	public static void parseFile(String fileUrl)  {
		JSONParser jsonParser = new JSONParser();
		
		try(FileReader reader = new FileReader(fileUrl))
		{
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
			SimulationSetup.area = (String) obj.get("area");
			SimulationSetup.mobileNum = Integer.parseInt((String) obj.get("mobileNum"));
			SimulationSetup.cloudNum = Integer.parseInt((String) obj.get("cloudNum"));
			SimulationSetup.cloudOnly = Boolean.parseBoolean((String) obj.get("cloudonly"));
			SimulationSetup.numberOfApps = Integer.parseInt((String) obj.get("appNum"));
			SimulationSetup.Eta = Double.parseDouble((String) obj.get("eta"));
			OffloadingSetup.outfile = (String) obj.get("outfile");
			OffloadingSetup.navigatorMapSize = Double.parseDouble((String) obj.get("navigatorMapSize"));
			OffloadingSetup.facerecImageSize = Double.parseDouble((String) obj.get("facerecImageSize"));
			OffloadingSetup.facebookImageSize = Double.parseDouble((String) obj.get("facebookImageSize"));
			OffloadingSetup.antivirusFileSize = Double.parseDouble((String) obj.get("antivirusFileSize"));
			OffloadingSetup.chessMI = Double.parseDouble((String) obj.get("chessMi"));
			OffloadingSetup.navigatorDistr = Double.parseDouble((String) obj.get("navigatorDistr"));
			OffloadingSetup.facerecDistr = Double.parseDouble((String) obj.get("facerecDistr"));
			OffloadingSetup.facebookDistr = Double.parseDouble((String) obj.get("facebookDistr"));
			OffloadingSetup.antivirusDistr = Double.parseDouble((String) obj.get("antivirusDistr"));
			OffloadingSetup.chessDistr = Double.parseDouble((String) obj.get("chessDistr"));
			OffloadingSetup.mobility = Boolean.parseBoolean((String)obj.get("mobility"));
			SimulationSetup.iterations = Integer.parseInt((String) obj.get("iter"));
			OffloadingSetup.testAlgorithms = (String[]) ((String) obj.get("algorithm")).split(",");
			OffloadingSetup.EchoAlpha = Double.parseDouble((String) obj.get("alpha"));
			OffloadingSetup.EchoBeta = Double.parseDouble((String) obj.get("beta"));
			OffloadingSetup.EchoGamma = Double.parseDouble((String) obj.get("gamma"));
			reader.close();
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
