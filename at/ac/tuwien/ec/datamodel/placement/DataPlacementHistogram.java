package at.ac.tuwien.ec.datamodel.placement;

import java.util.HashMap;

import at.ac.tuwien.ec.scheduling.offloading.OffloadScheduling;

public class DataPlacementHistogram extends HashMap<DataPlacement, DataPlacementStatistics>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5862304123979846289L;
	private String algorithm;
	
	public DataPlacementHistogram()
	{
		super();
	}
	
	public DataPlacementHistogram(String algName)
	{
		super();
		this.algorithm = algName;
	}
	
	public void add(DataPlacement dp, double frequency)
	{
		DataPlacementStatistics stats = new DataPlacementStatistics();
		stats.setFrequency(1.0);
		stats.addAverageLatency(dp.getAverageLatency());
		super.put(dp, stats);
	}
	
	public void update(DataPlacement dp, double freq)
	{
		if(super.containsKey(dp))
		{
			DataPlacementStatistics tmp = super.get(dp);
			tmp.setFrequency(tmp.getFrequency() + 1.0);
			tmp.addAverageLatency(dp.getAverageLatency());
			super.replace(dp, tmp);
		}
	}
	
	public double getAverageLatency(DataPlacement dp) {
		Double tmp = 0.0;
		for(Double d: super.get(dp).getAverageLatency())
			tmp += d;
		return (tmp.doubleValue() / super.get(dp).getFrequency());
	}
	
	public double[] getAverageLatencyConfidenceInterval(DataPlacement dp, double confidenceLevel){
		return super.get(dp).getConfidenceInterval(super.get(dp).getAverageLatency(), confidenceLevel);
	}

}
