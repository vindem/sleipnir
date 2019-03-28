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
	
	private ExponentialDistribution miDistr,inData,outData,coreD;
	
	public DataDistributionGenerator(int entryNum)
	{
		generatedData = new ArrayList<DataEntry>();
		this.entryNum = entryNum;
		miDistr = new ExponentialDistribution(2e3);
		inData = new ExponentialDistribution(5e3);
		outData = new ExponentialDistribution(5e3);
		coreD = new ExponentialDistribution(4);
	}
	
	public ArrayList<DataEntry> getGeneratedData()
	{
		if(generatedData.isEmpty())
			generateData();
		return generatedData;
	}

	private void generateData() {
		double mi, inD, outD;
		int coreNum;
		for(int i = 0; i < entryNum; i++) 
		{
			do
				coreNum = (int) ((int) 1 + coreD.sample());
			while(coreNum > 16 || coreNum < 1);
			mi = 1e6 + miDistr.sample();
			inD = 5e6 + inData.sample();
			outD = 5e6 + outData.sample();
			generatedData.add(
					new DataEntry("entry"+i,
					new Hardware(coreNum, 1, inD + outD),
					mi,
					"iot"+(i%SimulationSetup.iotDevicesNum),
					inD,
					outD,
					SimulationSetup.topics[i % SimulationSetup.topics.length])
					);
		}
	}
	
	
}
