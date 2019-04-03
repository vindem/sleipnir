package at.ac.tuwien.ec.datamodel.placement;

import java.util.ArrayList;

public class DataPlacementStatistics {
	
	private double frequency;
	private ArrayList<Double> averageLatency;
	
	public DataPlacementStatistics()
	{
		averageLatency = new ArrayList<Double>();
	}
	
	public void addAverageLatency(double latency)
	{
		averageLatency.add(latency);
	}
	
	public double calculateMean(ArrayList<Double> sample){
		Double tmp = 0.0;
		for(Double d: sample)
			tmp += d;
		return tmp / sample.size();
	}
	
	public double calculateVariance(ArrayList<Double> sample){
		Double tmp = 0.0;
		double mean = calculateMean(sample);
		for(Double d:sample)
			tmp += (d-mean)*(d-mean);
		return tmp / sample.size();
	}
	
	public double calculateStandardDeviation(ArrayList<Double> sample){
		return Math.sqrt(calculateVariance(sample));
	}
	
	public double[] getConfidenceInterval(ArrayList<Double> sample, double confidenceLevel){
		double[] interval = new double[2];
		double mean = calculateMean(sample);
		double stDv = calculateStandardDeviation(sample);
		double cfVal = (confidenceLevel * stDv) / Math.sqrt(sample.size()); 
		interval[0] = mean - cfVal;
		interval[1] = mean + cfVal;
		return interval;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	public ArrayList<Double> getAverageLatency() {
		return averageLatency;
	}

}
