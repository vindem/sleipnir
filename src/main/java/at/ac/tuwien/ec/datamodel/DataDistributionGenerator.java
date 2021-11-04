package at.ac.tuwien.ec.datamodel;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import at.ac.tuwien.ec.model.Hardware;
import at.ac.tuwien.ec.sleipnir.configurations.IoTFaaSSetup;
import at.ac.tuwien.ec.sleipnir.configurations.SimulationSetup;
import at.ac.tuwien.ec.datamodel.DataEntry;

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
		if(IoTFaaSSetup.workloadType.equals("POLLUTION"))
		{
			miDistr = new ExponentialDistribution(250); //cpu-bound
			inData = new ExponentialDistribution(32); //cpu-bound
			outData = new ExponentialDistribution(32); //cpu-bound
			coreD = new ExponentialDistribution(1); //cpu-bound
		}		
		else if(IoTFaaSSetup.workloadType.equals("TRAFFIC"))
		{
			miDistr = new ExponentialDistribution(4000); //data-bound
			inData = new ExponentialDistribution(64800); //data-bound
			outData = new ExponentialDistribution(65200); //data-bound
			coreD = new ExponentialDistribution(4); //data-bound
		}
		else
		{
			miDistr = new ExponentialDistribution(1000); //data-bound
			inData = new ExponentialDistribution(64800); //data-bound
			outData = new ExponentialDistribution(65200); //data-bound
			coreD = new ExponentialDistribution(4); //data-bound
		}
	}
	
	public ArrayList<DataEntry> getGeneratedData()
	{
		if(generatedData.isEmpty())
			generateData();
		return generatedData;
	}

	private void generateData() {
		double mi, inD, outD;
		int coreNum = 1;
		for(int i = 0; i < entryNum; i++) 
		{
			/*
			if(SimulationSetup.workloadType.equals("CPU0"))
			{
				mi = 5e3 + miDistr.sample(); // cpu-bound
				inD = 1e3 + inData.sample(); //cpu-bound
				outD = 1e3 + outData.sample(); //cpu-bound
			}
			else if(SimulationSetup.workloadType.equals("CPU1"))
			{
				mi = 1e4 + miDistr.sample(); // cpu-bound
				inD = 1e3 + inData.sample(); //cpu-bound
				outD = 1e3 + outData.sample(); //cpu-bound
			}
			else if(SimulationSetup.workloadType.equals("CPU2"))
			{
				mi = 5e4 + miDistr.sample(); // cpu-bound
				inD = 1e3 + inData.sample(); //cpu-bound
				outD = 1e3 + outData.sample(); //cpu-bound
			}
			else if(SimulationSetup.workloadType.equals("CPU3"))
			{
				mi = 1e5 + miDistr.sample(); // cpu-bound
				inD = 1e3 + inData.sample(); //cpu-bound
				outD = 1e3 + outData.sample(); //cpu-bound
			}
			else if(SimulationSetup.workloadType.equals("DATA0"))
			{
				mi = 1e3 + miDistr.sample(); // data-bound
				inD = 5e5 + inData.sample(); //data-bound
				outD = 5e5 + outData.sample(); //data-bound
			}
			else if(SimulationSetup.workloadType.equals("DATA1"))
			{
				mi = 1e3 + miDistr.sample(); // data-bound
				inD = 1e6 + inData.sample(); //data-bound
				outD = 1e6 + outData.sample(); //data-bound
			}
			else if(SimulationSetup.workloadType.equals("DATA2"))
			{
				mi = 1e3 + miDistr.sample(); // data-bound
				inD = 5e6 + inData.sample(); //data-bound
				outD = 5e6 + outData.sample(); //data-bound
			}
			else //(SimulationSetup.workloadType.equals("DATA3"))
			{
				mi = 1e3 + miDistr.sample(); // data-bound
				inD = 1e7 + inData.sample(); //data-bound
				outD = 1e7 + outData.sample(); //data-bound
			}*/
			mi = miDistr.sample(); // data-bound
			inD = inData.sample(); //data-bound
			outD = outData.sample(); //data-bound
			generatedData.add(
					new DataEntry("entry"+i,
							new Hardware(coreNum, 1, inD + outD),
							mi,
							"iot"+(i%IoTFaaSSetup.iotDevicesNum),
							inD,
							outD,
							IoTFaaSSetup.topics[i % IoTFaaSSetup.topics.length])
					);
		}
	}
	
	
}
