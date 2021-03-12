package at.ac.tuwien.ec.sleipnir.utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import at.ac.tuwien.ec.sleipnir.OffloadingSetup;

public class ConfigFileParser {
	
	public static void parseFile(String fileUrl)  {
		JSONParser jsonParser = new JSONParser();
		
		try(FileReader reader = new FileReader(fileUrl))
		{
			JSONObject obj = (JSONObject) jsonParser.parse(reader);
			OffloadingSetup.area = (String) obj.get("area");
			OffloadingSetup.mobileNum = Integer.parseInt((String) obj.get("mobileNum"));
			OffloadingSetup.cloudNum = Integer.parseInt((String) obj.get("cloudNum"));
			OffloadingSetup.cloudOnly = Boolean.parseBoolean((String) obj.get("cloudonly"));
			OffloadingSetup.numberOfApps = Integer.parseInt((String) obj.get("appNum"));
			OffloadingSetup.Eta = Double.parseDouble((String) obj.get("eta"));
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
