package at.ac.tuwien.ec.model.infrastructure.costs;

import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;

import com.opencsv.CSVReader;

public class ElectricityPriceTrace implements Serializable{

	private String filename,timezoneName;
	private ArrayList<Double> trace;
	
	public ElectricityPriceTrace(ArrayList<Double> trace)
	{
		this.trace = trace;
	}
	
	public ElectricityPriceTrace(String filename, String timezoneName) throws Exception
	{
		trace = new ArrayList<Double>();
		this.filename = filename;
		this.timezoneName = timezoneName;
		CSVReader csvReader = null;
		try {
			FileReader filereader = new FileReader(filename);
			csvReader = new CSVReader(filereader);
			String nextRecord[];
			csvReader.readNext();
			while((nextRecord = csvReader.readNext())!=null)
				if(nextRecord[1].equals(timezoneName))
					trace.add(Double.parseDouble(nextRecord[3]));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		csvReader.close();
	}
	
	public int size() {
		return trace.size();
	}

	public double get(int index) {
		return trace.get(index);
	}

}
