package at.ac.tuwien.ac.datamodel;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.sleipnir.SimulationSetup;

public class DataDistributionGenerator implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1230409659206834914L;
	private ArrayList<DataEntry> generatedData;
	private int entryNum;
	
	private ExponentialDistribution miDistr,inData,outData;
	
	public DataDistributionGenerator(int entryNum)
	{
		generatedData = new ArrayList<DataEntry>();
		this.entryNum = entryNum;
		miDistr = new ExponentialDistribution(20000);
		inData = new ExponentialDistribution(50000);
		outData = new ExponentialDistribution(50000);
	}
	
	public ArrayList<DataEntry> getGeneratedData()
	{
		if(generatedData.isEmpty())
			generateData();
		return generatedData;
	}

	private void generateData() {
		double mi, inD, outD;
		do
		{
			mi = miDistr.sample();
		}
		while( mi <= 0);
		do
		{
			inD = miDistr.sample();
		}
		while( inD <= 0);
		do
		{
			outD = miDistr.sample();
		}
		while( outD <= 0);
		for(int i = 0; i < entryNum; i++)
			generatedData.add(
					new DataEntry("entry"+i,
					new Hardware(1, 1, 1),
					miDistr.sample(),
					"iot"+(i%SimulationSetup.iotDevicesNum),
					inData.sample(),
					outData.sample(),
					SimulationSetup.topics[i % SimulationSetup.topics.length])
					);
	}
	
	
}
